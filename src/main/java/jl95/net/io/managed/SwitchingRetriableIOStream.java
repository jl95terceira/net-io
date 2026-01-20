package jl95.net.io.managed;

import static jl95.lang.SuperPowers.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import jl95.lang.I;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method0;
import jl95.lang.variadic.Method2;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.managed.util.Defaults;
import jl95.util.*;

public abstract class SwitchingRetriableIOStream extends RetriableIOStream<Integer> {

    public static class NoAddressesException      extends RuntimeException {}
    public static class NoMoreReswitchesException extends RuntimeException {}

    private final Iterator<Integer>  peerAddressSwitcher;
    private final ScheduledExecutorService     pool;
    private       Integer                      peerCurAddress;
    private       Function1<Boolean, Integer>  reswitchPredicate = null;
    private       Function0<Integer>           reswitchTimeoutMs = null;
    private       Method2<Integer, Integer>    reswitchHandler   = null;

    private void reswitchIo(Integer reswitchesSoFar) {
        switchAddress();
        if (!ifNull(reswitchPredicate, i -> true).apply(reswitchesSoFar)) {
            throw new NoMoreRetriesException();
        }
        sleep(ifNull(reswitchTimeoutMs, Defaults.reswitchTimeoutMs).apply());
    }

    protected SwitchingRetriableIOStream(Iterable<Function0<CloseableIOStreamSupplier>> connectionFunctions) {

        var connectionFunctionsList = I.of(connectionFunctions).toList();
        pool = new ScheduledThreadPoolExecutor(connectionFunctionsList.size());
        if (connectionFunctionsList.isEmpty()) {
            throw new NoAddressesException();
        }
        peerAddressSwitcher = I.range(connectionFunctionsList.size()).cycle().iterator();
        peerCurAddress = peerAddressSwitcher.next();
        for (var t: I.of(connectionFunctionsList).enumer()) {
            var i    = t.a1;
            var addr = t.a2;
            put(i, addr);
        }
    }

    @Override protected final Integer loadAddress    () {
        var reswitchesSoFar = 0;
        do {
            if (isConnected(peerCurAddress)) {
                return peerCurAddress;
            } else {
                reswitchIo(reswitchesSoFar);
                reswitchesSoFar += 1;
            }
        } while (ifNull(reswitchPredicate, i -> true).apply(reswitchesSoFar));
        throw new NoMoreReswitchesException();
    }
    @Override protected final void    onIosException (Integer addr, Exception ex) {
        reconnect(addr);
        reswitchIo(0);
    }
    @Override protected final void    retryExecute   (Method0 f) {
        pool.execute(f::accept);
    }

    public final void switchAddress       () {
        var peerPreviousAddress = peerCurAddress;
        peerCurAddress = peerAddressSwitcher.next();
        ifNull(reswitchHandler, (addr_prev,addr_new) -> {}).accept(peerPreviousAddress, peerCurAddress);
    }
    public final void setReswitchPredicate(Function1<Boolean, Integer> f) {
        reswitchPredicate = f;
    }
    public final void setReswitchLimit    (Integer max) {
        setReswitchPredicate(i -> i < max);
    }
    public final void setReswitchTimeoutMs(Function0<Integer>          f) {
        reswitchTimeoutMs = f;
    }
    public final void setReswitchHandler  (Method2<Integer, Integer>   f) {
        reswitchHandler = f;
    }
}
