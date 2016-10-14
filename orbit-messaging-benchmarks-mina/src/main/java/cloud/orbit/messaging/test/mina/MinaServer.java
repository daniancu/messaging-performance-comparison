package cloud.orbit.messaging.test.mina;


import cloud.orbit.messaging.test.api.Receiver;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An TCP server used for performance tests.
 *
 * It does nothing fancy, except receiving the messages, and counting the number of
 * received messages.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
@Service(name = "MinaReceiver")
public class MinaServer extends IoHandlerAdapter implements Receiver {
    /** The listening port (check that it's not already in use) */
    public static final int PORT = 18567;

    public static final String HOST = "localhost";

    private ByteCounter byteCounter = new ByteCounter();


    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        session.closeNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        IoBuffer ioBuffer = (IoBuffer) message;
        byteCounter.count(ioBuffer);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("Session closed...");

        // Reinitialize the counter and expose the number of received messages
        System.out.println("server stats : " + byteCounter);

    }


    @Override
    public void start(String[] args) {
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(this);

        // The logger, if needed. Commented atm
        //DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        //chain.addLast("logger", new LoggingFilter());

        try {
            System.out.println(String.format("starting Mina server on port %d ...", PORT));
            acceptor.bind(new InetSocketAddress(PORT));
        } catch (IOException e) {
            throw new RuntimeException("could not start Mina server");
        }

        System.out.println("Mina server started");
    }

    @Override
    public long getMessageCount() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public long getTransferredBytes() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public long getActiveClients() {
        throw new UnsupportedOperationException("not implemented");
    }
}