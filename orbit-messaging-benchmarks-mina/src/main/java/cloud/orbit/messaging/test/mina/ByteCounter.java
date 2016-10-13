package cloud.orbit.messaging.test.mina;

import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 */
public class ByteCounter {

    private long bytes = 0;
    private int counts;

    public void count(IoBuffer bfr) {
        bytes += (bfr.limit() - bfr.position());
        counts++;
    }

    public long getBytes() {
        return bytes;
    }

    public int getCounts() {
        return counts;
    }

    @Override
    public String toString() {
        return "bytes=" + bytes + ", counts=" + counts;
    }
}
