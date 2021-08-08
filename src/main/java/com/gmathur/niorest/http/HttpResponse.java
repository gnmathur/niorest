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

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.gmathur.niorest.http.HttpExtra.*;
import static com.gmathur.niorest.http.HttpResponseHeader.*;

public class HttpResponse {
    private List<HttpTransferEncoding> transferEncoding;
    private HttpStatusLine statusLine;
    private ContentType contentType;
    public String payload;
    public byte[] messageBody;

    private HttpResponse(final String payload,
                         final ContentType contentType,
                         final HttpStatusLine statusLine,
                         final List<HttpTransferEncoding> transferEncoding,
                         final byte[] messageBody) {
        this.payload = payload;
        this.contentType = contentType;
        this.statusLine = statusLine;
        this.transferEncoding = transferEncoding;
        this.messageBody = messageBody;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpResponse{");
        sb.append("transferEncoding=").append(transferEncoding);
        sb.append(", statusLine=").append(statusLine);
        sb.append(", contentType=").append(contentType);
        sb.append(", payload='").append(payload).append('\'');
        sb.append(", messageBody=").append(new String(messageBody, 0, Math.min(messageBody.length, 128), StandardCharsets.UTF_8)); // ToDo only printable
        sb.append('}');
        return sb.toString();
    }

    // Builder
    public static HttpResponseBuilder newBuilder() { return new HttpResponseBuilder(); }

    public static class HttpResponseBuilder {
        private HttpStatusLine statusLine;
        private List<HttpTransferEncoding> transferEncoding;
        private ContentType contentType;
        private String payload;
        private byte[] messageBody;

        public HttpResponseBuilder payload(final String payload) {
            this.payload = payload;
            return this;
        }
        public HttpResponseBuilder contentType(final ContentType contentType) {
            this.contentType = contentType;
            return this;
        }
        public HttpResponseBuilder statusLine(final HttpStatusLine statusLine) {
            this.statusLine = statusLine; return  this;
        }

        public HttpResponseBuilder transferEncoding(final List<HttpTransferEncoding> transferEncoding) {
            this.transferEncoding = transferEncoding;
            return this;
        }

        public HttpResponseBuilder messageBody(final byte[] messageBody) {
            this.messageBody = messageBody;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(payload, contentType, statusLine, transferEncoding, messageBody);
        }
    }

}
