package net.osslabz.evmclient;

import lombok.extern.slf4j.Slf4j;
import net.osslabz.evmclient.dto.Chain;
import net.osslabz.evmclient.dto.CoinBalance;
import net.osslabz.evmclient.dto.Erc20Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class EvmClientTest {

    public static final String CONTRACT_ADDRESS_WRAPPED_AVAX_ON_AVALANCHE = "0xb31f66aa3c1e785363f0875a1b74e27b85fd66c7";

    @Test
    public void testGetTokenInfo() throws Exception {
        EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN);
        Erc20Token tokenInfo = evmClient.getTokenInfo(CONTRACT_ADDRESS_WRAPPED_AVAX_ON_AVALANCHE);

        log.debug("tokenInfo: {}", tokenInfo);

        Erc20Token tokenInfo2 = evmClient.getTokenInfo(CONTRACT_ADDRESS_WRAPPED_AVAX_ON_AVALANCHE);

        Assertions.assertEquals("WAVAX", tokenInfo.getSymbol());
        Assertions.assertEquals("Wrapped AVAX", tokenInfo.getName());
        Assertions.assertEquals(18, tokenInfo.getDecimals().intValueExact());
    }

    @Test
    public void testGetBalance() throws Exception {
        EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN);
        CoinBalance balance = evmClient.getBalance("0x81c36bAb8dB9C25E6736427C13E183B881Cb00bD");

        log.debug("balance: {}", balance);
    }
}
