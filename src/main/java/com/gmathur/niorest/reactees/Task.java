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
package com.gmathur.niorest.reactees;

import com.gmathur.niorest.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class Task implements Reactee {
    private static final Logger logger = LoggerFactory.getLogger(Task.class.getCanonicalName());

    private final String clientId;
    private final ByteBuffer buffer;
    private final HttpRequest request;
    private final Long interval;
    private final String host;
    private final Short port;
    private final String endpoint;
    private final Long maxBackoffTimeMs = 64000L;
    private final ReacteeState reacteeState;
    private final HttpResponseParser httpResponseParser;


    private Task(final String host, final Short port, final String endpoint, final Long interval) {
        this.buffer = ByteBuffer.allocate(1024);
        HttpRequest.HttpRequestBuilder builder = HttpRequest.newBuilder();
        request = builder
                .host(host).port(port).endpoint(endpoint)
                .useragent(new HttpUserAgent("niorestapp", "0.1alpha"))
                .version(HttpVersion.HTTP_1_1)
                .type(HttpRequestType.HTTP_GET)
                .build();
        this.interval = interval;
        this.host = host;
        this.port = port;
        this.endpoint = endpoint;
        this.reacteeState = new ReacteeState();
        this.clientId = String.format("%s:%s:%s", host, port, endpoint);
        this.httpResponseParser = new HttpResponseParser();
    }

    @Override
    public ReacteeState getState() {
        return reacteeState;
    }

    @Override
    public Long getMaxBackoffTimeMs() {
        return maxBackoffTimeMs;
    }

    @Override
    public Long getInterval()       { return interval;  }

    @Override
    public String getHost()         { return host;      }

    @Override
    public Short getPort()          { return port;      }
    public String getEndpoint()     { return endpoint;  }

    @Override
    public String getId()     { return clientId;  }
    @Override
    public ByteBuffer getBuffer()   { return buffer; }

    @Override
    public byte[] getRequestBytes() { return request.toBytes(); }

    @Override
    public boolean readCb(ByteBuffer msg) {
        boolean msgDone = false;
        while (msg.hasRemaining()) {
            HttpResponseParseState s = httpResponseParser.runForInput(msg.get());
            if (s == HttpResponseParseState.DONE) {
                HttpResponse httpResponse = httpResponseParser.toHttpResponse();
                logger.info("{}", httpResponse);
                httpResponseParser.clear();
                msgDone = true;
            }
        }
        return msgDone;
    }

    @Override
    public void writeCb(boolean didWrite) {

    }

    /**
     * Create a clone of a task. The clone has the same config as the original task, with the Task state reset.
     * @return The cloned Task
     */
    @Override
    public Task clone() {
        return new TaskBuilder()
                .host(host)
                .port(port)
                .endpoint(endpoint)
                .interval(interval)
                .build();
    }
    public static TaskBuilder newBuilder() {
        return new TaskBuilder();
    }

    public static class TaskBuilder {
        private String host;
        private Short port;
        private String endpoint;
        private Long interval;

        private TaskBuilder() {}

        public TaskBuilder host(final String host) { this.host = host; return this; }
        public TaskBuilder port(final Short port) { this.port = port; return this; }
        public TaskBuilder endpoint(final String endpoint) { this.endpoint = endpoint; return this; }
        public TaskBuilder interval(final Long interval) { this.interval = interval; return this; }

        public Task build() {
            return new Task(host, port, endpoint, interval);
        }
    }

}
