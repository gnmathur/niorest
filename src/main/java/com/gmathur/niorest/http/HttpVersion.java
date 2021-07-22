package com.gmathur.niorest.http;

public enum HttpVersion {
    HTTP_1_1("HTTP/1.1"),
    HTTP_2("HTTP/2"),
    HTTP_3("HTTP/3");

    public final String label;

    private HttpVersion(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

