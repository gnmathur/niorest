package com.gmathur.niorest;

import java.nio.channels.SelectionKey;

public interface ReactorAdaptor {
    public void connect(SelectionKey key,  Object adaptee);

    public void  connectCb(SelectionKey key, Object adaptee);
}
