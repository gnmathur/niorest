package com.gmathur.niorest.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.util.function.Function;

public class TimerBackoff extends Timer {
    private static final Logger logger = LoggerFactory.getLogger(TimerBackoff.class.getCanonicalName());

    private final Long nRetries;

    public TimerBackoff(final Long intervalInMs, final Function<SelectionKey, Integer> timerFn, final SelectionKey key, final Long nRetries) {
        super(intervalInMs, timerFn, key);
        this.nRetries = nRetries;
    }

    @Override
    public void fn() {
        Integer result = timerFn.apply(selectionKey);
        assert (nRetries >= 0);
        if (result == -1 && nRetries > 0) {
            TimerDb.get().register(new TimerBackoff(intervalInMs, timerFn, selectionKey, nRetries-1));
        }
        if (nRetries == 0) {
            logger.warn("No more retries");
        }
    }
}
