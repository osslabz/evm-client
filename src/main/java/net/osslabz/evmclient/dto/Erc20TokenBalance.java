package net.osslabz.evmclient.dto;

import java.io.Serializable;
import java.math.BigInteger;
import lombok.Data;

@Data
public class Erc20TokenBalance implements Serializable {

    private final Erc20Token token;

    private final BigInteger balance;

    public Erc20TokenBalance(Erc20Token token, BigInteger balance) {
        this.token = token;
        this.balance = balance;
    }
}
