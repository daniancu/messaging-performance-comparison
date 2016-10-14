package cloud.orbit.messaging.test.app;

import cloud.orbit.messaging.test.api.Receiver;

/**
 *
 */
public class PerformanceMonitor extends Thread {

    private Receiver server;

    public PerformanceMonitor(Receiver server) {
        this.server = server;
    }

    @Override
    public void run() {
        System.out.println("Starting performance monitor...");
        try {
            long oldCounter = server.getTransferredBytes();
            long startTime = System.currentTimeMillis();
            long oldMessages = server.getMessageCount();

            for (;;) {
                Thread.sleep(3000);

                long endTime = System.currentTimeMillis();
                long newCounter = server.getTransferredBytes();
                long newMessages = server.getMessageCount();

                System.err.format("%d clients, %d messages/sec, %4.3f MB/s%n",
                        server.getActiveClients(),
                        ((newMessages - oldMessages) * 1000) / (endTime - startTime),
                        (newCounter - oldCounter) * 1000 / (endTime - startTime) / 1048576.0);
                oldCounter = newCounter;
                startTime = endTime;
                oldMessages = newMessages;
            }
        } catch (InterruptedException e) {
            // Stop monitoring asked
            return;
        }
    }
}
