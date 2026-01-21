package jl95.net.io.managed;

import static jl95.lang.SuperPowers.ifNull;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Method0;

public abstract class SimpleRetriable<O> extends Retriable<O, Integer> {

    private Method0 reconnectHandler = null;


    protected SimpleRetriable(Function0<O> supplier) {

        put(0, supplier);
    }

    public final void reset() {
        reset(0);
    }

    @Override protected final Integer next() {
        return 0;
    }
    @Override protected final void    onException(Integer key, Exception ex) {

        reset(key);
        ifNull(reconnectHandler, () -> {}).accept();
    }
    @Override protected final void    retryExecute(Method0 f) {
        f.accept();
    }

    public final void setReconnectHandler(Method0 f) {
        reconnectHandler = f;
    }
}
