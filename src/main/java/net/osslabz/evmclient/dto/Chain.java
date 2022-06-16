package net.osslabz.evmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public class Chain implements Serializable {

    // Chain Details: https://support.avax.network/en/articles/4626956-how-do-i-set-up-metamask-on-avalanche
    public static final Chain AVALANCHE_MAIN = new Chain("Avalanche Network", "AVAX", NetworkType.MAIN, 43114, "https://api.avax.network/ext/bc/C/rpc", " https://snowtrace.io/");

    // Chain Details: https://docs.linkpool.io/docs/public_rpc
    public static final Chain ETHEREUM_MAIN = new Chain("Ethereum Network", "ETH", NetworkType.MAIN, 1, "https://cloudflare-eth.com/", " https://etherscan.io/");

    private final String name;

    private final NetworkType type;

    private int id;

    private final String rpcUrl;
    private final String explorerUrl;

    @JsonIgnore
    private final PrimaryCoin coin;

    Chain(String name, String symbol, NetworkType type, int id, String rpcUrl, String explorerUrl) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.rpcUrl = rpcUrl;
        this.explorerUrl = explorerUrl;

        this.coin = new PrimaryCoin(symbol);
    }


    public String toString() {
        return this.getClass().getName() + "(name=" + this.name + ", type=" + this.type + ", id=" + this.id + ", coin=" + this.coin + ")";
    }
}
