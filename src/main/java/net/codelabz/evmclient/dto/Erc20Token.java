package net.codelabz.evmclient.dto;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigInteger;

@Data
public class Erc20Token implements Serializable {

    private final Chain chain;

    private final String contractAddress;

    private String name;

    private String symbol;

    private BigInteger decimals;

    private final BigInteger totalSupply;


    public Erc20Token(Chain chainInfo, String contractAddress, String name, String symbol, BigInteger decimals, BigInteger totalSupply) {
        this.chain = chainInfo;
        this.contractAddress = contractAddress;
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.totalSupply = totalSupply;
    }
}
