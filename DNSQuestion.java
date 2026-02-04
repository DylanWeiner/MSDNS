import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {
    String[] domain;
    int type;
    int DNSClass;

    DNSQuestion(String[] domain, int type, int DNSClass) {
        this.domain = domain;
        this.type = type;
        this.DNSClass = DNSClass;
    }

    static DNSQuestion decodeQuestion(InputStream inpStr, DNSMessage msg) throws IOException {
        String[] domainName = msg.readDomainName(inpStr); // retrieves bytes for domain name.

        int decodeType = inpStr.read() << 8 | inpStr.read();
        int decodeClass = inpStr.read() << 8 | inpStr.read(); // Reads input string to retrieve the Question's type and class.

        return new DNSQuestion(domainName, decodeType, decodeClass);
    }

    void writeBytes(ByteArrayOutputStream byteStream, HashMap<String,Integer> domainNameLocations) {
        DNSMessage.writeDomainName(byteStream, domainNameLocations, this.domain);

        byteStream.write((type >> 8) & 0xff); // Places input type into the byte stream.
        byteStream.write(type & 0xff);
        byteStream.write((DNSClass >> 8) & 0xff); // Places the input class into the byte stream.
        byteStream.write(DNSClass & 0xff);
    }

    @Override
    public String toString() {
        return "DNSQuestion{" +
                "domain=" + Arrays.toString(domain) +
                ", type=" + type +
                ", DNSClass=" + DNSClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DNSQuestion that = (DNSQuestion) o;
        return type == that.type && DNSClass == that.DNSClass && Objects.deepEquals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(domain), type, DNSClass);
    }
}