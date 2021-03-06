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

import java.nio.channels.SelectionKey;
import java.util.function.Function;

public abstract class Timer {
    protected Long intervalInMs;
    protected Function<SelectionKey, Integer> timerFn;
    protected Long nextDispatchMs;
    protected final SelectionKey selectionKey;
    protected final String sourceDesc;
    protected final Object association;

    public Timer(final Object association,
                 final String sourceDesc,
                 final Long intervalInMs,
                 final Function<SelectionKey, Integer> timerFn,
                 final SelectionKey selectionKey) {
        this.association = association;
        this.sourceDesc = sourceDesc;
        this.intervalInMs = intervalInMs;
        this.timerFn = timerFn;
        this.selectionKey = selectionKey;
        this.nextDispatchMs = System.currentTimeMillis() + intervalInMs;
    }

    public abstract void fn();

    public Long intervalInMs() {
        return intervalInMs;
    }

    public Long nextDispatchMs() {
        return nextDispatchMs;
    }
}
