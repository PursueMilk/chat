package com.example.chat.exception;

public class ChatException extends RuntimeException {

    private String msg;


    public ChatException() {
        super();
    }

    public ChatException(String message) {
        super(message);
        this.msg = message;
    }

    public ChatException(String message, Throwable cause) {
        super(message, cause);
        this.msg = message;
    }
}
