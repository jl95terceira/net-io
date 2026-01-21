package jl95.net.io;

import static jl95.lang.SuperPowers.function;
import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;
import java.io.OutputStream;

import jl95.lang.variadic.*;
import jl95.net.io.managed.SwitchingRetriableIOStream;
import jl95.net.io.util.InputStreams;
import jl95.net.io.util.OutputStreams;

public interface ManagedIOStream extends Managed<CloseableIOStreamSupplier> {

    <U> U doWith(Function1<U, CloseableIOStreamSupplier> f);

    default void close() {
        get().close();
    }
    default Managed<InputStream> input() {
        return new Managed<>() {
            @Override
            public void close() {
                /* not supported */
            }
            @Override
            public <U> U doWith(Function1<U, InputStream> f) {
                return ManagedIOStream.this.doWith((CloseableIOStreamSupplier ios) -> f.apply(ios.getInputStream()));
            }
        };
    }
    default Managed<OutputStream> output() {
        return new Managed<>() {
            @Override
            public <U> U doWith(Function1<U, OutputStream> f) {
                return ManagedIOStream.this.doWith((CloseableIOStreamSupplier ios) -> f.apply(ios.getOutputStream()));
            }
            @Override
            public void close() {
                /* not supported */
            }
        };
    }

    static ManagedIOStream of(IOStreamSupplier ios) {
        var cios = CloseableIOStreamSupplier.of(ios);
        return new ManagedIOStream() {
            @Override
            public void close() {
                cios.close();
            }
            @Override
            public <U> U doWith(Function1<U, CloseableIOStreamSupplier> f) {
                return f.apply(cios);
            }
        };
    }
}
