package com.gmathur.niorest;

import com.gmathur.niorest.http.HttpRequest;
import com.gmathur.niorest.http.HttpRequestType;
import com.gmathur.niorest.http.HttpUserAgent;
import com.gmathur.niorest.http.HttpVersion;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class NioConnect {
    /*
    @Test
    public void basic() {
        try {
            // Create a selector
            Selector clientSelector = Selector.open();

            // Create a non-blocking client channel
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true); // ... set some options

            SelectionKey clientKey = clientChannel.register(clientSelector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            clientChannel.connect(new InetSocketAddress("localhost", 8080));

            clientKey.attach(new ClientHandler());

            while (true) {
                clientSelector.select(1000);

                Set<SelectionKey> selectedKeys = clientSelector.selectedKeys();
                for (SelectionKey key: selectedKeys) {
                    SocketChannel ch = (SocketChannel) key.channel();
                    ClientHandler handler = (ClientHandler) key.attachment();

                    if (key.isConnectable()) {
                        // ToDo handle connect exceptions here
                        boolean isConnected = ch.finishConnect();
                        assert(isConnected);
                        key.interestOps(SelectionKey.OP_WRITE);
                        System.out.println("connected....");
                    } else if (key.isWritable()) {
                        System.out.println("writable");
                        handler.write(ch);
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        System.out.println("readable");
                        handler.read(ch);
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    selectedKeys.clear();;
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     */
}
