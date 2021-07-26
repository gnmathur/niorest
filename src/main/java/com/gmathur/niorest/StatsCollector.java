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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Statics collector singleton
 */
public class StatsCollector {
    private static final Logger logger = LoggerFactory.getLogger(StatsCollector.class.getCanonicalName());

    private class ClientStats {
        public Integer totalRequest;
        public Integer failedRequests;
        public Integer successfulRequests;
    }

    private static StatsCollector instance = null;
    private static final Object creationLock = new Object();
    private final Object updateLock = new Object();
    private Long totalRequests;
    private Long totalRequestsFailed;
    private final Map<String, ClientStats> clientStatsMap = new HashMap<>();

    private StatsCollector() { }

    public static StatsCollector get() {
        synchronized (creationLock) {
            if (instance == null) {
                instance = new StatsCollector();
            }
            return instance;
        }
    }

    public void incrementRequest(final String clientId) {
        synchronized (updateLock) {
            totalRequests++;
            ClientStats s = clientStatsMap.getOrDefault(clientId, new ClientStats());
            s.totalRequest++;
            s.successfulRequests++;
        }
    }

    public void printer() {
        synchronized (updateLock) {
            logger.info("total requests: " + totalRequests);
            logger.info("total clients: " + clientStatsMap.size());
        }
    }
}
