import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class DNSServer {
    private static final String GOOGLE_DNS = "8.8.8.8";
    private static final int GOOGLE_PORT = 53;

    public void run() throws IOException {
        int CLIENT_PORT = 8053;
        System.out.println("Listening at " + CLIENT_PORT);
        DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
        byte[] buffer = new byte[512];
        DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
        for (int count = 1; true; count++) {
            socket.receive(pkt); // <----- waits here
            InetAddress requestAddress = pkt.getAddress();
            int requestPort = pkt.getPort();

            DNSMessage dnsRequest = DNSMessage.decodeMessage(buffer);

            InetAddress svar_host = InetAddress.getByName(GOOGLE_DNS);
            pkt.setAddress(svar_host);
            pkt.setPort(GOOGLE_PORT);

            socket.send(pkt);

            socket.receive(pkt);
            DNSMessage dnsResponse = DNSMessage.decodeMessage(buffer);

            //if(DNS r Code is R_NoError) cache it.
            DNSHeader requestHeader = dnsRequest.getHeaderForResponse();
            DNSHeader responseHeader = dnsResponse.getHeaderForResponse();
            if(requestHeader.getrCode() == responseHeader.getrCode()) {
                pkt.setAddress(requestAddress);
                pkt.setPort(requestPort);

                socket.send(pkt);
            }
        }
    }
}