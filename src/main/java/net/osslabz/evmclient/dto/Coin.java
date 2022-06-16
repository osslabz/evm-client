package net.osslabz.evmclient.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Represents the primary coin of a blockchain protocol, e.g. Ether (ETH) on Ethereum or AVAX on Avalanche.
 * <p>
 * Each Coin instance carries a {@link Chain} instance that provides detailed information of the token's chain.
 */
@Getter
@Setter
public class Coin extends PrimaryCoin {

    private final Chain chain;

    public Coin(Chain chain, String symbol) {
        this(chain, symbol, symbol);
    }


    public Coin(Chain chain, String name, String symbol) {
        super(name, symbol);
        this.chain = chain;
    }


    public Coin(Chain chain, String name, String symbol, BigInteger decimals) {
        super(name, symbol, decimals);
        this.chain = chain;
    }


    public String toString() {
        return super.toString() + "[chain= " + this.chain + "]";
    }
}
