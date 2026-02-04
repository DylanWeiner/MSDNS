import static org.junit.jupiter.api.Assertions.*;

class DNSHeaderTest {

    byte exampleDotComDNSRequest[] = {
        (byte)0xc8, (byte)0x4a, (byte)0x1, (byte)0x20, (byte)0x0, (byte)0x1, (byte)0x0, (byte)0x0,
        (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x1, (byte)0x7, (byte)0x65, (byte)0x78, (byte)0x61,
        (byte)0x6d, (byte)0x70, (byte)0x6c, (byte)0x65, (byte)0x3, (byte)0x63, (byte)0x6f, (byte)0x6d,
        (byte)0x0, (byte)0x0, (byte)0x1, (byte)0x0, (byte)0x1, (byte)0x0, (byte)0x0, (byte)0x29,
        (byte)0x10, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0};

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.Test
    void decodeHeader() {
    }

    @org.junit.jupiter.api.Test
    void buildHeaderForResponse() {
    }

    @org.junit.jupiter.api.Test
    void writeBytes() {
    }

    @org.junit.jupiter.api.Test
    void testToString() {
    }
}