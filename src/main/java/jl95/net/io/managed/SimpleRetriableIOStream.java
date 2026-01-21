package jl95.net.io.managed;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method0;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Managed;
import jl95.net.io.ManagedIOStream;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.io.OutputStream;

public class SimpleRetriableIOStream extends SimpleRetriable<CloseableIOStreamSupplier> implements ManagedIOStream {

    public SimpleRetriableIOStream(Function0<CloseableIOStreamSupplier> iosSupplier) {
        super(iosSupplier);
    }

    @Override
    protected void close(CloseableIOStreamSupplier ios) {
        ios.close();
    }

    @Override
    public Managed<InputStream> input() {
        return new Managed<>() {
            @Override
            public void close() {
                /* not supported */
            }
            @Override
            public <U> U doWith(Function1<U, InputStream> f) {
                return SimpleRetriableIOStream.this.doWith((CloseableIOStreamSupplier ios) -> f.apply(ios.getInputStream()));
            }
        };
    }
    @Override
    public Managed<OutputStream> output() {
        return new Managed<>() {
            @Override
            public <U> U doWith(Function1<U, OutputStream> f) {
                return SimpleRetriableIOStream.this.doWith((CloseableIOStreamSupplier ios) -> f.apply(ios.getOutputStream()));
            }
            @Override
            public void close() {
                /* not supported */
            }
        };
    }
}
