package jl95.net.io.managed;

import jl95.net.io.Closeable;

public abstract class CloseableRetriable<O extends Closeable, K> extends Retriable<O, K> {

    @Override
    protected void close(O ios) {
        ios.close();
    }
}
