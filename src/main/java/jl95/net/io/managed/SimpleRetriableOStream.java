package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;
import java.io.OutputStream;

import jl95.lang.variadic.Function0;

public class SimpleRetriableOStream extends SimpleRetriable<OutputStream> {

    protected SimpleRetriableOStream(Function0<OutputStream> oSupplier) {
        super(oSupplier);
    }

    @Override
    protected void close(OutputStream o) {
        uncheck(o::close);
    }
}
