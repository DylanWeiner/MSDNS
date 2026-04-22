import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class DNSRecord {
    String[] domain;
    int type;
    int DNSClass;
    int lifespan;
    byte[] data;  // Changed from static to instance variable
    Date today;

    DNSRecord(String[] domain, int type, int DNSClass, int lifespan, byte[] data) {
        this.domain = domain;
        this.type = type;
        this.DNSClass = DNSClass;
        this.lifespan = lifespan;
        this.data = data;
        today = new Date();
    }

    static DNSRecord decodeRecord(InputStream inpStr, DNSMessage msg) throws IOException {
        String[] domainName = msg.readDomainName(inpStr); // retrieves bytes for domain name.

        int decodeType = inpStr.read() << 8 | inpStr.read();
        int decodeClass = inpStr.read() << 8 | inpStr.read(); // Reads input string to retrieve the Question's type and class.
        // TTL is a 4-byte field
        int lifetime = (inpStr.read() << 24) | (inpStr.read() << 16) | (inpStr.read() << 8) | inpStr.read();
        // RDLENGTH is a 2-byte field
        int recordTime = inpStr.read() << 8 | inpStr.read();
        byte[] recordData = new byte[recordTime];
        inpStr.read(recordData);

        DNSRecord record = new DNSRecord(domainName, decodeType, decodeClass, lifetime, recordData);

        return record;
    }

    void writeBytes(ByteArrayOutputStream byteStream, HashMap<String, Integer> recordBytes) throws IOException {
        DNSMessage.writeDomainName(byteStream, recordBytes, this.domain);

        byteStream.write((type >> 8) & 0xff); // Places input type into the byte stream.
        byteStream.write(type & 0xff);
        byteStream.write((DNSClass >> 8) & 0xff); // Places the input class into the byte stream.
        byteStream.write(DNSClass & 0xff);
        byteStream.write((lifespan >> 24) & 0xff);
        byteStream.write((lifespan >> 16) & 0xff);
        byteStream.write((lifespan >> 8) & 0xff); // Places the lifespan into the byte stream.
        byteStream.write(lifespan & 0xff);
        byteStream.write((data.length >> 8) & 0xff);
        byteStream.write(data.length & 0xff);
        byteStream.write(data);
    }

    @Override
    public String toString() {
        return "DNSRecord{" +
                "domain=" + Arrays.toString(domain) +
                ", type=" + type +
                ", DNSClass=" + DNSClass +
                ", lifespan=" + lifespan +
                ", today=" + today +
                '}';
    }

    boolean isExpired() {
        return System.currentTimeMillis() > (today.getTime() + lifespan * 1000L);
    }
}
