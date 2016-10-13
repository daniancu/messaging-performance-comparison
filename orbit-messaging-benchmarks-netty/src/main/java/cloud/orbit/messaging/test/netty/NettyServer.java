package cloud.orbit.messaging.test.netty;
import cloud.orbit.messaging.test.api.Receiver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.jvnet.hk2.annotations.Service;

/**
 * Echoes back any received data from a client.
 */
@Service (name ="NettyReceiver")
public final class NettyServer implements Receiver {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    private final ServerHandler serverHandler = new ServerHandler();

    public void setup(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast("bytesDecoder", new ByteArrayDecoder());
//                            p.addLast("frameDecoder",
//                                    new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
//                            p.addLast("frameEncoder", new LengthFieldPrepender(4));
                            p.addLast("bytesEncoder", new ByteArrayEncoder());
                            p.addLast(serverHandler);
                        }
                    });

            // Start the server.
            System.out.println("binding on port " + PORT + "...");
            ChannelFuture f = b.bind(PORT).sync();

            System.out.println("Netty server is up");
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
            System.out.println("closed");
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void start(String[] args) {
        System.out.println("Starting Netty server...");
        try {
            setup(args);
        } catch (Exception e) {
            throw new RuntimeException("could not start Netty server", e);
        }
    }
}
