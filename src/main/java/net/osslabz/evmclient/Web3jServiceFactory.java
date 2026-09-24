package net.osslabz.evmclient;

import java.net.ConnectException;
import java.time.Duration;
import java.util.Collections;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.LongLivingWebSocketService;

final class Web3jServiceFactory {

    // HTTP traces log under EvmClient, the logger users enable them on.
    private static final Logger log = LoggerFactory.getLogger(EvmClient.class);

    private Web3jServiceFactory() {}

    static Web3jService create(String rpcUrlString) {
        try {
            if (rpcUrlString == null) {
                throw new EvmClientException("Can't instantiate Web3jService because the provided RPC URL is null.");
            }

            int protocolEndIndex = rpcUrlString.indexOf("://");

            if (protocolEndIndex <= 1) {
                throw new EvmClientException("Can't instantiate Web3jService because the provided RPC URL '"
                        + rpcUrlString + "' is invalid.");
            }

            String protocol = rpcUrlString.substring(0, protocolEndIndex);

            switch (protocol) {
                case "ws", "wss" -> {
                    LongLivingWebSocketService webSocketService = new LongLivingWebSocketService(rpcUrlString, false);
                    webSocketService.connect();
                    return webSocketService;
                }
                case "http", "https" -> {
                    return new HttpService(rpcUrlString, createHttpClientWithCookieSupport());
                }
                default ->
                    throw new EvmClientException(
                            "Unknown protocol '" + protocol + "' in provided RPC URL '" + rpcUrlString + "'.");
            }
        } catch (ConnectException e) {

            throw new EvmClientException(e);
        }
    }

    private static OkHttpClient createHttpClientWithCookieSupport() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.cookieJar(new InMemoryCookieJar());
        builder.connectTimeout(Duration.ofSeconds(30));
        builder.readTimeout(Duration.ofSeconds(60));
        builder.writeTimeout(Duration.ofSeconds(15));
        builder.pingInterval(Duration.ofSeconds(15));
        builder.protocols(Collections.singletonList(Protocol.HTTP_1_1));
        builder.retryOnConnectionFailure(true);

        if (log.isTraceEnabled()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::trace);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }

        return builder.build();
    }
}
