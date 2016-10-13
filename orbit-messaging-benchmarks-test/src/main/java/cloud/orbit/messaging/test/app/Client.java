package cloud.orbit.messaging.test.app;

import cloud.orbit.messaging.test.api.PayloadGenerator;
import cloud.orbit.messaging.test.api.Sender;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * A client that send data to server using an implementation of Sender
 */
public class Client {
    @Inject
    private Sender sender;

    @Inject
    PayloadGenerator payloadGenerator;

    public void stop() {
        sender.disconnect();
    }

    public void init() {
        sender.connect();
    }

    public void benchmark() {
        ClientStats stats = new ClientStats();
        byte[] next;
        while (payloadGenerator.hasNext()) {
            next = payloadGenerator.next();
            sender.send(next);
            stats.count(next);
        }
        stats.print();
    }


    public static void main(String[] args) {
        try {
            ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();

            Sender sender = locator.getService(Sender.class);
            System.out.println("sender = " + sender);

            PayloadGenerator payloadGenerator = locator.getService(PayloadGenerator.class);
            System.out.println("payloadGenerator = " + payloadGenerator);

            Client client = locator.create(Client.class);
            locator.inject(client);

            client.init();
            client.benchmark();

            Thread.sleep(5000);
            client.stop();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
