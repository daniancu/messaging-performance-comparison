**HOW To build and run specific implementation**

In root folder

`mvn clean install -P[netty|mina|grizzly]`

**How to start the server**

`java -cp orbit-messaging-benchmarks-test/target/orbit-messaging-benchmarks-test-1.0-SNAPSHOT-[netty|mina|grizzly].jar cloud.orbit.messaging.test.app.Server`


**How to run the client**

`java -cp orbit-messaging-benchmarks-test/target/orbit-messaging-benchmarks-test-1.0-SNAPSHOT-[netty|mina|grizzly].jar cloud.orbit.messaging.test.app.Server [hostname]`


