package jl95.net.io;

import java.io.OutputStream;

import jl95.lang.variadic.Function1;

public interface Sender<T> {

    void send(T outgoing);

    default <T2> Sender<T2> adapted(Function1<T, T2> adapterFunction) {

        return new Sender<>() {

            @Override public void send(T2 outgoing) {
                var adaptedOutgoing = adapterFunction.apply(outgoing);
                Sender.this.send(adaptedOutgoing);
            }
        };
    }
}