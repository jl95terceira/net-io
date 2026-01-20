package jl95.net.io.util;

import static jl95.lang.SuperPowers.uncheck;
import static jl95.lang.SuperPowers.unchecked;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import jl95.lang.variadic.Function0;

public class OutputStreams {

    public static OutputStream fromSocket    (Socket socket) {

        return uncheck(socket::getOutputStream);
    }
    public static OutputStream fromSocketLazy(Socket socket) {

        return OutputStreams.getLazy(unchecked(socket::getOutputStream));
    }
    public static OutputStream getLazy       (Function0<OutputStream> outSupplier) {
        return new OutputStream() {

            private OutputStream getOutputStream() {return outSupplier.apply();}

            @Override
            public void close() throws IOException { getOutputStream().close(); }

            @Override
            public void flush() throws IOException { getOutputStream().flush(); }

            @Override
            public void write(byte[] b) throws IOException { getOutputStream().write(b); }

            @Override
            public void write(byte[] b, int off, int len) throws IOException { getOutputStream().write(b, off, len); }

            @Override
            public void write(int b) throws IOException {
                getOutputStream().write(b);
            }
        };
    }
}
