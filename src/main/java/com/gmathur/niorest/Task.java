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
package com.gmathur.niorest;

import com.gmathur.niorest.http.HttpRequest;
import com.gmathur.niorest.http.HttpRequestType;
import com.gmathur.niorest.http.HttpUserAgent;
import com.gmathur.niorest.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Task {
    private final Logger logger = LoggerFactory.getLogger(Task.class.getCanonicalName());

    private final String clientId = UUID.randomUUID().toString();
    private final ByteBuffer buffer;
    private final HttpRequest request;
    private final Long interval;
    private final String host;
    private final Short port;
    private final String endpoint;
    public final Long maxBackoffTimeMs = 64000L;
    public final TaskState taskState;
    public final String identifier;

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
        this.taskState = new TaskState();
        this.identifier = String.format("%s:%s:%s", host, port, endpoint);
    }

    public String getClientId()     { return clientId;  }
    public Long getInterval()       { return interval;  }
    public String getHost()         { return host;      }
    public Short getPort()          { return port;      }
    public String getEndpoint()     { return endpoint;  }
    public ByteBuffer getBuffer()   { return buffer; }
    public byte[] getRequestBytes() { return request.toBytes(); }

    public static TaskBuilder newBuilder() {
        return new TaskBuilder();
    }

    public static class TaskBuilder {
        private String host;
        private Short port;
        private String endpoint;
        private Long interval;

        private TaskBuilder() {}

        public TaskBuilder host(final String host) {
            this.host = host;
            return this;
        }

        public TaskBuilder port(final Short port) {
            this.port = port;
            return this;
        }

        public TaskBuilder endpoint(final String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public TaskBuilder interval(final Long interval) {
            this.interval = interval;
            return this;
        }

        public Task build() {
            return new Task(host, port, endpoint, interval);
        }
    }

    /**
     * Create a clone of a task. The clone has the same config as the original task, with the Task state reset.
     * @param t The task to clone
     * @return The cloned Task
     */
    public static Task clone(final Task t) {
        return new TaskBuilder()
                .host(t.host)
                .port(t.port)
                .endpoint(t.endpoint)
                .interval(t.interval)
                .build();
    }
}
