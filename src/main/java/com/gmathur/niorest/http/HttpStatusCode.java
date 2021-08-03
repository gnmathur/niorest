package com.gmathur.niorest.http;

public interface HttpStatusCode {
    static final int CONTINUE = 100;
    static final int SWITCHING_PROTOCOLS = 101;
    static final int OK = 200;
    static final int CREATED = 201;
    static final int ACCEPTED = 202;
    static final int NON_AUTHORITATIVE_INFORMATION = 203;
    static final int NO_CONTENT = 204;
    static final int RESET_CONTENT = 205;
    static final int PARTIAL_CONTENT = 206;
    static final int MULTIPLE_CHOICES = 300;
    static final int BAD_REQUEST = 400;
    static final int NOT_FOUND = 404;

    public static int parse(byte[] status) {
        return CONTINUE;
    }
}
