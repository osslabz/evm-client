package net.osslabz.evmclient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.osslabz.evmclient.dto.CoinBalance;
import net.osslabz.evmclient.dto.Erc20TokenBalance;
import net.osslabz.evmclient.dto.Chain;
import net.osslabz.evmclient.dto.Erc20Token;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.LongLivingWebSocketService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.time.Duration;
import java.util.Collections;

@Slf4j
@Getter
public class EvmClient implements Closeable {

    private final Chain chainInfo;

    private final Web3j web3j;


    public EvmClient(Chain chain) {
        this(chain, createWeb3Service(chain.getRpcUrl()));
    }

    public EvmClient(Chain chain, String url) {
        this(chain, createWeb3Service(url));
    }

    public EvmClient(Chain chain, Web3jService web3jService) {
        this.chainInfo = chain;
        this.web3j = Web3j.build(web3jService);
    }

    public static Web3jService createWeb3Service(String rpcUrlString) {
        try {
            if (rpcUrlString == null) {
                throw new EvmClientException("Can't instantiate Web3jService because the provided RPC URL is null.");
            }

            int protocolEndIndex = rpcUrlString.indexOf("://");

            if (protocolEndIndex <= 1) {
                throw new EvmClientException("Can't instantiate Web3jService because the provided RPC URL '" + rpcUrlString + "' is invalid.");
            }

            String protocol = rpcUrlString.substring(0, protocolEndIndex);

            switch (protocol) {
                case "ws":
                case "wss":
                    LongLivingWebSocketService webSocketService = new LongLivingWebSocketService(rpcUrlString, false);
                    webSocketService.connect();
                    return webSocketService;

                case "http":
                case "https":
                    HttpService httpService = new HttpService(rpcUrlString, createHttpClientWithCookieSupport());
                    return httpService;

                default:
                    throw new EvmClientException("Unknown protocol '" + protocol + "' in provided RPC URL '" + rpcUrlString + "'.");
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

        OkHttpClient okHttpClient = builder.build();
        return okHttpClient;
    }


    public Erc20Token getTokenInfo(String contractAddress) {

        ERC20 erc20 = loaErc20ContractWithReadOnlyDefaults(contractAddress);
        Erc20Token tokenInfo = getTokenInfo(erc20);

        return tokenInfo;
    }


    public CoinBalance getBalance(String address) {

        try {
            BigInteger balance = this.web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
            return new CoinBalance(this.chainInfo, balance);
        } catch (IOException e) {
            throw new EvmClientException(e);
        }
    }


    public Erc20TokenBalance getTokenBalance(String contractAddress, String address) {

        ERC20 erc20 = loaErc20ContractWithReadOnlyDefaults(contractAddress);
        Erc20Token tokenInfo = getTokenInfo(erc20);

        try {
            BigInteger balance = erc20.balanceOf(address).send();
            return new Erc20TokenBalance(tokenInfo, balance);
        } catch (Exception e) {
            throw new EvmClientException(e);
        }
    }


    public Erc20TokenBalance getTokenBalance(Erc20Token tokenInfo, String address) {
        if (!this.chainInfo.equals(tokenInfo.getChain())) {
            throw new IllegalArgumentException("This instance of " + this.getClass().getName() + " is bound to chain " + this.chainInfo + ", the provided token is from chain " + tokenInfo.getChain() + ".");
        }
        try {
            ERC20 erc20 = loaErc20ContractWithReadOnlyDefaults(tokenInfo.getContractAddress());
            BigInteger balance = erc20.balanceOf(address).send();
            return new Erc20TokenBalance(tokenInfo, balance);
        } catch (Exception e) {
            throw new EvmClientException(e);
        }
    }


    public ERC20 loaErc20ContractWithReadOnlyDefaults(String contractAddress) {

        TransactionManager readOnlyTransactionManager = new ReadonlyTransactionManager(web3j, null);
        ERC20 erc20 = ERC20.load(contractAddress, this.web3j, readOnlyTransactionManager, (ContractGasProvider) null);

        return erc20;
    }


    public BigInteger getLastBlockNumber() {
        try {
            return this.web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            throw new EvmClientException(e);
        }
    }


    public Erc20Token getTokenInfo(ERC20 erc20) {

        String contractAddress = erc20.getContractAddress();

        String name = null;
        String symbol = null;
        BigInteger decimals = null;
        BigInteger totalSupply = null;

        try {
            name = erc20.name().send();
        } catch (Exception e) {
            log.warn("Couldn't fetch name for contract address {}.", contractAddress);
        }
        try {
            symbol = erc20.symbol().send();
        } catch (Exception e) {
            log.warn("Couldn't fetch symbol for contract address {}.", contractAddress);
        }
        try {
            decimals = erc20.decimals().send();
        } catch (Exception e) {
            log.warn("Couldn't fetch decimals for contract address{}.", contractAddress);
        }
        try {
            totalSupply = erc20.totalSupply().send();
        } catch (Exception e) {
            log.warn("Couldn't fetch totalSupply for contract address {}.", contractAddress);
        }

        if (name == null && symbol == null && decimals == null && totalSupply == null) {
            throw new EvmClientException("Couldn't fetch any token info for " + contractAddress + ". Most likely not a valid ERC20 contract.");
        }


        Erc20Token tokenInfo = new Erc20Token(this.chainInfo, contractAddress, name, symbol, decimals, totalSupply);
        return tokenInfo;

    }


    public void shutdown() {
        this.web3j.shutdown();
    }


    /**
     * Alias for {@link #shutdown()} for those who fancy a closable interface.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.shutdown();
    }
}
