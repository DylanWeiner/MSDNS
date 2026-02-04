import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/*
                0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
               +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
               |                      ID                       |
               +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
               |QR|   OpCode  |AA|TC|RD|RA| Z|AD|CD|   RCODE   |
               +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
               |                QDCOUNT/ZOCOUNT                |
               +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
               |                ANCOUNT/PRCOUNT                |
               +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
               |                NSCOUNT/UPCOUNT                |
               +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
               |                    ARCOUNT                    |
               +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

*/

public class DNSHeader {
    short ID; // Top of header
    short flags;
    short QDCount;
    short ANCount;
    short NSCount;
    short ARCount;

    boolean QR, AA, TC, RD, RA, Z, AD, CD; // These are the bytes btwn the opcode and rcode.
    int opCode;
    int rCode;

    static DNSHeader decodeHeader(InputStream inpStr) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(inpStr.readNBytes(12));
        DNSHeader header = new DNSHeader();

        // Extracts values of the shorts from the messages in order.
        header.ID = buffer.getShort();
        header.flags = buffer.getShort();
        header.QDCount = buffer.getShort();
        header.ANCount = buffer.getShort();
        header.NSCount = buffer.getShort();
        header.ARCount = buffer.getShort();

        // Extracts the bytes in the flags of the code off whether or not they are present.
        header.QR = ((header.flags >> 15) & 0x01) == 1;
        header.opCode = (header.flags >> 11) & 0x0f; // Opcode is 4 bytes
        header.AA = ((header.flags >> 10) & 0x01) == 1;
        header.TC = ((header.flags >> 9) & 0x01) == 1;
        header.RD = ((header.flags >> 8) & 0x01) == 1;
        header.RA = ((header.flags >> 7) & 0x01) == 1;
        header.Z = ((header.flags >> 6) & 0x01) == 1;
        header.AD = ((header.flags >> 5) & 0x01) == 1;
        header.rCode = header.flags & 0x0f; // rCode is 4 bytes and won't require any bitwise shifting.

        return header;
    }

    static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) {
        DNSHeader requestHeader = request.getHeaderForResponse();
        DNSHeader responseHeader = response.getHeaderForResponse();

        responseHeader.QR = requestHeader.QR;
        responseHeader.opCode = requestHeader.opCode;
        responseHeader.AA = requestHeader.AA;
        responseHeader.TC = requestHeader.TC;
        responseHeader.RD = requestHeader.RD;
        responseHeader.RA = requestHeader.RA;
        responseHeader.Z = requestHeader.Z;
        responseHeader.AD = requestHeader.AD;
        responseHeader.rCode = requestHeader.rCode;

        return responseHeader;
    }

    void writeBytes(OutputStream outStr) throws IOException {
        // Write the question bytes which will be sent to the client. The hash map is used for us to compress the message, see the DNSMessage class below
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort(ID);
        buffer.putShort(flags);
        buffer.putShort(QDCount);
        buffer.putShort(ANCount);
        buffer.putShort(NSCount);
        buffer.putShort(ARCount);

        outStr.write(buffer.array());
    }

    int getQDCount() {
        return QDCount;
    }

    int getANCount() {
        return ANCount;
    }

    @Override
    public String toString() {
        return "DNSHeader{" +
                "ID=" + ID +
                ", flags=" + flags +
                ", QDCount=" + QDCount +
                ", ANCount=" + ANCount +
                ", NSCount=" + NSCount +
                ", ARCount=" + ARCount +
                ", QR=" + QR +
                ", AA=" + AA +
                ", TC=" + TC +
                ", RD=" + RD +
                ", RA=" + RA +
                ", Z=" + Z +
                ", AD=" + AD +
                ", CD=" + CD +
                ", opCode=" + opCode +
                ", rCode=" + rCode +
                '}';
    }
}