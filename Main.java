import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        int server_port = 5678;
        System.out.println("Listening at " + server_port);
        DatagramSocket socket = new DatagramSocket(server_port);
        byte[] buffer = new byte[512];
        DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
        RunDig("google.com");
        for (int count = 1; true; count++) {
            socket.receive(pkt); // <----- waits here
            System.out.println(count + " Heard from " + pkt.getAddress() + " " + pkt.getPort());
            for (int i = 0; i < pkt.getLength(); i++) {
                System.out.printf(" %x", (int)buffer[i] & 0xFF);
            }
            System.out.print("\n");
            RunDig("google.com");
        }
    }

    public static void RunDig(String domain) throws IOException {
        Process p;
        Runtime runtime = Runtime.getRuntime();
        p = runtime.exec(new String[]{"dig " + domain + " @127.0.0.1 -p 5678"});
    }
}

