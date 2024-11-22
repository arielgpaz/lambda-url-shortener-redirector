package com.apaz.lambda.url.redirect;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UrlData {
    private String originalUrl;
    private long expirationInstant;

    public UrlData(String originalUrl, String ttl) {
        this.originalUrl = this.insertHttpsPrefix(originalUrl);
        this.expirationInstant = this.toExpirationInstant(ttl);
    }

    private String insertHttpsPrefix(String originalUrl) {
        if (originalUrl.startsWith("http://") || originalUrl.startsWith("https://")) {
            return originalUrl;
        }
        return "https://" + originalUrl;
    }

    private long toExpirationInstant(String ttl) {
        return System.currentTimeMillis() + (Long.parseLong(ttl) * 1000);
    }
}
