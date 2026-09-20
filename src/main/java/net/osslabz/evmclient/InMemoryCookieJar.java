package net.osslabz.evmclient;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryCookieJar implements CookieJar {

    private final List<Cookie> cookies = new ArrayList<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies == null) {
            log.debug("No cookies present (null), no cookies saved.");
        } else if (cookies.isEmpty()) {
            log.debug("No cookies present (empty list), no cookies saved.");
        } else {
            log.debug("{} cookies saved to memory.", cookies.size());
            for (Cookie cookie : cookies) {
                if (!this.cookies.contains(cookie)) {
                    this.cookies.add(cookie);
                }
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        this.cookies.removeIf(cookie -> cookie.expiresAt() < System.currentTimeMillis());

        List<Cookie> matchingCookies = this.cookies.stream().filter(cookie -> cookie.matches(url)).collect(Collectors.toList());

        if (matchingCookies.isEmpty()) {
            log.debug("No matching cookies for url={} available in memory, returning empty list (total num of cookies is {}).", url, this.cookies.size());
            return Collections.emptyList();
        }

        log.debug("Sending {} matching cookies for url={} from memory storage (total num of cookies is {}).", matchingCookies.size(), url, this.cookies.size());
        return matchingCookies;
    }
}

