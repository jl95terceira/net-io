package jl95.net.io.managed;

import static jl95.lang.SuperPowers.constant;
import static jl95.lang.SuperPowers.ifNull;
import static jl95.lang.SuperPowers.method;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method0;
import jl95.lang.variadic.Method2;
import jl95.net.io.Closeable;
import jl95.net.io.Managed;
import jl95.net.io.managed.util.Defaults;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

public abstract class Retriable<O, K> implements Managed<O>, Closeable {

    private final StrictMap<K, Function0<O>>
                                iosSupplierMap = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, O>
                                iosMap = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, Object>
                                iosReconnectSyncMap = strict(new ConcurrentHashMap<>());
    private final StrictSet<K>  reconnectingSet = strict(new HashSet<>());
    private       Boolean       toStopRetries       = false;
    private       Integer       retriesSoFar        = 0;
    // settings
    private       Function0<Integer>          retryTimeoutMs;
    private       Function1<Boolean, Integer> retryPredicate;
    private       Function0<Integer>          reconnectTimeoutMs;
    private       Method2<K, O>               onConnection;

    private <T> T   retried    (Function1<T, O> f) {
        while (!toStopRetries) {
            var key = next();
            O ios;
            var gotIosError  = false;
            var deferOnError = method(() -> {});
            try {
                if (iosMap.containsKey(key)) {
                    deferOnError = ioKeyRemover(key);
                    ios = iosMap.get(key);
                }
                else {
                    ios = iosSupplierMap.get(key).apply();
                    deferOnError = ioKeyRemover(key);
                }
                return f.apply(ios);
            }
            catch (Exception ex) {
                gotIosError = true;
                deferOnError.accept();
                onException(key, ex);
                if (!ifNull(retryPredicate, n -> true).apply(retriesSoFar)) {
                    throw new NoMoreRetriesException();
                }
                sleep(ifNull(retryTimeoutMs, Defaults.retryTimeoutMs).apply());
                retriesSoFar += 1;
            }
            if (gotIosError) continue;
            retriesSoFar = 0;
        }
        throw new StopRetriesException();
    }
    private Method0 ioKeyRemover(K key) {
        return () -> {
            iosMap.remove(key);
        };
    }

    public Retriable() {

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopRetries));
    }

    protected abstract K    next();
    protected abstract void onException(K key, Exception ex);
    protected abstract void close(O ios);
    protected          void retryExecute(Method0 f) {
        new Thread(f::accept).start();
    }
    protected          void onToStopRetries() {}

    synchronized
    public final void           put               (K key, Function0<O> iosSupplier) {
        iosSupplierMap.put(key, iosSupplier);
        iosReconnectSyncMap.put(key, new Object());
        try {
            var ios = iosSupplierMap.get(key).apply();
            ifNull(onConnection, (key_, ios_) -> {}).accept(key, ios);
            iosMap.put(key, ios);
        }
        catch (Exception ex) {
            reset(key);
        }
    }
    public final O
                                get               (K key) {
        return iosMap.get(key);
    }
    public final Iterable<O>
                                getAll            () {return iosMap.values();}
    @Override
    synchronized
    public final void           close             () {
        stopRetries();
        for (var key: iosMap.keySet()) {
            var sync = getReconnectSync(key);
            if (sync != null) {
                synchronized (getReconnectSync(key)) {/* wait stop */}
            }
        }
        for (var ios: getAll()) {
            close(ios);
        }
    }
    synchronized
    public final void           forget            (K key) {
        iosReconnectSyncMap.remove(key);
        if (iosMap.containsKey(key)) {
            try {
                close(iosMap.get(key));
            }
            catch (Exception ex) {/* who cares */}
            iosMap.remove(key);
        }
    }
    synchronized
    public final void           reset             (K key) {
        if (reconnectingSet.contains(key)) /* already reconnecting */ {
            return;
        }
        forget(key);
        iosReconnectSyncMap.put(key, new Object());
        reconnectingSet.add(key);
        retryExecute(() -> {
            synchronized (getReconnectSync(key)) {
                while (!toStopRetries()) {
                    O ios;
                    try {
                        ios = iosSupplierMap.get(key).apply();
                        try {
                            ifNull(onConnection, (key_, ios_) -> {}).accept(key, ios);
                        }
                        catch (Exception ex) {
                            try {
                                close(ios);
                            }
                            catch (Exception ex_) {/* the show must go on */}
                        }
                        iosMap.put(key, ios);
                    } catch (Exception ex) {
                        sleep(ifNull(reconnectTimeoutMs, Defaults.reconnectTimeoutMs).apply());
                        continue;
                    }
                    reconnectingSet.remove(key);
                    break;
                }
            }
        });
    }
    synchronized
    public final Object         getReconnectSync  (K key) {return iosReconnectSyncMap.get(key);}
    public final Boolean        isAvailable       (K key) {
        return iosMap.containsKey(key);
    }
    public final void           setOnConnection   (Method2<K, O> m) {
        onConnection = m;
    }
    public final void           setRetryTimeoutMs (Function0<Integer> t) { this.retryTimeoutMs = t; }
    public final void           setRetryTimeoutMs (Integer            t) { setRetryTimeoutMs(constant(t)); }
    public final void           setRetryPredicate (Function1<Boolean, Integer> f) { this.retryPredicate = f; }
    public final void           setRetryLimit     (Integer max) { setRetryPredicate(n -> n < max); }
    public final Integer        getRetriesSoFar   () {return retriesSoFar;}
    public final void           stopRetries       () {
        toStopRetries = true;
        onToStopRetries();
    }
    public final Boolean        toStopRetries     () { return toStopRetries; }
    public final void           setReconnectTimeoutMs(Function0<Integer> t) { this.reconnectTimeoutMs = t; }
    public final void           setReconnectTimeoutMs(Integer            t) { setReconnectTimeoutMs(constant(t)); }

    @Override
    public <U> U doWith(Function1<U, O> f) {
        return retried(f);
    }

    public static class NoMoreRetriesException extends RuntimeException {}
    public static class StopRetriesException   extends RuntimeException {}
}
