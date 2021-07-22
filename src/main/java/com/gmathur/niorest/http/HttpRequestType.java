package com.gmathur.niorest.http;

public enum HttpRequestType {
    HTTP_GET("GET"),
    HTTP_PUT("PUT"),
    HTTP_POST("POST");

    public final String label;

    private HttpRequestType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

