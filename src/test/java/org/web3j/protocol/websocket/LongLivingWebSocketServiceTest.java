package org.web3j.protocol.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.reactivex.Flowable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.BatchResponse;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthSubscribe;
import org.web3j.protocol.websocket.events.NewHeadsNotification;

@Timeout(30)
public class LongLivingWebSocketServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SUBSCRIPTION_ID = "0xcd0c3e8af590364c09d0fa6a1210faf5";

    private WebSocketTestServer openServer;

    private LongLivingWebSocketService openService;

    @Test
    public void testSendReturnsTheReplyToTheRequest() throws Exception {
        LongLivingWebSocketService service = connectTo(new WebSocketTestServer(reply(request -> "0x2a")));

        EthBlockNumber blockNumber = service.send(blockNumberRequest(service), EthBlockNumber.class);

        Assertions.assertEquals(BigInteger.valueOf(42), blockNumber.getBlockNumber());
    }

    @Test
    public void testSendFailsWhenTheConnectionIsDropped() throws Exception {
        LongLivingWebSocketService service = connectTo(new WebSocketTestServer(message -> null));

        IOException exception = Assertions.assertThrows(
                IOException.class, () -> service.send(blockNumberRequest(service), EthBlockNumber.class));

        Assertions.assertEquals("Connection was closed", exception.getMessage());
    }

    @Test
    public void testSendKeepsTheInterruptFlagWhenInterrupted() throws Exception {
        LongLivingWebSocketService service = connectTo(new WebSocketTestServer(message -> List.of()));
        Request<?, EthBlockNumber> request = blockNumberRequest(service);

        Thread.currentThread().interrupt();
        Assertions.assertThrows(IOException.class, () -> service.send(request, EthBlockNumber.class));

        Assertions.assertTrue(Thread.interrupted());
    }

    @Test
    public void testSendBatchReturnsTheRepliesInRequestOrder() throws Exception {
        LongLivingWebSocketService service =
                connectTo(new WebSocketTestServer(reply(request -> "0x" + request.get("id"))));
        Request<?, EthBlockNumber> first = blockNumberRequest(service);
        first.setId(7);
        Request<?, EthBlockNumber> second = blockNumberRequest(service);
        second.setId(9);

        BatchResponse response =
                service.sendBatch(new BatchRequest(service).add(first).add(second));

        Assertions.assertEquals(2, response.getResponses().size());
        Assertions.assertEquals(7, response.getResponses().get(0).getId());
        Assertions.assertEquals(
                BigInteger.valueOf(9), ((EthBlockNumber) response.getResponses().get(1)).getBlockNumber());
    }

    @Test
    public void testSendBatchKeepsTheInterruptFlagWhenInterrupted() throws Exception {
        LongLivingWebSocketService service = connectTo(new WebSocketTestServer(message -> List.of()));
        BatchRequest batch = new BatchRequest(service).add(blockNumberRequest(service));

        Thread.currentThread().interrupt();
        Assertions.assertThrows(IOException.class, () -> service.sendBatch(batch));

        Assertions.assertTrue(Thread.interrupted());
    }

    @Test
    public void testSubscribeDeliversTheNotificationsOfTheSubscription() throws Exception {
        LongLivingWebSocketService service = connectTo(new WebSocketTestServer(message -> List.of(
                "{\"jsonrpc\":\"2.0\",\"id\":" + parse(message).get("id") + ",\"result\":\"" + SUBSCRIPTION_ID + "\"}",
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_subscription\",\"params\":{\"subscription\":\"" + SUBSCRIPTION_ID
                        + "\",\"result\":{\"number\":\"0x1b4\"}}}")));

        Flowable<NewHeadsNotification> notifications =
                service.subscribe(newHeadsRequest(service), "eth_unsubscribe", NewHeadsNotification.class);

        Assertions.assertEquals(
                "0x1b4", notifications.blockingFirst().getParams().getResult().getNumber());
    }

    @Test
    public void testSubscribeReportsARejectedSubscription() throws Exception {
        LongLivingWebSocketService service =
                connectTo(new WebSocketTestServer(message -> List.of("{\"jsonrpc\":\"2.0\",\"id\":"
                        + parse(message).get("id") + ",\"error\":{\"code\":-32601,\"message\":\"no newHeads\"}}")));

        Flowable<NewHeadsNotification> notifications =
                service.subscribe(newHeadsRequest(service), "eth_unsubscribe", NewHeadsNotification.class);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, notifications::blockingFirst);
        Assertions.assertEquals(
                "Subscription request failed with error: no newHeads",
                exception.getCause().getMessage());
    }

    @Test
    public void testConnectFailsWhenNoServerListens() throws Exception {
        String url;
        try (WebSocketTestServer server = new WebSocketTestServer(message -> List.of())) {
            url = server.url();
        }
        this.openService = new LongLivingWebSocketService(url, false);

        Assertions.assertThrows(ConnectException.class, this.openService::connect);
    }

    private LongLivingWebSocketService connectTo(WebSocketTestServer server) throws ConnectException {
        this.openServer = server;
        this.openService = new LongLivingWebSocketService(server.url(), false);
        this.openService.connect();
        return this.openService;
    }

    @AfterEach
    public void stopServiceAndServer() throws InterruptedException {
        if (this.openService != null) {
            this.openService.close();
        }
        if (this.openServer != null) {
            this.openServer.close();
        }
    }

    private static Request<?, EthBlockNumber> blockNumberRequest(LongLivingWebSocketService service) {
        return new Request<>("eth_blockNumber", Collections.<String>emptyList(), service, EthBlockNumber.class);
    }

    private static Request<?, EthSubscribe> newHeadsRequest(LongLivingWebSocketService service) {
        return new Request<>("eth_subscribe", List.of("newHeads"), service, EthSubscribe.class);
    }

    /** Answers a single request or a batch with one result per request, computed from the request. */
    private static Function<String, List<String>> reply(Function<JsonNode, String> resultForRequest) {
        return message -> {
            JsonNode requests = parse(message);
            if (!requests.isArray()) {
                return List.of(result(requests, resultForRequest).toString());
            }
            ArrayNode results = OBJECT_MAPPER.createArrayNode();
            requests.forEach(request -> results.add(result(request, resultForRequest)));
            return List.of(results.toString());
        };
    }

    private static ObjectNode result(JsonNode request, Function<JsonNode, String> resultForRequest) {
        ObjectNode response = OBJECT_MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", request.get("id"));
        response.put("result", resultForRequest.apply(request));
        return response;
    }

    private static JsonNode parse(String message) {
        try {
            return OBJECT_MAPPER.readTree(message);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
