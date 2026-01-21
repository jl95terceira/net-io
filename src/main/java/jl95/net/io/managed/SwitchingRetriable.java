package jl95.net.io.managed;

import static jl95.lang.SuperPowers.ifNull;
import static jl95.lang.SuperPowers.sleep;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import jl95.lang.I;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method0;
import jl95.lang.variadic.Method2;
import jl95.net.io.managed.util.Defaults;

public abstract class SwitchingRetriable<O> extends Retriable<O, Integer> {

    public static class NoConnectionProvidersException extends RuntimeException {}
    public static class NoMoreReswitchesException      extends RuntimeException {}

    private final Iterator<Integer>            indexSwitcher;
    private final ScheduledExecutorService     pool;
    private       Integer                      curIndex;
    private       Function1<Boolean, Integer>  reswitchPredicate = null;
    private       Function0<Integer>           reswitchTimeoutMs = null;
    private       Method2<Integer, Integer>    reswitchHandler   = null;

    private void reswitch(Integer reswitchesSoFar) {
        switchToNext();
        if (!ifNull(reswitchPredicate, i -> true).apply(reswitchesSoFar)) {
            throw new NoMoreReswitchesException();
        }
        sleep(ifNull(reswitchTimeoutMs, Defaults.reswitchTimeoutMs).apply());
    }

    public SwitchingRetriable(Iterable<Function0<O>> connectionFunctions) {

        var connectionFunctionsList = I.of(connectionFunctions).toList();
        pool = new ScheduledThreadPoolExecutor(connectionFunctionsList.size());
        if (connectionFunctionsList.isEmpty()) {
            throw new NoConnectionProvidersException();
        }
        indexSwitcher = I.range(connectionFunctionsList.size()).cycle().iterator();
        curIndex = indexSwitcher.next();
        for (var t: I.of(connectionFunctionsList).enumer()) {
            var i    = t.a1;
            var key = t.a2;
            put(i, key);
        }
    }

    @Override protected final Integer next() {
        var reswitchesSoFar = 0;
        do {
            if (isConnected(curIndex)) {
                return curIndex;
            } else {
                reswitch(reswitchesSoFar);
                reswitchesSoFar += 1;
            }
        } while (ifNull(reswitchPredicate, i -> true).apply(reswitchesSoFar));
        throw new NoMoreReswitchesException();
    }
    @Override protected final void    onException(Integer key, Exception ex) {
        reset(key);
        reswitch(0);
    }
    @Override protected final void    retryExecute(Method0 f) {
        pool.execute(f::accept);
    }

    public final void switchToNext() {
        var peerPreviousAddress = curIndex;
        curIndex = indexSwitcher.next();
        ifNull(reswitchHandler, (addr_prev,addr_new) -> {}).accept(peerPreviousAddress, curIndex);
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
