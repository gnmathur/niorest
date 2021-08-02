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

package com.gmathur.niorest.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.util.function.Function;

/**
 * A Timer that calculates the timer interval and next dispatch time according to the following exponential backoff
 * algorithm intervalMs = (2^nConnectionRetries + randomMs)
 *
 * The calculated timer value is checked a provided max backoff time and is set to that value in case the calculated
 * value matches or exceeded it.
 */
public class TimerBackoff extends Timer {
    private static final Logger logger = LoggerFactory.getLogger(TimerBackoff.class.getCanonicalName());

    public TimerBackoff(final Object association,
                        final String sourceDesc,
                        final Integer nRetries,
                        final Long maxBackoffTimeMs,
                        final Function<SelectionKey, Integer> timerFn,
                        final SelectionKey key) {
        super(association, sourceDesc, 0L, timerFn, key);
        Long interval = ((1L << nRetries) * 1000L) + ((long)(Math.random() * 1000L));
        if (interval > maxBackoffTimeMs) {
            interval = maxBackoffTimeMs;
        }
        this.intervalInMs = interval;
        this.nextDispatchMs = System.currentTimeMillis() + interval;
        logger.info("Created a backoff timer for {} ms", interval);
    }

    @Override
    public void fn() {
        timerFn.apply(selectionKey);
    }
}
