package jl95.net.io;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method1;

public interface CloseableIOStreamSupplier extends IOStreamSupplier, Closeable {

    static CloseableIOStreamSupplier of(IOStreamSupplier ios) { return of(ios, self -> {}); }
    static CloseableIOStreamSupplier of(InputStream  is,
                           OutputStream os) { return of(IOStreamSupplier.ofConstant(is, os)); }
    static CloseableIOStreamSupplier of(IOStreamSupplier ios,
                           Method1<CloseableIOStreamSupplier> closer) {
        return new CloseableIOStreamSupplier() {
            @Override public InputStream  getInputStream () {
                return ios.getInputStream();
            }
            @Override public OutputStream getOutputStream() {
                return ios.getOutputStream();
            }
            @Override public void         close          () { closer.accept(this); }
        };
    }
    static CloseableIOStreamSupplier of(InputStream  is,
                           OutputStream os,
                           Method1<CloseableIOStreamSupplier> closer) {
        return of(IOStreamSupplier.ofConstant(is, os), closer);
    }

    Function1<Method1<CloseableIOStreamSupplier>, Socket> SOCKET_CLOSER = socket -> self -> {
            uncheck(socket::close);
        };

    static CloseableIOStreamSupplier fromSocket    (Socket socket) {

        var ios = IOStreamSupplier.fromSocketLazy(socket);
        return CloseableIOStreamSupplier.of(ios.getInputStream(), ios.getOutputStream(), SOCKET_CLOSER.apply(socket));
    }
    static CloseableIOStreamSupplier fromSocketLazy(Socket socket) {

        var ios = IOStreamSupplier.fromSocketLazy(socket);
        return CloseableIOStreamSupplier.of(ios.getInputStream(), ios.getOutputStream(), SOCKET_CLOSER.apply(socket));
    }
}
