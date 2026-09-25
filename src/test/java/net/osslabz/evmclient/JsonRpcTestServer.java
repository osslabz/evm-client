package net.osslabz.evmclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Function;

/**
 * A local JSON-RPC endpoint that answers every call with the result its handler returns for the request.
 */
final class JsonRpcTestServer implements AutoCloseable {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpServer server;

    JsonRpcTestServer(Function<JsonNode, String> resultForRequest) throws IOException {
        this((HttpHandler) exchange -> respond(exchange, resultForRequest));
    }

    private JsonRpcTestServer(HttpHandler handler) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        this.server.createContext("/", handler);
        this.server.start();
    }

    /** A server that answers every request with the given HTTP status and an empty body. */
    static JsonRpcTestServer failingWith(int httpStatus) throws IOException {
        return new JsonRpcTestServer(exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(httpStatus, -1);
            exchange.close();
        });
    }

    String url() {
        return "http://127.0.0.1:" + this.server.getAddress().getPort() + "/";
    }

    private static void respond(HttpExchange exchange, Function<JsonNode, String> resultForRequest) throws IOException {
        JsonNode request;
        try (InputStream body = exchange.getRequestBody()) {
            request = OBJECT_MAPPER.readTree(body);
        }

        ObjectNode response = OBJECT_MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", request.get("id"));
        response.put("result", resultForRequest.apply(request));
        byte[] responseBody = OBJECT_MAPPER.writeValueAsBytes(response);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBody.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(responseBody);
        }
    }

    @Override
    public void close() {
        this.server.stop(0);
    }
}
