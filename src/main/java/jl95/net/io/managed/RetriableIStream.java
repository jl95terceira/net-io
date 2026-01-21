package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;

public abstract class RetriableIStream<K> extends Retriable<InputStream, K> {

    @Override
    protected void close(InputStream is) {
        uncheck(is::close);
    }
}
