package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;
import jl95.net.io.Closeable;
import jl95.net.io.IStreamSupplier;

public interface ManagedIStreamSupplier extends IStreamSupplier, Closeable {

    <T> T withInput (Function1<T, InputStream>  f);

    default void withInput (Method1<InputStream> f) {
        this.<Void>withInput(in -> {
            f.accept(in);
            return null;
        });
    }

    @Override default InputStream getInputStream() {
        return withInput(is -> is);
    }

    static ManagedIStreamSupplier of(InputStream is) {
        return new ManagedIStreamSupplier() {
            @Override
            public void close() {
                uncheck(is::close);
            }
            @Override
            public <T> T withInput(Function1<T, InputStream> f) {
                return f.apply(is);
            }
        };
    }
}
