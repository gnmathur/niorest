/**
Copyright (c) 2021 Gaurav Mathur

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.gmathur.niorest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Reactor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(Reactor.class.getCanonicalName());

    private final Selector clientSelector;
    private final PriorityQueue<Timer> timedTasks = new PriorityQueue<>((o1, o2) -> (int)(o1.getIntervalMs() - o2.getIntervalMs()));
    private final Long DEFAULT_REACTOR_TO_MS = 5000L;

    private boolean keepRunning = true;

    // Todo better handle exceptions in constructor
    public Reactor() throws IOException {
        this.clientSelector = Selector.open();
    }

    private boolean keepRunning() { return keepRunning; }
    public void stop() { this.keepRunning = false; }

    public void addTask(final Task task) throws IOException {
        // Create a non-blocking client channel
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

        SelectionKey clientKey = clientChannel.register(clientSelector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        clientChannel.connect(new InetSocketAddress(task.getHost(), task.getPort()));

        clientKey.attach(task);
        timedTasks.add(new Timer(clientKey, task.getInterval()));
        logger.info("Client {} registered", task.getClientId());
    }

    private void write(SocketChannel ch, ByteBuffer buffer, byte[] request) {
        buffer.clear();
        buffer.put(request);
        buffer.flip();
        while (buffer.hasRemaining()) {
            try {
                int written = ch.write(buffer);
                assert(written > 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        buffer.clear();
    }

    private boolean read(SocketChannel ch, ByteBuffer buffer) {
        boolean didRead = false;
        buffer.clear();
        try {
            int readBytes = ch.read(buffer); // ToDo handle exceptions
            if (readBytes != -1) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, readBytes, StandardCharsets.UTF_8);
                buffer.clear();
                didRead = true;
                logger.info(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return didRead;
    }

    /**
     * Core NIO request/response loop
     *
     * @throws IOException
     */
    private void once() throws IOException {
        // ToDo null check
        final Timer top = timedTasks.peek();

        long selectTimeout = (top != null) ? top.getIntervalMs() : DEFAULT_REACTOR_TO_MS;

        clientSelector.select(selectTimeout);
        Long now = System.currentTimeMillis();

        Set<SelectionKey> selectedKeys = clientSelector.selectedKeys();
        for (SelectionKey key: selectedKeys) {
            SocketChannel ch = (SocketChannel) key.channel();
            Task handler = (Task) key.attachment();

            if (key.isConnectable()) {
                // ToDo handle connect exceptions here
                boolean isConnected = ch.finishConnect();
                assert(isConnected);
                key.interestOps(SelectionKey.OP_WRITE & ~SelectionKey.OP_CONNECT);
            } else if (key.isWritable()) {
                write(ch, handler.getBuffer(), handler.getRequestBytes());
                key.interestOps(SelectionKey.OP_READ & ~SelectionKey.OP_WRITE) ;
            } else if (key.isReadable()) {
                if (read(ch, handler.getBuffer())) {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_READ) ;
                }
            }
        }
        selectedKeys.clear();;

        for (Timer t : timedTasks) {
            if ((now - t.getLastFiredAt()) > t.getIntervalMs()) {
                t.getKey().interestOps(SelectionKey.OP_WRITE);
                t.setLastFiredAt(now);
            }
        }
    }

    @Override
    public void run() {
        logger.info("Starting the core reactor");
        while (keepRunning()) {
            try {
                once();
            } catch (IOException e) {
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
