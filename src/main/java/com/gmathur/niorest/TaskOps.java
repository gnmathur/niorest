package com.gmathur.niorest;

import com.gmathur.niorest.timer.TimerBackoff;
import com.gmathur.niorest.timer.TimerDb;
import com.gmathur.niorest.timer.TimerPeriodic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

public final class TaskOps {
    private TaskOps() {}

    public static void register(final Task t, final Reactor r) throws IOException {
        SelectionKey key = r.addTask(t);
        TimerDb.get().register(new TimerBackoff(t.getInterval(), new Function<SelectionKey, Integer>() {
            @Override
            public Integer apply(SelectionKey key) {
                boolean isConnected = false;
                SocketChannel clientChannel = (SocketChannel) key.channel();
                try {
                    clientChannel.connect(new InetSocketAddress(t.getHost(), t.getPort()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!isConnected) return -1;
                return 1;
            }
        }, key, 5L));
    }

    public static void connectCb(final Task t, final SelectionKey key) {
        key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
        timerDb.register(new TimerPeriodic(taskCtx.getInterval(), k -> {
            k.interestOps(SelectionKey.OP_WRITE);
            return null;
        }, key));
    }

    public static void write() {

    }

    public static void read() {

    }
}
