package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.OutputStream;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;
import jl95.net.io.Closeable;
import jl95.net.io.OStreamSupplier;

public interface ManagedOStreamSupplier extends OStreamSupplier, Closeable {

    <T> T withOutput(Function1<T, OutputStream> f);

    default void withOutput(Method1<OutputStream> f) {

        this.<Void>withOutput(out -> {
            f.accept(out);
            return null;
        });
    }

    @Override default OutputStream getOutputStream() { return withOutput(os -> os); }

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
