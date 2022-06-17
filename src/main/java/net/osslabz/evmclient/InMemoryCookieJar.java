package net.osslabz.evmclient;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class InMemoryCookieJar implements CookieJar {

    private List<Cookie> cookies;

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies == null) {
            log.debug("No cookies present (null), no cookies saved.");
        } else if (cookies.isEmpty()) {
            log.debug("No cookies present (empty list), no cookies saved.");
        } else {
            log.debug("{} cookies saved to memory.", cookies.size());
            if (this.cookies == null) {
                this.cookies = new ArrayList<>();
            }
            for (Cookie cookie : cookies) {
                if (!this.cookies.contains(cookie)) {
                    this.cookies.add(cookie);
                }
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (this.cookies != null) {
            log.debug("Sending {} cookies from memory storage.", this.cookies.size());
            return this.cookies;
        }

        log.debug("No cookies available in memory, returning empty list.");
        return Collections.emptyList();
    }
}

