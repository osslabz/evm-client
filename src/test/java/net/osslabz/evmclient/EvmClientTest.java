package net.osslabz.evmclient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.List;
import java.util.Locale;
import net.osslabz.evmclient.dto.Chain;
import net.osslabz.evmclient.dto.CoinBalance;
import net.osslabz.evmclient.dto.Erc20Token;
import net.osslabz.evmclient.dto.Erc20TokenBalance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.http.HttpService;

public class EvmClientTest {

    private static final String CONTRACT_ADDRESS = "0xB31F66AA3C1E785363F0875A1B74E27B85FD66C7";

    private static final String HOLDER_ADDRESS = "0x81c36bab8db9c25e6736427c13e183b881cb00bd";

    private static final BigInteger TOTAL_SUPPLY = new BigInteger("1000000000000000000000000");

    private static final BigInteger HOLDER_TOKEN_BALANCE = new BigInteger("2500000000000000000");

    private static final List<String> NO_TOKEN_INFO_WARNINGS = List.of(
            "Couldn't fetch name for contract address " + CONTRACT_ADDRESS + ".",
            "Couldn't fetch symbol for contract address " + CONTRACT_ADDRESS + ".",
            "Couldn't fetch decimals for contract address " + CONTRACT_ADDRESS + ".",
            "Couldn't fetch totalSupply for contract address " + CONTRACT_ADDRESS + ".");

    private final Logger clientLogger = (Logger) LoggerFactory.getLogger(EvmClient.class);

    private final ListAppender<ILoggingEvent> clientLog = new ListAppender<>();

    @BeforeEach
    void captureClientLog() {
        clientLog.start();
        clientLogger.addAppender(clientLog);
        clientLogger.setAdditive(false);
    }

    @AfterEach
    void releaseClientLog() {
        clientLogger.setAdditive(true);
        clientLogger.detachAppender(clientLog);
    }

    @Test
    public void testGetBalanceReturnsTheBalanceInTheChainsCoin() throws IOException {
        try (JsonRpcTestServer server = new JsonRpcTestServer(request -> "0xde0b6b3a7640000");
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            CoinBalance balance = evmClient.getBalance(HOLDER_ADDRESS);

            Assertions.assertEquals(new BigInteger("1000000000000000000"), balance.getBalance());
            Assertions.assertEquals("AVAX", balance.getCoin().getSymbol());
        }
    }

