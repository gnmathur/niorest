package com.gmathur.niorest.http;

public class HttpUserAgent {
    public String product;
    public String version;

    public HttpUserAgent(String product, String version) {
        this.product = product;
        this.version = version;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User-Agent: ");
        sb.append(product).append("/").append(version);
        return sb.toString();
    }
}
