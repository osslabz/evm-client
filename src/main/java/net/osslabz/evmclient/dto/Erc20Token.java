package net.osslabz.evmclient.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Locale;

/**
 * Represents an ERC-20 compatible token on a EVM-compatible chain.
 * <p>
 * Each token instance carries a Chain instance that provides detailed information of the token's chain.
 */

@Getter
@Setter
public class Erc20Token extends Coin implements Serializable {

    private final String contractAddress;

    private final BigInteger totalSupply;


    public Erc20Token(Chain chainInfo, String contractAddress, String name, String symbol, BigInteger decimals, BigInteger totalSupply) {
        super(chainInfo, name, symbol, decimals);
        this.contractAddress = contractAddress.toLowerCase(Locale.ROOT);
        this.totalSupply = totalSupply;
    }
}
