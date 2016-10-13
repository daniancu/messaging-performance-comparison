package cloud.orbit.messaging.test.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private long receivedBytes = 0;
    private int messages = 0;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        byte[] buffer = (byte[]) msg;
        receivedBytes += buffer.length;
        messages ++;
//        System.out.println("buffer = " + new String(buffer));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("receivedBytes = " + receivedBytes +  ", messages =  " + messages);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.receivedBytes = 0;
        this.messages = 0;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }
}