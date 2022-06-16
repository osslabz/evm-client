package net.osslabz.evmclient.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Represents the primary coin of a blockchain protocol, e.g. Ether (ETH) on Ethereum or AVAX on Avalanche.
 * <p>
 * Each Coin instance carries a {@link Chain} instance that provides detailed information of the token's chain.
 */
@Getter
@Setter
public class PrimaryCoin implements Serializable {

    private String name;

    private String symbol;

    private BigInteger decimals;


    public PrimaryCoin(String name) {
        this(name, name);
    }


    public PrimaryCoin(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }


    public PrimaryCoin(String name, String symbol, BigInteger decimals) {
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
    }


    public String toString() {
        return this.getClass().getName() + "(name=" + this.name + ", symbol=" + this.symbol + ", decimals=" + this.decimals + ")";
    }
}
