package com.gmathur.niorest;

import java.nio.channels.SelectionKey;

public abstract class ReactorAdaptor {
    abstract public void connect(SelectionKey key,  Object adaptee);

    abstract public void  connectCb(SelectionKey key, Object adaptee);

}
