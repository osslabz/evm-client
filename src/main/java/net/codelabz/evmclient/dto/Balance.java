package net.codelabz.evmclient.dto;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigInteger;

@Data
public class Balance implements Serializable {

    private final Erc20Token token;

    private final BigInteger balance;

    public Balance(Erc20Token token, BigInteger balance) {
        this.token = token;
        this.balance = balance;
    }
}
