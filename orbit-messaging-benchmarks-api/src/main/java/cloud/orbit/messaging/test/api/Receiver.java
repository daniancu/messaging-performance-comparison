package cloud.orbit.messaging.test.api;


import org.jvnet.hk2.annotations.Contract;

/**
 * Defines a source that receives a messages from an other node
 */
@Contract
public interface Receiver {

    void start(String[] args);


}
