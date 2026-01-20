package jl95.net.io;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;

public interface ManagedIStreamSupplier extends Closeable {

    <T> T withInput (Function1<T, InputStream>  f);

    default void withInput (Method1<InputStream> f) {
        this.<Void>withInput(in -> {
            f.accept(in);
            return null;
        });
    }
    default InputStream getInputStream() {
        return withInput(is -> is);
    }

    static ManagedIStreamSupplier of(InputStream is) {
        return new ManagedIStreamSupplier() {

            private boolean isClosed = false;
            @Override
            public synchronized void close() {
                if (!isClosed) {
                    uncheck(is::close);
                    isClosed = true;
                }
            }
            @Override
            public <T> T withInput(Function1<T, InputStream> f) {
                return f.apply(is);
            }
        };
    }
}
