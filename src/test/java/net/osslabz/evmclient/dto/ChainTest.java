package net.osslabz.evmclient.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChainTest {

    @Test
    public void testExplorerUrlsAreUsableAsIs() {
        Assertions.assertEquals("https://snowtrace.io/", Chain.AVALANCHE_MAIN.getExplorerUrl());
        Assertions.assertEquals("https://etherscan.io/", Chain.ETHEREUM_MAIN.getExplorerUrl());
    }
}
