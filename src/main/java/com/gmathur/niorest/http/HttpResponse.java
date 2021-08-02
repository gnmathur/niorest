package com.gmathur.niorest.http;

import java.util.regex.Pattern;

import static com.gmathur.niorest.http.HttpExtra.*;

public class HttpResponse {
    private ContentType contentType;
    public String payload;

    private HttpResponse(final String payload, final ContentType contentType) {
        this.payload = payload;
        this.contentType = contentType;
    }

    public static HttpResponse parse(final String message) {
        String[] lines = message.split("\r\n");
        HttpResponseBuilder builder = newBuilder();
        for (String line : lines) {
            if (0 == line.compareTo("\r\n")) {
                builder.payload(line);
            } else if (line.startsWith("Content-Type")) {
                // ToDo Assuming json for now.. fix parsing
                builder.contentType(ContentType.APPLICATION_JSON);
            }
        }
        return builder.build();
    }

    public static HttpResponseBuilder newBuilder() { return new HttpResponseBuilder(); }

    public static class HttpResponseBuilder {
        private ContentType contentType;
        private String payload;

        public HttpResponseBuilder payload(final String payload) {
            this.payload = payload;
            return this;
        }
        public HttpResponseBuilder contentType(final ContentType contentType) {
            this.contentType = contentType;
            return this;
        }
        public HttpResponse build() {
            return new HttpResponse(payload, contentType);
        }
    }

}
