package net.codelabz.evmclient.dto;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
public class Chain implements Serializable {

    // Chain Details: https://support.avax.network/en/articles/4626956-how-do-i-set-up-metamask-on-avalanche
    public static final Chain AVALANCHE_MAIN = new Chain("Avalanche Network", "AVAX", NetworkType.MAIN, 43114, "https://api.avax.network/ext/bc/C/rpc", " https://snowtrace.io/");

    // Chain Details: https://docs.linkpool.io/docs/public_rpc
    public static final Chain ETHEREUM_MAIN = new Chain("Ethereum Network", "ETH", NetworkType.MAIN, 1, "https://cloudflare-eth.com/", " https://etherscan.io/");

    private final String name;
    private final String symbol;

    private final NetworkType type;

    private int id;

    private final String rpcUrl;
    private final String explorerUrl;


    Chain(String name, String symbol, NetworkType type, int id, String rpcUrl, String explorerUrl) {
        this.name = name;
        this.symbol = symbol;
        this.type = type;
        this.id = id;
        this.rpcUrl = rpcUrl;
        this.explorerUrl = explorerUrl;
    }


    public String toString() {
        return this.getClass().getName() + "(name=" + this.name + ", symbol=" + this.symbol + ", type=" + this.type + ", id=" + this.id + ")";
    }
}
