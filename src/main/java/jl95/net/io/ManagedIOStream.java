package jl95.net.io;

import static jl95.lang.SuperPowers.function;
import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;
import java.io.OutputStream;

import jl95.lang.variadic.*;

public interface ManagedIOStream extends ManagedIStream, ManagedOStream {

    <T> T withIo(Function2<T, InputStream, OutputStream> f);

    default void withIo(Method2<InputStream, OutputStream> f) {

        this.<Void>withIo((in,out) -> {
            f.accept(in,out);
            return null;
        });
    }
    default CloseableIOStreamSupplier getIo() { return withIo(function((i,o) -> CloseableIOStreamSupplier.of(i,o))); }
    @Override
    default void close() {
        withIo((is, os) -> {
            uncheck(is::close);
            uncheck(os::close);
            return null;
        });
    }

    static ManagedIOStream of(IOStreamSupplier ios) {
        return new ManagedIOStream() {

            private boolean isClosedInput = false;
            private boolean isClosedOutput = false;
            @Override
            public void close() {
                if (!isClosedInput) {
                    uncheck(ios.getInputStream ()::close);
                    isClosedInput = true;
                }
                if (!isClosedOutput) {
                    uncheck(ios.getOutputStream()::close);
                    isClosedOutput = true;
                }
            }
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
        };
    }
}
