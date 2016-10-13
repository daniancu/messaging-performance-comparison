package cloud.orbit.messaging.test.api;

/**
 * Defines a client that messages to an other node
 */

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface Sender {

    void send(byte[] message);

    void disconnect();

    void connect();
}
