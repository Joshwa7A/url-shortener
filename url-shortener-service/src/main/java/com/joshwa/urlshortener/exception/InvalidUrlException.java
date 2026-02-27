package com.joshwa.urlshortener.exception;

public class InvalidUrlException extends Exception{
    public InvalidUrlException(String message){
        super(message);
    }
}
