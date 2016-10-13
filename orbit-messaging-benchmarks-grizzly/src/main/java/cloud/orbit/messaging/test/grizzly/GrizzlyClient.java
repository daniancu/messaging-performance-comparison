package cloud.orbit.messaging.test.grizzly;

import cloud.orbit.messaging.test.api.Sender;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.StringFilter;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * The simple client, which sends a message to the echo server
 * and waits for response
 */
@Service(name = "GrizzlySender")
public class GrizzlyClient implements Sender {
    private Connection connection;
    private TCPNIOTransport transport;

    @Override
    public void connect() {
        try {
            setup();
        } catch (Exception e) {
            throw new RuntimeException("grizzly connection failure", e);
        }
    }

    @Override
    public void send(byte[] bytes) {
        GrizzlyFuture result = connection.write(new ByteBufferWrapper(ByteBuffer.wrap(bytes)));
    }

    @Override
    public void disconnect() {

        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setup() throws IOException,
            ExecutionException, InterruptedException, TimeoutException {
        System.out.println("Connecting to Grizzly server...");
        connection = null;

        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        // Add TransportFilter, which is responsible
        // for reading and writing data to the connection
        filterChainBuilder.add(new TransportFilter());

        // StringFilter is responsible for Buffer <-> String conversion
//        filterChainBuilder.add(new StringFilter(Charset.forName("UTF-8")));

        // ClientFilter is responsible for redirecting server responses to the standard output
//        filterChainBuilder.add(new ClientFilter());

        // Create TCP transport
        transport = TCPNIOTransportBuilder.newInstance().build();
        transport.setProcessor(filterChainBuilder.build());


        // start the transport
        transport.start();

        // perform async. connect to the server
        Future<Connection> future = transport.connect(GrizzlyServer.HOST, GrizzlyServer.PORT);
        // wait for connect operation to complete
        connection = future.get(3, TimeUnit.SECONDS);

        assert connection != null;

        System.out.println("connected to grizzly server");

    }

    public void close() throws IOException {

        // close the client connection
        if (connection != null) {
            connection.close();
        }

        // stop the transport
        transport.shutdownNow();
    }


}

