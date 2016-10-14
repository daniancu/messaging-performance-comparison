package cloud.orbit.messaging.test.mina;

import cloud.orbit.messaging.test.api.Sender;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.jvnet.hk2.annotations.Service;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * A simple TCP client, write back to the client every received messages.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */

/**
 * An UDP client taht just send thousands of small messages to a UdpServer.
 *
 * This class is used for performance test purposes. It does nothing at all, but send a message
 * repetitly to a server.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
@Service (name="MinaSender")
public class MinaClient extends IoHandlerAdapter implements Sender {
    /** The connector */
    private IoConnector connector;

    /** The session */
    private static IoSession session;

    /** The buffer containing the message to send */
    private IoBuffer buffer = IoBuffer.allocate(8);

    /** Timers **/
    private long t0;
    private long t1;

    /** the counter used for the sent messages */
    private CountDownLatch counter;

    private ByteCounter byteCounter = new ByteCounter();

    /**
     * Create the UdpClient's instance
     */
    public MinaClient() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }


    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        byteCounter.count((IoBuffer) message);
    }

    @Override
    public void send(byte[] message) {
        try {
            session.write(IoBuffer.wrap(message)).await();
        } catch (InterruptedException e) {
            throw new RuntimeException("send error", e);
        }

    }

    @Override
    public void disconnect() {
        connector.dispose(true);
        System.out.println("disconnected from Mina server ( " + byteCounter + ")");
    }

    @Override
    public void connect(String host) {
        connector = new NioSocketConnector();
        connector.setHandler(this);
        System.out.println(String.format("connecting to Mina server at %s on port %d", host, MinaServer.PORT));
        ConnectFuture connFuture = connector.connect(new InetSocketAddress(host, MinaServer.PORT));

        connFuture.awaitUninterruptibly();

        session = connFuture.getSession();
        System.out.println("connected to Mina server");
    }
}