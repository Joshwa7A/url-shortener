package com.joshwa.urlshortener.exception;

public class InvalidExpiryException extends Exception{
    public InvalidExpiryException(String message){
        super(message);
    }
}
