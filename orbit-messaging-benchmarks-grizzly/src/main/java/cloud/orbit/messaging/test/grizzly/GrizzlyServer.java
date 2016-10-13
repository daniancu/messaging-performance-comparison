package cloud.orbit.messaging.test.grizzly;

import java.io.IOException;
import java.nio.charset.Charset;

import cloud.orbit.messaging.test.api.Receiver;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.StringFilter;
import org.jvnet.hk2.annotations.Service;

/**
 * Class initializes and starts the echo server, based on Grizzly 2.3
 */
@Service(name = "GrizzlyReceiver")
public class GrizzlyServer implements Receiver{


    public static final String HOST = "localhost";
    public static final int PORT = 7777;
    private final ServerFilter filter = new ServerFilter();

    public void start(String[] args) {
        try {
            startGrizzly(args);
        } catch (IOException e) {
            e.printStackTrace();throw new RuntimeException("could not start grizzly server", e);
        }
    }

    public void startGrizzly(String[] args) throws IOException {
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

        // Add TransportFilter, which is responsible
        // for reading and writing data to the connection
        filterChainBuilder.add(new TransportFilter());

//         StringFilter is responsible for Buffer <-> String conversion
//        filterChainBuilder.add(new StringFilter(Charset.forName("UTF-8")));

        // ServerFilter is responsible for echoing received messages
        filterChainBuilder.add(filter);

        // Create TCP transport
        final TCPNIOTransport transport =
                TCPNIOTransportBuilder.newInstance().build();

        transport.setProcessor(filterChainBuilder.build());
        try {
            // binding transport to start listen on certain host and port
            transport.bind(HOST, PORT);

            // start the transport
            transport.start();

            System.out.println("Press any key to stop the server...");
            System.in.read();

            System.out.println("Received " + filter.getReceivedBytes() + " bytes");
        } finally {
            System.out.println("Stopping transport...");
            // stop the transport
            transport.shutdownNow();

            System.out.println("Transport stopped");
        }
    }
}

