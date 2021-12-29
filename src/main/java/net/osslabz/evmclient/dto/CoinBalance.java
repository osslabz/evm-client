package net.osslabz.evmclient.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
public class CoinBalance implements Serializable {

    private final Coin coin;

    private final BigInteger balance;


    public CoinBalance(Chain chain, BigInteger balance) {
        this.coin = chain.getCoin();
        this.balance = balance;
    }
}
