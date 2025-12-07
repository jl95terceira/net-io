package jl95.net.io.managed;

import jl95.lang.variadic.Method0;

import java.net.InetSocketAddress;

import static jl95.lang.SuperPowers.*;

public abstract class SimpleRetriableIos extends RetriableIos {

    private final InetSocketAddress peerAddress;
    private       Method0           reconnectHandler = null;

    protected SimpleRetriableIos(InetSocketAddress peerAddress) {

        this.peerAddress = peerAddress;
        put(peerAddress);
    }

    public final void reconnect() { reconnect(peerAddress); }

    @Override protected final InetSocketAddress loadAddress    () {
        return peerAddress;
    }
    @Override protected final void              onIosException (InetSocketAddress addr, Exception ex) {

        reconnect(addr);
        ifNull(reconnectHandler, () -> {}).accept();
    }
    @Override protected final void              retryExecute   (Method0 f) {
        f.accept();
    }

    public final void setReconnectHandler(Method0 f) {reconnectHandler = f;}
}
