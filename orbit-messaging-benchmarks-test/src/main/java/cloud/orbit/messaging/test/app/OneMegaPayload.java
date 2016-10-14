package cloud.orbit.messaging.test.app;

import cloud.orbit.messaging.test.api.PayloadGenerator;
import org.jvnet.hk2.annotations.Service;

import java.nio.charset.Charset;

/**
 *
 */
@Service (name = "oneMegaPayload")
public class OneMegaPayload implements PayloadGenerator {

    private static final byte[] payload = "simple payload data".getBytes(Charset.forName("UTF-8"));
    private long generated = 0;
    private final long oneMB = 10 * 1024 * 1024;

    @Override
    public boolean hasNext() {
        return generated < oneMB;
    }

    @Override
    public byte[] next() {
        generated += payload.length;
        return payload;
    }
}
