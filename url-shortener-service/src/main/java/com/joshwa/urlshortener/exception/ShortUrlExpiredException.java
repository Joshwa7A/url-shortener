package com.joshwa.urlshortener.exception;

public class ShortUrlExpiredException extends Exception {
    public ShortUrlExpiredException(String message){
        super(message);
    }

}
