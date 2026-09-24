package org.web3j.protocol.websocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A local WebSocket endpoint that sends back the messages its handler returns for each message it receives, or
 * drops the connection when the handler returns null.
 */
final class WebSocketTestServer extends WebSocketServer implements AutoCloseable {

    private final Function<String, List<String>> repliesForMessage;

    private final CountDownLatch started = new CountDownLatch(1);

    WebSocketTestServer(Function<String, List<String>> repliesForMessage) throws InterruptedException {
        super(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        this.repliesForMessage = repliesForMessage;
        setReuseAddr(true);
        start();
        if (!this.started.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("WebSocket test server did not start");
        }
    }

    String url() {
        return "ws://127.0.0.1:" + getPort() + "/";
    }

    @Override
    public void onStart() {
        this.started.countDown();
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake) {}

    @Override
    public void onMessage(WebSocket connection, String message) {
        List<String> replies = this.repliesForMessage.apply(message);
        if (replies == null) {
            connection.close();
        } else {
            replies.forEach(connection::send);
        }
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {}

    @Override
    public void onError(WebSocket connection, Exception e) {}

    @Override
    public void close() throws InterruptedException {
        stop(1000);
    }
}
