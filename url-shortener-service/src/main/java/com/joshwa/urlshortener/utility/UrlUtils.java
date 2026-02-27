package com.joshwa.urlshortener.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {
    private static final String SHORT_CODE_CHARSET=
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String HTTP_PROTOCOL_PREFIX = "http://";
    private static final String HTTPS_PROTOCOL_PREFIX = "https://";
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    public static final String INVALID_URL_EXCEPTION_MESSAGE="Invalid url.";
    public static final String INVALID_EXPIRY_EXCEPTION_MESSAGE="Expiry date is in the past.";
    public static final String SHORT_CODE_GENERATION_EXCEPTION_MESSAGE="Could not generate unique code after max attempts.";
    private static final SecureRandom random = new SecureRandom();
    public static final String SHORT_URL_NOT_FOUND_EXCEPTION_MESSAGE="The requested short URL does not exist.";
    public static final String SHORT_URL_EXPIRED_EXCEPTION_MESSAGE="The requested short URL has expired.";

    public static String generateShortCode(int length) {
        StringBuilder shortCode = new StringBuilder();
        for (int i = 0; i < length; i++) {
            shortCode.append(SHORT_CODE_CHARSET.charAt(random.nextInt(SHORT_CODE_CHARSET.length())));
        }
        return shortCode.toString();
    }

    public static String normalizeUrl(String url){
        String originalUrl=url.trim();
        String testUrl = url.toLowerCase().trim();
        if(testUrl.startsWith(HTTP_PROTOCOL_PREFIX) || testUrl.startsWith(HTTPS_PROTOCOL_PREFIX)){
            return originalUrl;
        } else{
            return HTTPS_PROTOCOL_PREFIX.concat(originalUrl);
        }
    }

    public static boolean isValidUrl(String url) {
        URI uri;
        try{
           uri=URI.create(url);
        }catch(IllegalArgumentException | NullPointerException e) {
            return false;
        }
        return !StringUtils.isBlank(uri.getScheme())
                && (uri.getScheme().equalsIgnoreCase(HTTP_PROTOCOL)
                || uri.getScheme().equalsIgnoreCase(HTTPS_PROTOCOL))
                && !StringUtils.isBlank(uri.getHost());
    }
}
