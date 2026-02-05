import java.util.HashMap;

public class DNSCache {
    HashMap<DNSQuestion, DNSRecord> cachedAnswer;

    DNSCache() {
        cachedAnswer = new HashMap<>();
    }

    DNSRecord getCachedAnswer(DNSQuestion question) {
        DNSRecord answer = cachedAnswer.get(question);
        if (answer == null || answer.isExpired()) {
            cachedAnswer.remove(question);
            return null;
        } else {
            return answer;
        }
    }

    void insertQuestion(DNSQuestion question, DNSRecord answer) {
        cachedAnswer.put(question, answer);
    }
}
