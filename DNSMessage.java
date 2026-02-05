
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
        // domain name starts at 0xc0
        List<String> domainName = new ArrayList<>();
        char charVal;
        while(true) {
            int leftOffsetVal = inpStr.read();
            if(leftOffsetVal == 0) break;
            if((leftOffsetVal & 0xc0) == 0xc0) {
                int byteRead = inpStr.read();
                charVal = (char) (((leftOffsetVal & 0x3f) << 8) | byteRead);
                domainName.addAll(Arrays.asList(readDomainName(charVal)));
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

        msg.header = DNSHeader.buildHeaderForResponse(request, msg);

        return msg;
    }
    // Build a response based on the request and the answers you intend to send back.

    byte[] toBytes() throws IOException {
        // Call the write bytes functions from the other classes.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        header.writeBytes(bos);

        for (DNSQuestion q : questions) {
            q.writeBytes(bos, null);
        }
        for (DNSRecord rec : answerRecords) {
            rec.writeBytes(bos, null);
        }
        for (DNSRecord rec : authority) {
            rec.writeBytes(bos, null);
        }
        for (DNSRecord rec : additional) {
            rec.writeBytes(bos, null);
        }
        return bos.toByteArray();
    } // Get the bytes to put in a packet and send back

    static void writeDomainName(ByteArrayOutputStream byteStream, HashMap<String,Integer> domainLocations, String[] domainPieces) {

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
