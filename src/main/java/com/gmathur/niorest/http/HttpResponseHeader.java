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

import com.gmathur.niorest.utils.HashRuleTokenizer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class HttpResponseHeader {
    public static enum HttpHeaderFields {
        CONTENT_ENCODING("Content-Encoding"),
        CONTENT_LENGTH("Content-Length"),
        CONTENT_TYPE("Content-Type"),
        DATE("Date"),
        TRANSFER_ENCODING("Transfer-Encoding");

        private String fieldNameStr;
        HttpHeaderFields(String fieldNameStr) { this.fieldNameStr = fieldNameStr; }

        public String getFieldNameStr() { return fieldNameStr; }
    }
    public static enum HttpTransferEncoding {
        CHUNKED,
        COMPRESS,
        DEFLATE,
        GZIP,
        IDENTITY
    }
    private static Map<String, HttpTransferEncoding> TeStringToEnum = Map.of(
            "chunked", HttpTransferEncoding.CHUNKED,
            "compress", HttpTransferEncoding.COMPRESS,
            "deflate", HttpTransferEncoding.DEFLATE,
            "gzip", HttpTransferEncoding.GZIP
    );
    private HttpResponseHeader() { }

    public static class TransferEncodingParser implements Function<String, List<HttpTransferEncoding>> {
        @Override
        public List<HttpTransferEncoding> apply(String s) {
            String hv = s.substring(HttpHeaderFields.TRANSFER_ENCODING.fieldNameStr.length()+1).stripLeading();
            List<String> token = HashRuleTokenizer.tokenize(hv);
            return token.stream().map(ts -> TeStringToEnum.get(ts)).collect(Collectors.toList());
        }
    }

    public static class ContentLengthParser implements Function<String, Integer> {
        // Thread-safe; Some efficieny achieved by having a static buffer and also not deleting the characters in the buffer
        private static final StringBuffer contentStr = new StringBuffer();

        @Override
        public Integer apply(String s) {
            contentStr.setLength(0);
            int i = 0;
            while (i < s.length() && (s.charAt(i) < '0' || s.charAt(i) > '9')) { // Todo throw error if non space
                i++;
            }
            while (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') {
                contentStr.append(s.charAt(i));
                i++;
            }
            if (contentStr.length() == 0) throw new IllegalStateException("Error parsing Content-Length");
            return Integer.parseInt(contentStr.toString());
        }
    }
}
