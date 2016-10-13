package cloud.orbit.messaging.test.app;

/**
 *
 */
public class ClientStats {
    long time;
    long bytes = 0;
    private int messages;

    public ClientStats() {
        this.time = System.currentTimeMillis();

    }

    public long elapsed() {
        return System.currentTimeMillis() - time;
    }

    public void count(byte[] next) {
        bytes += next.length;
        messages++;
    }

    public long getBytes() {
        return bytes;
    }

    public int getMessages() {
        return messages;
    }

    public void print() {
        System.out.println(String.format("ClientStats: sent %d messages, %d bytes  in %d milliseconds", messages, bytes, elapsed()));
    }
}
