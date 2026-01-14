package jl95.net.io;

import static jl95.lang.SuperPowers.constant;
import static jl95.lang.SuperPowers.uncheck;
import static jl95.lang.SuperPowers.unchecked;

import java.io.InputStream;
import java.net.Socket;

import jl95.lang.variadic.Function0;
import jl95.net.io.util.InputStreams;

public interface IStreamSupplier {

    InputStream  getInputStream ();

    static IStreamSupplier of            (Function0<InputStream> in) {
        return in::apply;
    }
    static IStreamSupplier ofConstant    (InputStream in) {
        return of(constant(in));
    }
    static IStreamSupplier fromSocket    (Socket socket) {

        return IStreamSupplier.ofConstant(uncheck(socket::getInputStream));
    }
    static IStreamSupplier fromSocketLazy(Socket socket) {

        return IStreamSupplier.ofConstant(InputStreams.getLazy(unchecked(socket::getInputStream)));
    }
}
