package net.osslabz.evmclient;

import java.util.List;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InMemoryCookieJarTest {

    private static final HttpUrl URL = HttpUrl.get("https://api.avax.network/ext/bc/C/rpc");

    @Test
    public void testEmptyListWithoutStoredCookies() {
        InMemoryCookieJar cookieJar = new InMemoryCookieJar();

        Assertions.assertTrue(cookieJar.loadForRequest(URL).isEmpty());
    }

    @Test
    public void testExpiredCookiesAreNotReturned() {
        InMemoryCookieJar cookieJar = new InMemoryCookieJar();
        cookieJar.saveFromResponse(
                URL, List.of(cookie("expired", "api.avax.network", System.currentTimeMillis() - 1000)));

        Assertions.assertTrue(cookieJar.loadForRequest(URL).isEmpty());
    }

    @Test
    public void testOnlyCookiesMatchingTheUrlAreReturned() {
        InMemoryCookieJar cookieJar = new InMemoryCookieJar();
        long expiresAt = System.currentTimeMillis() + 60_000;
        cookieJar.saveFromResponse(
                URL,
                List.of(
                        cookie("session", "api.avax.network", expiresAt),
                        cookie("other", "cloudflare-eth.com", expiresAt)));

        List<Cookie> cookies = cookieJar.loadForRequest(URL);

        Assertions.assertEquals(1, cookies.size());
        Assertions.assertEquals("session", cookies.get(0).name());
    }

    private static Cookie cookie(String name, String domain, long expiresAt) {
        return new Cookie.Builder()
                .name(name)
                .value("value")
                .hostOnlyDomain(domain)
                .expiresAt(expiresAt)
                .build();
    }
}
