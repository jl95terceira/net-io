package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;
import java.io.OutputStream;

import jl95.lang.variadic.Function0;

public class SwitchingRetriableOStream extends SwitchingRetriable<OutputStream> {

    public SwitchingRetriableOStream(Iterable<Function0<OutputStream>> suppliers) {
        super(suppliers);
    }

    @Override
    protected void close(OutputStream ios) {
        uncheck(ios::close);
    }
}
