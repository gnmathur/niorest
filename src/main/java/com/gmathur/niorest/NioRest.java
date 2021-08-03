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

import com.gmathur.niorest.reactees.Task;

import java.io.IOException;

/**
 * Application entry point
 */
public class NioRest {
    public static void main(String[] args) throws IOException {
        final Reactor reactor = new Reactor();

        Runtime.getRuntime().addShutdownHook(new Thread(reactor::stop));
        final Task c1 = Task.newBuilder()
                .host("localhost").port((short)8080).endpoint("/health?name=Gaurav").interval(5000L)
                .build();
        ReactorOps.register(c1, reactor);

        final Task c2 = Task.newBuilder()
                .host("localhost").port((short)8080).endpoint("/ip").interval(10000L)
                .build();
        //ReactorOps.register(c2, reactor);

        final Task c3 = Task.newBuilder()
                .host("localhost").port((short)8080).endpoint("/book?name=donquixote").interval(5 * 60 * 1000L)
                .build();
        // ReactorOps.register(c3, reactor);

        final Task c4 = Task.newBuilder()
                .host("localhost").port((short)8080).endpoint("/poem").interval(5000L)
                .build();
        ReactorOps.register(c4, reactor);

        final Thread r = new Thread(reactor);
        r.start();

        try {
            r.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
