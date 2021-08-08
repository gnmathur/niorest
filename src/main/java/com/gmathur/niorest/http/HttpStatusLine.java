package com.gmathur.niorest.http;

import java.util.Optional;

public class HttpStatusLine {
    public String httpVersion;
    public int statusCode;
    public Optional<String> reason = Optional.empty();

    public static HttpStatusLine parseStatusLine(final String statusLine) {
        final HttpStatusLine s = new HttpStatusLine();

        final String[] tokens = statusLine.split(" ");

        s.httpVersion = tokens[0];
        s.statusCode = Integer.parseInt(tokens[1]);
        if (tokens.length > 2) {
            // https://datatracker.ietf.org/doc/html/rfc2616#section-6.1.1
            // Reason code is not necessarily present
            s.reason = Optional.of(tokens[2]);
        }
        return s;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpStatusLine{");
        sb.append("httpVersion='").append(httpVersion).append('\'');
        sb.append(", statusCode=").append(statusCode);
        sb.append(", reason=").append(reason);
        sb.append('}');
        return sb.toString();
    }
}
