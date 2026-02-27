package com.joshwa.urlshortener.exception;

public class ShortUrlNotFoundException extends Exception{
    public ShortUrlNotFoundException(String message){
        super(message);
    }
}
