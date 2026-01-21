package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.OutputStream;

public abstract class RetriableOStream<K> extends Retriable<OutputStream, K> {

    @Override
    protected void close(OutputStream is) {
        uncheck(is::close);
    }
}
