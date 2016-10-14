package cloud.orbit.messaging.test.netty;

import cloud.orbit.messaging.test.api.Receiver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.childOption(ChannelOption.MAX_MESSAGES_PER_READ, 16);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);
            b.childOption(ChannelOption.TCP_NODELAY, true);
            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //b.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(200, 128 * 1024, 512 * 1024));
            //bootstrap.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
            //bootstrap.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            p.addLast("serverDecoder", new ServerDecoder());
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
        boolean interrupted = false;
        try {
            try {
                channel.close().sync();
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS);
            workerGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public long getActiveClients() {
        return serverHandler.getActiveClients();
    }
}
