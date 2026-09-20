package net.osslabz.evmclient.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChainTest {

    @Test
    public void testExplorerUrlsAreUsableAsIs() {
        Assertions.assertEquals("https://snowtrace.io/", Chain.AVALANCHE_MAIN.getExplorerUrl());
        Assertions.assertEquals("https://etherscan.io/", Chain.ETHEREUM_MAIN.getExplorerUrl());
    }

    @Test
    public void testBinanceSmartChainMain() {
        Chain chain = Chain.BINANCE_SMART_CHAIN_MAIN;

        Assertions.assertEquals("Binance Smart Chain", chain.getName());
        Assertions.assertEquals(NetworkType.MAIN, chain.getType());
        Assertions.assertEquals(56, chain.getId());
        Assertions.assertEquals("BNB", chain.getCoin().getSymbol());
        Assertions.assertEquals("https://bsc-dataseed.binance.org/", chain.getRpcUrl());
        Assertions.assertEquals("https://bscscan.com", chain.getExplorerUrl());
    }
}
