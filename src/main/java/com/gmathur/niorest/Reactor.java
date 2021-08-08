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

import com.gmathur.niorest.reactees.Reactee;
import com.gmathur.niorest.timer.TimerDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Reactor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Reactor.class.getCanonicalName());
    private final Selector clientSelector;
    private final TimerDb timerDb = TimerDb.get();

    private boolean keepRunning = true;

    // Todo better handle exceptions in constructor
    public Reactor() throws IOException {
        this.clientSelector = Selector.open();
    }

    private boolean keepRunning() { return keepRunning; }
    public void stop() { this.keepRunning = false; }

    public SelectionKey addReactee(final Reactee reactee) throws IOException {
        // Create a non-blocking client channel and set its options
        final SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

        // Register the channwith the selector and set its interest ops to OP_CONNECT to enable it to initiate
        // a connection
        // ToDo can't we register without expressing interest
        final SelectionKey clientKey = clientChannel.register(clientSelector, 0);
        // Attach the task to the key
        clientKey.attach(reactee);

        logger.info("Client {} registered", reactee.getId());
        return clientKey;
    }

    public void removeTask(final Reactee task) {

    }

    public static boolean write(SocketChannel ch, ByteBuffer buffer, byte[] request) {
        boolean didWrite = true;

        buffer.clear();
        buffer.put(request);
        buffer.flip();
        try {
            while (buffer.hasRemaining()) {
                int written = ch.write(buffer);
                assert (written > 0);
            }
        } catch (IOException e) {
            logger.error("Failed to write (err: " + e.getMessage() + ")");
            didWrite = false;
        }
        buffer.clear();
        return didWrite;
    }

    /**
     *
     * @param reactee
     * @param key
     * @return true whether reactee read all it expected to. That is a signal to the reactor to start expecting
     * bytes
     */
    private boolean read(final Reactee reactee, final SelectionKey key) {
        final SocketChannel ch = (SocketChannel) key.channel();
        ByteBuffer buffer = reactee.getBuffer();
        Boolean readMore = false;

        buffer.clear();
        try {
            int readBytes = ch.read(buffer); // ToDo handle exceptions
            logger.debug("read " + readBytes + " bytes");
            if (readBytes != -1) {
                buffer.flip();
                readMore = !reactee.readCb(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readMore;
    }

    /**
     * Core NIO request/response loop
     *
     * @throws IOException
     */
    private void once() throws IOException {
        // ToDo null check
        clientSelector.select(timerDb.smallestTimer());

        Set<SelectionKey> selectedKeys = clientSelector.selectedKeys();
        for (SelectionKey key: selectedKeys) {
            Reactee reactee = (Reactee) key.attachment();

            if (key.isConnectable()) {
                ReactorOps.connectCb(reactee, key, this);
            } else if (key.isWritable()) {
                ReactorOps.writeCb(reactee, key, this);
            } else if (key.isReadable()) {
                if (!read(reactee, key)) {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                }
            }
        }
        selectedKeys.clear();;

        timerDb.dispatchExpiredTimers();
    }

    @Override
    public void run() {
        logger.info("Starting the core reactor");
        while (keepRunning()) {
            try {
                once();
            } catch (IOException e) {
                e.printStackTrace();
                logger.info(String.format("Error starting reactor (err: %s)", e.getMessage()));
                keepRunning = false;
            }
        }
        try {
            clientSelector.close();
        } catch (IOException e) {
            logger.error("Error closing the client select (err: {})", e.getMessage());
        }
        logger.info("Closed reactor");
    }
}
