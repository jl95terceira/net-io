package jl95.net.io.util;

import static jl95.lang.SuperPowers.uncheck;
import static jl95.lang.SuperPowers.unchecked;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.net.io.Managed;

public class InputStreams {

    public static InputStream fromSocket    (Socket socket) {

        return uncheck(socket::getInputStream);
    }
    public static InputStream fromSocketLazy(Socket socket) {

        return InputStreams.getLazy(unchecked(socket::getInputStream));
    }
    public static InputStream getLazy       (Function0<InputStream> inSupplier) {
        return new InputStream() {

            private InputStream getInputStream() { return inSupplier.apply(); }

            @Override
            public int available() throws IOException { return getInputStream().available(); }

            @Override
            public void close() throws IOException { getInputStream().close(); }

            @Override
            public void mark(int readLimit) { getInputStream().mark(readLimit); }

            @Override
            public boolean markSupported() { return getInputStream().markSupported(); }

            @Override
            public int read() throws IOException { return getInputStream().read(); }

            @Override
            public int read(byte[] b) throws IOException { return getInputStream().read(b); }

            @Override
            public int read(byte[] b, int off, int len) throws IOException { return getInputStream().read(b, off, len); }

            @Override
            public void reset() throws IOException { getInputStream().reset(); }

            @Override
            public long skip(long n) throws IOException { return getInputStream().skip(n); }
        };
    }
    public static Managed<InputStream> getSimpleManaged(InputStream is) {
        return new Managed<>() {

            private boolean isClosed = false;

            @Override
            public synchronized void close() {
                if (!isClosed) {
                    uncheck(is::close);
                    isClosed = true;
                }
            }

            @Override
            public <T> T doWith(Function1<T, InputStream> f) {
                return f.apply(is);
            }
        };
    }
}
