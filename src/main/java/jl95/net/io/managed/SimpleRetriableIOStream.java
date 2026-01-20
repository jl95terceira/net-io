package jl95.net.io.managed;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Method0;
import jl95.net.io.CloseableIOStreamSupplier;

import static jl95.lang.SuperPowers.*;

public class SimpleRetriableIOStream extends RetriableIOStream<Integer> {

    private Method0 reconnectHandler = null;

    protected SimpleRetriableIOStream(Function0<CloseableIOStreamSupplier> iosSupplier) {

        put(0, iosSupplier);
    }

    public final void reconnect() {
        reconnect(0);
    }

    @Override protected final Integer nextIOKey() {
        return 0;
    }
    @Override protected final void    onIosException (Integer key, Exception ex) {

        reconnect(key);
        ifNull(reconnectHandler, () -> {}).accept();
    }
    @Override protected final void    retryExecute   (Method0 f) {
        f.accept();
    }

    public final void setReconnectHandler(Method0 f) {
        reconnectHandler = f;
    }
}
