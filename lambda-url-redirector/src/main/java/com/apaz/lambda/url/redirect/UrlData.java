package com.apaz.lambda.url.redirect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlData {
    private String originalUrl;
    private long expirationInstant;
}
