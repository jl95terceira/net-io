package jl95.net.io;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;

public interface Managed<T> extends Closeable {

    <U> U doWith(Function1<U, T>  f);

    default void doWith(Method1<T> f) {
        this.<Void>doWith((T in) -> {
            f.accept(in);
            return null;
        });
    }
    default T get() {
        return doWith((T is) -> is);
    }
}
