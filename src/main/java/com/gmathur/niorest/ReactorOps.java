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

import com.gmathur.niorest.timer.TimerBackoff;
import com.gmathur.niorest.timer.TimerDb;
import com.gmathur.niorest.timer.TimerOneshot;
import com.gmathur.niorest.timer.TimerPeriodic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

/**
 * Reactor functions. This class acts as an adaptor between the reactor and Tasks
 */
public final class ReactorOps {
    private static final Logger logger = LoggerFactory.getLogger(ReactorOps.class.getCanonicalName());
    private ReactorOps() {}

    public static void register(final Task t, final Reactor r) throws IOException {
        SelectionKey key = r.addTask(t);
        TimerDb.get().register(new TimerOneshot(key, "being connect timer", key1 -> {
            SocketChannel clientChannel = (SocketChannel) key1.channel();
            try {
                logger.info("Initiating connection to {}:{}", t.getHost(), t.getPort());
                key1.interestOps(SelectionKey.OP_CONNECT);
                clientChannel.connect(new InetSocketAddress(t.getHost(), t.getPort()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 1; // always
        }, key));
    }

    /**
     * Clean the Task t from the reactor and re-register it with the
     */
    public static void reRegister(final Task t, final Reactor r, final SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        clientChannel.close();
        key.cancel();
        TimerDb.get().cancelTimers(key);
        register(Task.clone(t), r);
    }

    public static void connectCb(final Task t, final SelectionKey key, final Reactor r) {
        logger.debug("Connect callback for task {}", t.identifier);

        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            clientChannel.finishConnect();
            key.interestOps(key.interestOps() & (~SelectionKey.OP_CONNECT));

            TimerDb.get().register(new TimerOneshot(key, "ready to write timer", selectionKey -> {
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                return 1;
            }, key));

            TimerDb.get().register(new TimerPeriodic(key, "periodic write timer", t.getInterval(),
                    selectionKey -> {
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                        return 1;
                    }, key));
        } catch (IOException e) {
            logger.error("Error connecting to remote endpoint. Retrying connection (err: {})", e.getMessage());
            t.taskState.nConnectionRetries += 1;
            TimerDb.get().register(new TimerBackoff(key, "finish connect failure timer",
                    t.taskState.nConnectionRetries, t.maxBackoffTimeMs, selectionKey -> {
                        try {
                            ReactorOps.register(t, r);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        return 1;
                    }, key));
        }
    }

    public static void writeCb(final Task t, final SelectionKey key, final Reactor r) throws IOException {
        final SocketChannel ch = (SocketChannel) key.channel();

        final boolean didWrite = Reactor.write(ch, t.getBuffer(), t.getRequestBytes());
        if (!didWrite) {
            reRegister(t, r, key);
        } else {
            key.interestOps(SelectionKey.OP_READ & ~SelectionKey.OP_WRITE);
        }
    }

    public static void readCb(final Task t, final SelectionKey key, final Reactor r) {
        final SocketChannel ch = (SocketChannel) key.channel();

        Reactor.read(ch, t.getBuffer());
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
    }
}
