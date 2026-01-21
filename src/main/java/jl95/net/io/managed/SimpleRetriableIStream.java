package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;

import jl95.lang.variadic.Function0;

public class SimpleRetriableIStream extends SimpleRetriable<InputStream> {

    protected SimpleRetriableIStream(Function0<InputStream> iSupplier) {
        super(iSupplier);
    }

    @Override
    protected void close(InputStream i) {
        uncheck(i::close);
    }
}
