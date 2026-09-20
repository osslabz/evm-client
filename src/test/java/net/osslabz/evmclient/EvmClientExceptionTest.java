package net.osslabz.evmclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EvmClientExceptionTest {

    @Test
    public void testMessageAndCauseArePreserved() {
        Exception cause = new IllegalStateException("underlying failure");

        EvmClientException exception = new EvmClientException("couldn't read the contract", cause);

        Assertions.assertEquals("couldn't read the contract", exception.getMessage());
        Assertions.assertSame(cause, exception.getCause());
    }
}
