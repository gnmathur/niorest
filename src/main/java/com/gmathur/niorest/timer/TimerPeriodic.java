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

public class TimerPeriodic extends Timer {
    private static final Logger logger = LoggerFactory.getLogger(TimerPeriodic.class.getCanonicalName());

    public TimerPeriodic(final Object associationKey,
                         final String sourceDesc,
                         final Long intervalInMs,
                         final Function<SelectionKey, Integer> timerFn,
                         final SelectionKey key) {
        super(associationKey, sourceDesc, intervalInMs, timerFn, key);
        logger.info("Created a periodic timer for {} ms", intervalInMs);
    }

    @Override
    public void fn() {
        timerFn.apply(selectionKey);
        TimerDb.get().register(new TimerPeriodic(association, sourceDesc, intervalInMs, timerFn, selectionKey));
    }
}
