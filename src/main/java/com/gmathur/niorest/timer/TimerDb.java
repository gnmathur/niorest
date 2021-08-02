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

import java.util.*;

/**
 * Singleton class to register and manage timers
 */
public class TimerDb {
    private static final Logger logger = LoggerFactory.getLogger(TimerDb.class.getCanonicalName());

    private static TimerDb instance = null;
    private final Long infiniteWaitMs = Long.MAX_VALUE;
    private final PriorityQueue<Timer> allTimedTasks = new PriorityQueue<>((o1, o2) -> (int)(o1.intervalInMs - o2.intervalInMs));
    // An arbitrary key that has an associated list of timers
    private final Map<Object, List<Timer>> associations = new HashMap<>();

    private TimerDb() { }

    public synchronized static TimerDb get() {
        if (instance == null)
            instance = new TimerDb();
        return instance;
    }

    public synchronized void register(final Timer t) {
        Objects.requireNonNull(t, "Illegal attempt to register a null timer");
        allTimedTasks.add(t);
        List<Timer> timersAssociatedWithKey = associations.getOrDefault(t.association, new ArrayList<>());
        timersAssociatedWithKey.add(t);
        associations.put(t.association, timersAssociatedWithKey);
        // ToDo should wakeup the reactor
    }

    public synchronized void cancelTimers(final Object association) {
        List<Timer> timers = associations.get(association);
        allTimedTasks.removeAll(timers);
    }

    public Long smallestTimer() {
        Long smallestTimer = infiniteWaitMs;
        if (allTimedTasks.size() != 0) {
            smallestTimer = allTimedTasks.peek().intervalInMs;
        }
        return smallestTimer;
    }

    public synchronized void dispatchExpiredTimers() {
        final Long now = System.currentTimeMillis();
        final List<Timer> expiredTimers = new ArrayList<>();
        boolean morePossibleExpired = true;

        Timer t = allTimedTasks.peek();

        while (t != null && morePossibleExpired) {
            if (now > t.nextDispatchMs()) {
                expiredTimers.add(t);
                allTimedTasks.poll();
                t = allTimedTasks.peek();
            } else {
                morePossibleExpired = false;
            }
        }
        expiredTimers.forEach(tmr -> logger.info("Executing timer {}", tmr.sourceDesc));
        allTimedTasks.removeAll(expiredTimers);
        expiredTimers.forEach(Timer::fn);
    }
}
