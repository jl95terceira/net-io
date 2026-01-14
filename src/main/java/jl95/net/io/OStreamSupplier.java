package jl95.net.io;

import static jl95.lang.SuperPowers.constant;
import static jl95.lang.SuperPowers.uncheck;
import static jl95.lang.SuperPowers.unchecked;

import java.io.OutputStream;
import java.net.Socket;

import jl95.lang.variadic.Function0;
import jl95.net.io.util.OutputStreams;

public interface OStreamSupplier {

    OutputStream getOutputStream();

    static OStreamSupplier of            (Function0<OutputStream> out) {
        return out::apply;
    }
    static OStreamSupplier ofConstant    (OutputStream out) {
        return of(constant(out));
    }
    static OStreamSupplier fromSocket    (Socket socket) {

        return OStreamSupplier.ofConstant(uncheck(socket::getOutputStream));
    }
    static OStreamSupplier fromSocketLazy(Socket socket) {

        return OStreamSupplier.ofConstant(OutputStreams.getLazy(unchecked(socket::getOutputStream)));
    }
}
