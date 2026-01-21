package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;

import jl95.lang.variadic.Function0;

public class SwitchingRetriableIStream extends SwitchingRetriable<InputStream> {

    public SwitchingRetriableIStream(Iterable<Function0<InputStream>> suppliers) {
        super(suppliers);
    }

    @Override
    protected void close(InputStream ios) {
        uncheck(ios::close);
    }
}
