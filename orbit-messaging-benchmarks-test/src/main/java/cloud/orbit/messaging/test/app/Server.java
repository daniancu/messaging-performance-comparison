package cloud.orbit.messaging.test.app;

import cloud.orbit.messaging.test.api.Receiver;
import cloud.orbit.messaging.test.api.Sender;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

/**
 *
 */
public class Server {

    @Inject
    Receiver receiver;

    public static void main(String[] args) {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();

        Receiver receiver = locator.getService(Receiver.class);
        System.out.println("receiver = " + receiver);

        Server server = locator.create(Server.class);
        locator.inject(server);

        receiver.start(args);

    }
}
