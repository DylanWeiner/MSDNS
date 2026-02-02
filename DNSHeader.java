import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class DNSHeader {
    static DNSHeader decodeHeader(InputStream) {
        byte[] allBytes = new byte[512];
        ByteArrayInputStream header = new ByteArrayInputStream(allBytes);
        byte[] headerBytes = new byte[12];

        try {
            int bytesRead = headerBytes.read(headerBytes, 0, 12);

            if (bytesRead < 12) {
                headerBytes = Arrays.copyOf(headerBytes, bytesRead);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        String headerString = new String(header);
        System.out.println(headerString);
    }

    static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) {

    }

    void writeBytes(OutputStream) {

    }

    String toString() {

    }
}