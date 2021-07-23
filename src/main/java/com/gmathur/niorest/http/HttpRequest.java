/*
 * Copyright (c) 2021 Gaurav Mathur
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.gmathur.niorest.http;

/**
 *  HTTP Request
 *
 * ToDo: Handle line charset
 */
public class HttpRequest {
    private String host;
    private String port;
    private String endpoint;
    private HttpRequestType type;
    private HttpVersion version;
    private HttpUserAgent userAgent;
    private final static char CR  = (char) 0x0D;
    private final static char LF  = (char) 0x0A;
    private final static String CRLF = "" + CR + LF;

    public HttpRequest(String host, String port, String endpoint, HttpRequestType type, HttpVersion version, HttpUserAgent userAgent) {
        this.host = host;
        this.port = port;
        this.endpoint = endpoint;
        this.type = type;
        this.version = version;
        this.userAgent = userAgent;
    }

    public static HttpRequestBuilder newBuilder() {
        return new HttpRequestBuilder();
    }

    public byte[] toBytes() {
        StringBuilder buffer = new StringBuilder();
        // HTTP request
        buffer.append(type).append(" ").append(endpoint).append(" ").append(version).append(CRLF);
        // HTTP Host
        buffer.append("Host: ").append(host).append(":").append(port).append(CRLF);
        // HTTP User-Agent
        buffer.append(userAgent).append(CRLF);
        // Last line of request
        buffer.append(CRLF);
        // ToDo: convery to bytes according to charset
        return buffer.toString().getBytes();
    }

    public static class HttpRequestBuilder {
        private String host;
        private Short port;
        private String endpoint;
        private HttpRequestType type;
        private HttpVersion version;
        private HttpUserAgent userAgent;

        private HttpRequestBuilder() { }

        public HttpRequestBuilder host(String host) {
            this.host = host;
            return this;
        }

        public HttpRequestBuilder port(Short port) {
            this.port = port;
            return this;
        }

        public HttpRequestBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public HttpRequestBuilder type(HttpRequestType type) {
            this.type = type;
            return this;
        }

        public HttpRequestBuilder useragent(HttpUserAgent agent) {
            this.userAgent = agent;
            return this;
        }

        public HttpRequestBuilder version(HttpVersion version) {
            this.version = version;
            return this;
        }

        public HttpRequest build() {
            if (userAgent == null) {
                userAgent = new HttpUserAgent("niorestlib", "0.1");
            }
            return new HttpRequest(host, port.toString(), endpoint, type, version, userAgent);
        }
    }
}
