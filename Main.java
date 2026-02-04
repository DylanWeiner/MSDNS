import java.io.IOException;

public class Main {
    void main() {
        try {
            new DNSServer().run();
        } catch (IOException e) {
            System.err.println("General I/O Error has Occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
