import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new DNSServer().run();
        } catch (IOException e) {
            System.err.println("General I/O Error has Occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
