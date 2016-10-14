package cloud.orbit.messaging.test.netty;

import cloud.orbit.messaging.test.api.Receiver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.util.concurrent.Executors;

import org.jvnet.hk2.annotations.Service;

/**
 * Echoes back any received data from a client.
 */
@Service(name = "NettyReceiver")
public final class NettyServer implements Receiver {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    private final ServerHandler serverHandler = new ServerHandler();
    private Channel channel;
    // Start server with Nb of active threads = 2*NB CPU + 1 as maximum.
    private EventLoopGroup bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2 + 1, Executors.newCachedThreadPool());
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

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
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }

                            p.addLast("frameDecoder",
                                    new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                            p.addLast("bytesDecoder", new ByteArrayDecoder());
                            p.addLast(serverHandler);
                        }
                    });

            // Start the server.
            System.out.println("binding on port " + PORT + "...");

            System.out.println("Netty server is up");
            // Wait until the server socket is closed.
            channel = b.bind(PORT).sync().channel();
            // Wait until the server socket is closed
            channel.closeFuture().sync();
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

    @Override
    public long getMessageCount() {
        return serverHandler.getMessageCount();
    }

    @Override
    public long getTransferredBytes() {
        return serverHandler.getReceivedBytes();
    }

    @Override
    public void stop() {
        channel.close();
        // Shut down all event loops to terminate all threads.
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public long getActiveClients() {
        return serverHandler.getActiveClients();
    }
}
