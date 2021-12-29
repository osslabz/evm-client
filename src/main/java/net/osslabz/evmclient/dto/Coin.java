package net.osslabz.evmclient.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Represents the primary coin of a blockchain protocol, e.g. Ether (ETH) on Ethereum or AVAX on Avalanche.
 * <p>
 * Each Coin instance carries a {@link Chain} instance that provides detailed information of the token's chain.
 */
@Data
public class Coin implements Serializable {

    private final Chain chain;

    private String name;

    private String symbol;

    private BigInteger decimals;


    public Coin(Chain chain, String symbol) {
        this(chain, symbol, symbol);
    }


    public Coin(Chain chain, String name, String symbol) {
        this.chain = chain;
        this.name = name;
        this.symbol = symbol;
    }


    public Coin(Chain chain, String name, String symbol, BigInteger decimals) {
        this.chain = chain;
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
    }


    public String toStringWithoutChain() {
        return this.getClass().getName() + "(name=" + this.name + ", symbol=" + this.symbol + ", decimals=" + this.decimals + ")";
    }


    public String toString() {
        return this.getClass().getName() + "(name=" + this.name + ", symbol=" + this.symbol + ", decimals=" + this.decimals + ", chain=" + this.chain + ")";
    }
}
