package cloud.orbit.messaging.test.grizzly;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.HeapBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Implementation of {@link FilterChain} filter, which replies with the request
 * message.
 */
public class ServerFilter extends BaseFilter {

    private long receivedBytes = 0;
    private int msgs = 0;

    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {
        System.out.println(String.format("got %d bytes in %d reads", receivedBytes, msgs));
        return ctx.getStopAction();
    }

    /**
     * Handle just read operation, when some message has come and ready to be
     * processed.
     *
     * @param ctx Context of {@link FilterChainContext} processing
     * @return the next action
     * @throws java.io.IOException
     */
    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        final Object message = ctx.getMessage();

        //count received bytes
        Buffer buffer = (Buffer) message;
        int bytes = buffer.limit() - buffer.position();

//        String msg = buffer.toStringContent();
//        System.out.println("msg = " + msg);

        receivedBytes += bytes;
        msgs++;
//        System.out.println("receivedBytes = " + receivedBytes);
        return ctx.getStopAction();
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }
}
