package jl95.net.io;

import static jl95.lang.SuperPowers.uncheck;

import java.io.OutputStream;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;

public interface ManagedOStreamSupplier extends Closeable {

    <T> T withOutput(Function1<T, OutputStream> f);

    default void withOutput(Method1<OutputStream> f) {

        this.<Void>withOutput(out -> {
            f.accept(out);
            return null;
        });
    }
    default OutputStream getOutputStream() { return withOutput(os -> os); }

    static ManagedOStreamSupplier of(OutputStream os) {
        return new ManagedOStreamSupplier() {

            private boolean isClosed = false;
            @Override
            public void close() {
                if (!isClosed) {
                    uncheck(os::close);
                    isClosed = true;
                }
            }
            @Override
            public <T> T withOutput(Function1<T, OutputStream> f) {
                return f.apply(os);
            }
        };
    }
}
