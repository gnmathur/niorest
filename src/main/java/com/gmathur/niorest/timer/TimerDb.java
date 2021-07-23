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

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Singleton class to register and manage timers
 */
public class TimerDb {
    private static TimerDb instance = null;
    private Long smallestIntervalInMs = Long.MAX_VALUE;
    private final PriorityQueue<Timer> timedTasks = new PriorityQueue<>((o1, o2) -> (int)(o1.nextDispatchMs() - o2.nextDispatchMs()));

    private TimerDb() {}

    public synchronized static TimerDb get() {
        if (instance == null)
            instance = new TimerDb();
        return instance;
    }

    public void register(final Timer t) {
        timedTasks.add(t);
        smallestIntervalInMs = (t.intervalInMs() < smallestIntervalInMs) ? t.intervalInMs() : smallestIntervalInMs;
    }

    public Long smallestIntervalInMs() {
        return smallestIntervalInMs;
    }

    public void dispatchExpiredTimers() {
        final Long now = System.currentTimeMillis();
        final List<Timer> expiredTimers = new ArrayList<>();

        for (Timer t: timedTasks) {
            if (now > t.nextDispatchMs())
                expiredTimers.add(t);
        }
        timedTasks.removeAll(expiredTimers);
        expiredTimers.forEach(Timer::fn);
    }
}
