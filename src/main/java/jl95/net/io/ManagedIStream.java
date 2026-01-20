package jl95.net.io;

import static jl95.lang.SuperPowers.uncheck;
import static jl95.lang.SuperPowers.unchecked;

import java.io.InputStream;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;

public interface ManagedIStream extends Closeable {

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
    default void close() {
        withInput((InputStream is) -> uncheck(is::close));
    }

    static ManagedIStream of(InputStream is) {
        return new ManagedIStream() {

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
