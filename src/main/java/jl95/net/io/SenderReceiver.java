package jl95.net.io;

import static jl95.lang.SuperPowers.constant;

import java.net.Socket;

import jl95.lang.variadic.Function0;
import jl95.net.io.managed.ManagedIos;

public interface SenderReceiver<S, R> {

    Sender<S> getSender  ();
    Receiver<R> getReceiver();

    static <S, R> SenderReceiver<S, R> of        (Function0<Sender  <S>> s,
                                                  Function0<Receiver<R>> r) {
        return new SenderReceiver<>() {
            @Override
            public Sender<S> getSender() {
                return s.apply();
            }

            @Override
            public Receiver<R> getReceiver() {
                return r.apply();
            }
        };
    }
    static <S, R> SenderReceiver<S, R> ofConstant(Sender  <S> s,
                                                  Receiver<R> r) {
        return of(constant(s), constant(r));
    }
    static SenderReceiver<byte[], byte[]> fromIo        (Ios ios) {
        return ofConstant(BytesIStreamSender.of(ios.getOutputStream()), BytesIStreamReceiver.of(ios.getInputStream()));
    }
    static SenderReceiver<byte[], byte[]> fromSocket    (Socket socket) {
        return fromIo(Ios.fromSocket(socket));
    }
    static SenderReceiver<byte[], byte[]> fromSocketLazy(Socket socket) {
        return fromIo(Ios.fromSocketLazy(socket));
    }
    static SenderReceiver<byte[], byte[]> fromManagedIo (ManagedIos ios) {
        return ofConstant(BytesIStreamSender.of(ios), BytesIStreamReceiver.of(ios));
    }
}
