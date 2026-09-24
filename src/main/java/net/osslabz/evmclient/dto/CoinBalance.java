package net.osslabz.evmclient.dto;

import java.io.Serializable;
import java.math.BigInteger;
import lombok.Data;

@Data
public class CoinBalance implements Serializable {

    private final PrimaryCoin coin;

    private final BigInteger balance;

    public CoinBalance(Chain chain, BigInteger balance) {
        this.coin = chain.getCoin();
        this.balance = balance;
    }
}
