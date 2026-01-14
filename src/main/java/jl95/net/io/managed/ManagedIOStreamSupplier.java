package jl95.net.io.managed;

import static jl95.lang.SuperPowers.function;

import java.io.InputStream;
import java.io.OutputStream;

import jl95.lang.variadic.*;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.IOStreamSupplier;

public interface ManagedIOStreamSupplier extends ManagedIStreamSupplier, ManagedOStreamSupplier {

    <T> T withIo(Function2<T, InputStream, OutputStream> f);

    default void withIo(Method2<InputStream, OutputStream> f) {

        this.<Void>withIo((in,out) -> {
            f.accept(in,out);
            return null;
        });
    }
    default CloseableIOStreamSupplier getIo() { return withIo(function((i,o) -> CloseableIOStreamSupplier.of(i,o))); }

    static ManagedIOStreamSupplier of(IOStreamSupplier ios) { return new ManagedIOStreamSupplier() {

        @Override
        public <T> T withIo(Function2<T, InputStream, OutputStream> f) {
            return f.apply(ios.getInputStream(), ios.getOutputStream());
        }

        @Override
        public <T> T withInput(Function1<T, InputStream> f) {
            return f.apply(ios.getInputStream());
        }

        @Override
        public <T> T withOutput(Function1<T, OutputStream> f) {
            return f.apply(ios.getOutputStream());
        }
    }; }
}
