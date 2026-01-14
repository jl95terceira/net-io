package jl95.net.io;

import static jl95.lang.SuperPowers.constant;
import static jl95.lang.SuperPowers.uncheck;
import static jl95.lang.SuperPowers.unchecked;

import java.io.*;
import java.net.Socket;

import jl95.lang.variadic.Function0;
import jl95.net.io.util.InputStreams;
import jl95.net.io.util.OutputStreams;

public interface IOStreamSupplier {

    InputStream  getInputStream ();
    OutputStream getOutputStream();

    static IOStreamSupplier of        (Function0<InputStream>  in,
                          Function0<OutputStream> out) {
        return new IOStreamSupplier() {
            @Override public InputStream  getInputStream () {
                return in .apply();
            }
            @Override public OutputStream getOutputStream() {
                return out.apply();
            }
        };
    }
    static IOStreamSupplier ofConstant(InputStream  in,
                          OutputStream out) {
        return of(constant(in), constant(out));
    }
    static IOStreamSupplier fromSocket    (Socket socket) {

        return IOStreamSupplier.ofConstant(uncheck(socket::getInputStream), uncheck(socket::getOutputStream));
    }
    static IOStreamSupplier fromSocketLazy(Socket socket) {

        return IOStreamSupplier.ofConstant(InputStreams.getLazy(unchecked(socket::getInputStream)), OutputStreams.getLazy(unchecked(socket::getOutputStream)));
    }
}
