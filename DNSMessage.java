
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DNSMessage {
    DNSHeader header;
    DNSQuestion[] questions;
    DNSRecord[] answerRecords;
    DNSRecord[] authority;
    DNSRecord[] additional;
    static byte[] messageBytes;

    int troubleshoot = 0;

    /*
    an array of questions
    an array of answers
    an array of "authority records" which we'll ignore
    an array of "additional records" which we'll almost ignore
     */

    DNSMessage() {}

    DNSHeader getHeaderForResponse() {
        return header;
    }


    DNSQuestion[] getQuestionForResponse() {
        return questions;
    }

    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        messageBytes = bytes;
        DNSMessage msg = new DNSMessage();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        msg.header = DNSHeader.decodeHeader(bais);
        int qd = msg.header.getQDCount();
        msg.questions = new DNSQuestion[qd];

        for (int i = 0; i < qd; i++) {
            msg.questions[i] = DNSQuestion.decodeQuestion(bais, msg);
        }

        int an = msg.header.getANCount();
        msg.answerRecords = new DNSRecord[an];

        for (int i = 0; i < an; i++) {
            msg.answerRecords[i] = DNSRecord.decodeRecord(bais, msg);
        }

        msg.authority = new DNSRecord[0];
        msg.additional = new DNSRecord[0];

        return msg;
    }

    String[] readDomainName(InputStream inpStr) throws IOException {
        // domain name is read as length-prefixed labels, terminated with 0
        List<String> domainName = new ArrayList<>();
        while(true) {
            int length = inpStr.read();
            if(length == 0) {
                // End of domain name
                break;
            } else if((length & 0xc0) == 0xc0) {
                // Compression pointer - top 2 bits are 11
                int secondByte = inpStr.read();
                int pointerOffset = ((length & 0x3f) << 8) | secondByte;
                // Recursively read the domain from the pointer location
                domainName.addAll(Arrays.asList(readDomainName(pointerOffset)));
                break;  // Pointer ends the name
            } else {
                // Normal label - read 'length' bytes
                byte[] labelBytes = new byte[length];
                inpStr.read(labelBytes);
                domainName.add(new String(labelBytes));
            }
        }
        return domainName.toArray(new String[0]);
    }
    // Read the pieces of a domain name starting from the current position of the input stream

    String[] readDomainName(int firstByte) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(messageBytes, firstByte, messageBytes.length - firstByte);

        return readDomainName(bais);
    }
    // Same, but used when there's compression and we need to find the domain from earlier in the message. This method should make a ByteArrayInputStream that starts at the specified byte and call the other version of this method

    static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers) {
        DNSMessage msg = new DNSMessage();

        msg.questions = request.getQuestionForResponse();
        msg.answerRecords = answers;
        msg.authority = new DNSRecord[0];
        msg.additional = new DNSRecord[0];
        
        // Create a new header for the response
        msg.header = new DNSHeader();
        msg.header = DNSHeader.buildHeaderForResponse(request, msg);

        return msg;
    }
    // Build a response based on the request and the answers you intend to send back.

    byte[] toBytes() throws IOException {
        // Call the write bytes functions from the other classes.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // Update header counts before writing
        header.setQDCount((short) questions.length);
        header.setANCount((short) answerRecords.length);
        header.setNSCount((short) authority.length);
        header.setARCount((short) additional.length);
        header.rebuildFlags();
        
        header.writeBytes(bos);
        
        // Create a map to track domain names for compression
        HashMap<String, Integer> domainLocations = new HashMap<>();

        // Write questions without compression in the map (but still record them)
        for (DNSQuestion q : questions) {
            int startPos = bos.size();
            String fullDomain = String.join(".", q.domain);
            if (!domainLocations.containsKey(fullDomain)) {
                domainLocations.put(fullDomain, startPos);
            }
            q.writeBytes(bos, null);  // Don't use compression for questions
        }
        
        // Write answers with compression
        for (DNSRecord rec : answerRecords) {
            rec.writeBytes(bos, domainLocations);
        }
        for (DNSRecord rec : authority) {
            rec.writeBytes(bos, domainLocations);
        }
        for (DNSRecord rec : additional) {
            rec.writeBytes(bos, domainLocations);
        }
        return bos.toByteArray();
    } // Get the bytes to put in a packet and send back

    static void writeDomainName(ByteArrayOutputStream byteStream, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {
        if (domainPieces == null || domainPieces.length == 0) {
            // Write just the root domain (0 byte)
            byteStream.write(0);
            return;
        }
        
        // Build the full domain name for checking compression
        StringBuilder fullDomain = new StringBuilder();
        for (int i = 0; i < domainPieces.length; i++) {
            if (i > 0) fullDomain.append(".");
            fullDomain.append(domainPieces[i]);
        }
        
        // Check if we've already written this domain (for compression)
        if (domainLocations != null && domainLocations.containsKey(fullDomain.toString())) {
            // Write a pointer to the previous location
            int location = domainLocations.get(fullDomain.toString());
            int pointer = 0xC000 | location;  // 0xC0 signals a pointer
            byteStream.write((pointer >> 8) & 0xFF);
            byteStream.write(pointer & 0xFF);
        } else {
            // First time seeing this domain - write it out with DNS encoding
            int startPos = byteStream.size();
            if (domainLocations != null) {
                domainLocations.put(fullDomain.toString(), startPos);
            }
            
            for (String piece : domainPieces) {
                byteStream.write(piece.length());
                byteStream.write(piece.getBytes());
            }
            // Write the terminating zero
            byteStream.write(0);
        }
    }
    // If this is the first time we've seen this domain name in the packet, write it using the DNS encoding (each segment of the domain prefixed with its length, 0 at the end), and add it to the hash map. Otherwise, write a back pointer to where the domain has been seen previously.
    // I might not need this.

    @Override
    public String toString() {
        return "DNSMessage{" +
                "header=" + header +
                ", questions=" + Arrays.toString(questions) +
                ", answerRecords=" + Arrays.toString(answerRecords) +
                ", authority=" + Arrays.toString(authority) +
                ", additional=" + Arrays.toString(additional) +
                ", messageBytes=" + Arrays.toString(messageBytes) +
                '}';
    }
}

// Use an input stream
// save the message as a DNSMessage