    @Test
    public void testGetBalanceWrapsAConnectionFailure() throws IOException {
        try (EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, unreachableUrl())) {

            EvmClientException exception =
                    Assertions.assertThrows(EvmClientException.class, () -> evmClient.getBalance(HOLDER_ADDRESS));

            Assertions.assertInstanceOf(ConnectException.class, exception.getCause());
        }
    }

    @Test
    public void testGetBalanceWrapsAnHttpErrorStatus() throws IOException {
        try (JsonRpcTestServer server = JsonRpcTestServer.failingWith(503);
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            EvmClientException exception =
                    Assertions.assertThrows(EvmClientException.class, () -> evmClient.getBalance(HOLDER_ADDRESS));

            Assertions.assertInstanceOf(ClientConnectionException.class, exception.getCause());
        }
    }

    @Test
    public void testGetLastBlockNumberReturnsTheNodesBlockNumber() throws IOException {
        try (JsonRpcTestServer server = new JsonRpcTestServer(request -> "0x2a");
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            Assertions.assertEquals(BigInteger.valueOf(42), evmClient.getLastBlockNumber());
        }
    }

    @Test
    public void testGetLastBlockNumberWrapsAConnectionFailure() throws IOException {
        try (EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, unreachableUrl())) {

            Assertions.assertThrows(EvmClientException.class, evmClient::getLastBlockNumber);
        }
    }

    @Test
    public void testGetLastBlockNumberWrapsAnHttpErrorStatus() throws IOException {
        try (JsonRpcTestServer server = JsonRpcTestServer.failingWith(503);
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            EvmClientException exception =
                    Assertions.assertThrows(EvmClientException.class, evmClient::getLastBlockNumber);

            Assertions.assertInstanceOf(ClientConnectionException.class, exception.getCause());
        }
    }

    @Test
    public void testGetTokenInfoReadsTheErc20Metadata() throws IOException {
        try (JsonRpcTestServer server = new JsonRpcTestServer(EvmClientTest::erc20Contract);
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            Erc20Token token = evmClient.getTokenInfo(CONTRACT_ADDRESS);

            Assertions.assertEquals("Wrapped AVAX", token.getName());
            Assertions.assertEquals("WAVAX", token.getSymbol());
            Assertions.assertEquals(BigInteger.valueOf(18), token.getDecimals());
            Assertions.assertEquals(TOTAL_SUPPLY, token.getTotalSupply());
            Assertions.assertEquals(CONTRACT_ADDRESS.toLowerCase(Locale.ROOT), token.getContractAddress());
            Assertions.assertEquals(Chain.AVALANCHE_MAIN, token.getChain());
        }
    }

    @Test
    public void testGetTokenInfoFailsForAnAddressWithoutAnErc20Contract() throws IOException {
        try (JsonRpcTestServer server = new JsonRpcTestServer(request -> "0x");
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            EvmClientException exception =
                    Assertions.assertThrows(EvmClientException.class, () -> evmClient.getTokenInfo(CONTRACT_ADDRESS));

            Assertions.assertTrue(exception.getMessage().startsWith("Couldn't fetch any token info"));
        }
        Assertions.assertEquals(NO_TOKEN_INFO_WARNINGS, clientWarnings());
    }

    @Test
    public void testGetTokenBalanceByContractAddressReadsTokenAndBalance() throws IOException {
        try (JsonRpcTestServer server = new JsonRpcTestServer(EvmClientTest::erc20Contract);
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            Erc20TokenBalance balance = evmClient.getTokenBalance(CONTRACT_ADDRESS, HOLDER_ADDRESS);

            Assertions.assertEquals(HOLDER_TOKEN_BALANCE, balance.getBalance());
            Assertions.assertEquals("WAVAX", balance.getToken().getSymbol());
        }
    }

    @Test
    public void testGetTokenBalanceByContractAddressFailsWithoutAnErc20Contract() throws IOException {
        try (JsonRpcTestServer server = new JsonRpcTestServer(request -> "0x");
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            Assertions.assertThrows(
                    EvmClientException.class, () -> evmClient.getTokenBalance(CONTRACT_ADDRESS, HOLDER_ADDRESS));
        }
        Assertions.assertEquals(NO_TOKEN_INFO_WARNINGS, clientWarnings());
    }

    @Test
    public void testGetTokenBalanceForAKnownTokenReadsOnlyTheBalance() throws IOException {
        Erc20Token token = new Erc20Token(Chain.AVALANCHE_MAIN, CONTRACT_ADDRESS, "Wrapped AVAX", "WAVAX", null, null);
        try (JsonRpcTestServer server = new JsonRpcTestServer(EvmClientTest::erc20Contract);
                EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, server.url())) {

            Erc20TokenBalance balance = evmClient.getTokenBalance(token, HOLDER_ADDRESS);

            Assertions.assertEquals(HOLDER_TOKEN_BALANCE, balance.getBalance());
            Assertions.assertSame(token, balance.getToken());
        }
    }

    @Test
    public void testGetTokenBalanceForAKnownTokenWrapsAConnectionFailure() throws IOException {
        Erc20Token token = new Erc20Token(Chain.AVALANCHE_MAIN, CONTRACT_ADDRESS, "Wrapped AVAX", "WAVAX", null, null);
        try (EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, unreachableUrl())) {

            Assertions.assertThrows(EvmClientException.class, () -> evmClient.getTokenBalance(token, HOLDER_ADDRESS));
        }
    }

    @Test
    public void testGetTokenBalanceRejectsATokenFromAnotherChain() throws IOException {
        Erc20Token token = new Erc20Token(Chain.ETHEREUM_MAIN, CONTRACT_ADDRESS, "Wrapped AVAX", "WAVAX", null, null);
        try (EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN, unreachableUrl())) {

            Assertions.assertThrows(
                    IllegalArgumentException.class, () -> evmClient.getTokenBalance(token, HOLDER_ADDRESS));
        }
    }

    @Test
    public void testCreateWeb3ServiceUsesHttpForAnHttpsUrl() {
        Assertions.assertInstanceOf(HttpService.class, EvmClient.createWeb3Service("https://rpc.example/"));
    }

    @Test
    public void testCreateWeb3ServiceRejectsMissingOrMalformedUrls() {
        Assertions.assertThrows(EvmClientException.class, () -> EvmClient.createWeb3Service(null));
        Assertions.assertThrows(EvmClientException.class, () -> EvmClient.createWeb3Service("localhost:8545"));
        Assertions.assertThrows(EvmClientException.class, () -> EvmClient.createWeb3Service("ftp://localhost/"));
    }

    @Test
    public void testCreateWeb3ServiceWrapsAFailedWebSocketConnection() throws IOException {
        String url = unreachableUrl().replace("http://", "ws://");

        EvmClientException exception =
                Assertions.assertThrows(EvmClientException.class, () -> EvmClient.createWeb3Service(url));

        Assertions.assertInstanceOf(ConnectException.class, exception.getCause());
    }

    private static String erc20Contract(JsonNode request) {
        String data = request.get("params").get(0).get("data").asText();
        String selector = data.substring(0, 10);
        return switch (selector) {
            case "0x06fdde03" -> "0x" + FunctionEncoder.encodeConstructor(List.of(new Utf8String("Wrapped AVAX")));
            case "0x95d89b41" -> "0x" + FunctionEncoder.encodeConstructor(List.of(new Utf8String("WAVAX")));
            case "0x313ce567" -> "0x" + TypeEncoder.encode(new Uint256(18));
            case "0x18160ddd" -> "0x" + TypeEncoder.encode(new Uint256(TOTAL_SUPPLY));
            case "0x70a08231" -> "0x" + TypeEncoder.encode(new Uint256(HOLDER_TOKEN_BALANCE));
            default -> throw new IllegalArgumentException("Unexpected call " + data);
        };
    }

    private List<String> clientWarnings() {
        return clientLog.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    private static String unreachableUrl() throws IOException {
        try (JsonRpcTestServer server = new JsonRpcTestServer(request -> "0x")) {
            return server.url();
        }
    }
}
