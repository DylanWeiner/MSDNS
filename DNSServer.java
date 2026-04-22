import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
public class DNSServer {
    private static final String GOOGLE_DNS = "8.8.8.8";
    private static final int GOOGLE_PORT = 53;
    private DNSCache cache;

    public void run() throws IOException {
        cache = new DNSCache();
        int CLIENT_PORT = 8053;
        System.out.println("Listening at " + CLIENT_PORT);
        DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
        
        for (int count = 1; true; count++) {
            try {
                byte[] buffer = new byte[512];
                DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
                socket.receive(pkt);
                
                InetAddress requestAddress = pkt.getAddress();
                int requestPort = pkt.getPort();
                int requestLength = pkt.getLength();
                
                System.out.println("\n[Request #" + count + "] From " + requestAddress + ":" + requestPort);

                // Make a copy of the request data
                byte[] requestData = new byte[requestLength];
                System.arraycopy(buffer, 0, requestData, 0, requestLength);
                
                DNSMessage dnsRequest = DNSMessage.decodeMessage(requestData);
                System.out.println("  Questions: " + dnsRequest.questions.length);
                
                DNSRecord[] answers = new DNSRecord[dnsRequest.questions.length];
                boolean needsForward = false;
                
                // Check cache for each question
                for (int i = 0; i < dnsRequest.questions.length; i++) {
                    DNSQuestion question = dnsRequest.questions[i];
                    DNSRecord cachedRecord = cache.getCachedAnswer(question);
                    if (cachedRecord != null) {
                        System.out.println("  ✓ Cache HIT");
                        answers[i] = cachedRecord;
                    } else {
                        System.out.println("  ✗ Cache MISS");
                        needsForward = true;
                    }
                }
                
                // Forward cache misses to Google
                if (needsForward) {
                    System.out.println("  Forwarding to Google DNS...");
                    
                    DatagramSocket googleSocket = new DatagramSocket();
                    InetAddress googleAddr = InetAddress.getByName(GOOGLE_DNS);
                    DatagramPacket googlePkt = new DatagramPacket(requestData, requestLength, googleAddr, GOOGLE_PORT);
                    googleSocket.send(googlePkt);
                    
                    // Receive response from Google
                    byte[] googleResponseBuffer = new byte[512];
                    DatagramPacket googleResponse = new DatagramPacket(googleResponseBuffer, googleResponseBuffer.length);
                    googleSocket.receive(googleResponse);
                    googleSocket.close();
                    
                    System.out.println("  Received response from Google");
                    
                    DNSMessage googleResponseMsg = DNSMessage.decodeMessage(googleResponseBuffer);
                    
                    // Cache successful responses (rCode 0 = no error)
                    if (googleResponseMsg.getHeaderForResponse().getrCode() == 0) {
                        for (int i = 0; i < dnsRequest.questions.length && i < googleResponseMsg.answerRecords.length; i++) {
                            if (answers[i] == null && googleResponseMsg.answerRecords[i] != null) {
                                cache.insertQuestion(dnsRequest.questions[i], googleResponseMsg.answerRecords[i]);
                                System.out.println("  Cached answer");
                                answers[i] = googleResponseMsg.answerRecords[i];
                            }
                        }
                    }
                }
                
                // Filter out null answers
                java.util.List<DNSRecord> answerList = new java.util.ArrayList<>();
                for (DNSRecord rec : answers) {
                    if (rec != null) {
                        answerList.add(rec);
                    }
                }
                DNSRecord[] finalAnswers = answerList.toArray(new DNSRecord[0]);
                
                // Build and send response
                DNSMessage responseMessage = DNSMessage.buildResponse(dnsRequest, finalAnswers);
                System.out.println("  Response questions: " + responseMessage.questions.length);
                if (responseMessage.questions.length > 0) {
                    System.out.println("    Q[0] domain: " + Arrays.toString(responseMessage.questions[0].domain));
                }
                System.out.println("  Response answers: " + responseMessage.answerRecords.length);
                byte[] responseBytes = responseMessage.toBytes();
                System.out.println("  Sending response (" + responseBytes.length + " bytes)");
                
                DatagramPacket responsePkt = new DatagramPacket(responseBytes, responseBytes.length, requestAddress, requestPort);
                socket.send(responsePkt);
                
            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}