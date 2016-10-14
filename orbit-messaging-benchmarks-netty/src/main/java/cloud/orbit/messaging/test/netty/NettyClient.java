package cloud.orbit.messaging.test.netty;

import cloud.orbit.messaging.test.api.Sender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Scope;
import javax.inject.Singleton;

@Service(name = "NettySender")
public final class NettyClient implements Sender {

    static final boolean SSL = false;

    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
//    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    private EventLoopGroup group;
    private Channel channel;
    private ClientHandler clientHandler = new ClientHandler();

    public void setup(String host) throws Exception {
        // Configure SSL.git
        final SslContext sslCtx;
        if (SSL) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        // Configure the client.
        group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, PORT));
                        }
                        p.addLast("frameEncoder", new LengthFieldPrepender(4));
                        p.addLast(clientHandler);
                    }
                });

        System.out.println(String.format("connecting to Netty server on %s, port %d ...", host, PORT));
        channel= b.connect(host, PORT).sync().channel();

        System.out.println("connected to Netty server!");
    }

    @Override
    public void send(byte[] message) {
        try {
            channel.writeAndFlush(channel.alloc().buffer(message.length).writeBytes(message)).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("send error", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() throws InterruptedException {
        System.out.println("closing connection...");
        try {

            channel.close().sync();
        } finally {
            System.out.println("Shut down the event loop to terminate all threads");
            group.shutdownGracefully();
        }
        System.out.println("Closed");
    }

    @Override
    public void connect(String host) {
        try {
            setup(host);
        } catch (Exception e) {
            throw new RuntimeException("could not connect to Netty server", e);
        }
    }
}