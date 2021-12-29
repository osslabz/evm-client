package net.osslabz.evmclient;

import lombok.Getter;
import net.osslabz.evmclient.dto.CoinBalance;
import net.osslabz.evmclient.dto.Erc20TokenBalance;
import net.osslabz.evmclient.dto.Chain;
import net.osslabz.evmclient.dto.Erc20Token;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;

@Getter
public class EvmClient implements Closeable {

    private final Chain chainInfo;

    private final Web3j web3j;


    public EvmClient(Chain chain) {
        this.chainInfo = chain;
        this.web3j = Web3j.build(new HttpService(this.chainInfo.getRpcUrl()));
    }


    public Erc20Token getTokenInfo(String contractAddress) {

        ERC20 erc20 = loadContractWithReadOnlyDefaults(contractAddress);
        Erc20Token tokenInfo = getTokenInfo(erc20);

        return tokenInfo;
    }


    public CoinBalance getBalance(String address) {

        try {
            BigInteger balance = this.web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
            String code = this.web3j.ethGetCode(address, DefaultBlockParameterName.LATEST).send().getCode();
            return new CoinBalance(this.chainInfo, balance);
        } catch (IOException e) {
            throw new EvmClientException(e);
        }
    }


    public Erc20TokenBalance getTokenBalance(String contractAddress, String address) {

        ERC20 erc20 = loadContractWithReadOnlyDefaults(contractAddress);
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
            ERC20 erc20 = loadContractWithReadOnlyDefaults(tokenInfo.getContractAddress());
            BigInteger balance = erc20.balanceOf(address).send();
            return new Erc20TokenBalance(tokenInfo, balance);
        } catch (Exception e) {
            throw new EvmClientException(e);
        }
    }


    public ERC20 loadContractWithReadOnlyDefaults(String contractAddress) {

        ReadonlyTransactionManager readOnlyTransactionManager = new ReadonlyTransactionManager(web3j, contractAddress);
        DefaultGasProvider contractGasProvider = new DefaultGasProvider();

        ERC20 erc20 = ERC20.load(contractAddress, this.web3j, readOnlyTransactionManager, contractGasProvider);
        return erc20;
    }


    private Erc20Token getTokenInfo(ERC20 erc20) {

        String contractAddress = erc20.getContractAddress();

        try {
            String name = erc20.name().send();
            String symbol = erc20.symbol().send();
            BigInteger decimals = erc20.decimals().send();
            BigInteger totalSupply = erc20.totalSupply().send();

            Erc20Token tokenInfo = new Erc20Token(this.chainInfo, contractAddress, name, symbol, decimals, totalSupply);

            return tokenInfo;
        } catch (Exception e) {
            throw new EvmClientException(e);
        }
    }


    public void shutdown() {
        this.web3j.shutdown();
    }


    /**
     * Alias for {@link this.shutdown()} for those who fancy a closable interface.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.shutdown();
    }
}
