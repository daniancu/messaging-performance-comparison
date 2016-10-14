package cloud.orbit.messaging.test.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author Johno Crawford (johno@sulake.com)
 */
public class ServerDecoder extends LengthFieldBasedFrameDecoder {

    public static final int MAX_BODY_SIZE = 65536;

    public ServerDecoder() {
        super(MAX_BODY_SIZE, 0, Integer.BYTES, 0, Integer.BYTES);
    }

    @Override
    protected Object decode(ChannelHandlerContext context, ByteBuf in) throws Exception {
        ByteBuf decoded = (ByteBuf) super.decode(context, in);
        if (decoded == null) {
            return null;
        }
        try {
            byte[] array = new byte[in.readableBytes()];
            in.getBytes(0, array);
            return array;
        } finally {
            in.release();
        }
    }
}
