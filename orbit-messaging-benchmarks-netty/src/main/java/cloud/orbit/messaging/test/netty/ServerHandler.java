package cloud.orbit.messaging.test.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicLong receivedBytes = new AtomicLong(0);
    private static final AtomicLong messages = new AtomicLong(0);
    private static final AtomicLong clients = new AtomicLong(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        byte[] buffer = (byte[]) msg;
        receivedBytes.addAndGet(buffer.length);
        messages.incrementAndGet();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client quit, total clients: " + clients.decrementAndGet());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("new client, total clients: " + clients.incrementAndGet());
    }

    public long getReceivedBytes() {
        return receivedBytes.get();
    }

    public long getMessageCount() {
        return messages.get();
    }

    public long getActiveClients() {
        return clients.get();
    }
}