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

public final class TaskOps {
    private static final Logger logger = LoggerFactory.getLogger(TaskOps.class.getCanonicalName());
    private TaskOps() {}

    public static void register(final Task t, final Reactor r) throws IOException {
        SelectionKey key = r.addTask(t);
        TimerDb.get().register(new TimerOneshot("being connect timer", new Function<SelectionKey, Integer>() {
            @Override
            public Integer apply(SelectionKey key) {
                SocketChannel clientChannel = (SocketChannel) key.channel();
                try {
                    logger.info("Initiating connection to {}:{}", t.getHost(), t.getPort());
                    key.interestOps(SelectionKey.OP_CONNECT);
                    clientChannel.connect(new InetSocketAddress(t.getHost(), t.getPort()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 1; // always
            }
        }, key));
    }

    public static void connectCb(final Task t, final SelectionKey key, final Reactor r) {
        logger.debug("Connect callback for task {}", t.identifier);

        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            clientChannel.finishConnect();
            key.interestOps(key.interestOps() & (~SelectionKey.OP_CONNECT));

            TimerDb.get().register(new TimerOneshot("ready to write timer", selectionKey -> {
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                return 1;
            }, key));

            TimerDb.get().register(new TimerPeriodic("periodic write timer", t.getInterval(),
                    selectionKey -> {
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                        return 1;
                    }, key));
        } catch (IOException e) {
            logger.error("Error connecting to remote endpoint. Retrying connection (err: {})", e.getMessage());
            t.taskState.nConnectionRetries += 1;
            TimerDb.get().register(new TimerBackoff("finish connect failure timer",
                    t.taskState.nConnectionRetries, t.maxBackoffTimeMs, selectionKey -> {
                        try {
                            TaskOps.register(t, r);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        return 1;
                    }, key));
        }
    }

    public static void writeCb(final Task t, final SelectionKey key) {
        final SocketChannel ch = (SocketChannel) key.channel();

        Reactor.write(ch, t.getBuffer(), t.getRequestBytes());
        logger.info("written");
        key.interestOps(SelectionKey.OP_READ & ~SelectionKey.OP_WRITE) ;
    }

    public static void readCb(final Task t, final SelectionKey key) {
        final SocketChannel ch = (SocketChannel) key.channel();

        Reactor.read(ch, t.getBuffer());
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
    }
}
