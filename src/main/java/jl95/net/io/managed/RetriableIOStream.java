package jl95.net.io.managed;

import static jl95.lang.SuperPowers.constant;
import static jl95.lang.SuperPowers.ifNull;
import static jl95.lang.SuperPowers.method;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import jl95.lang.variadic.*;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.IOStreamSupplier;
import jl95.net.io.ManagedIOStreamSupplier;
import jl95.net.io.managed.util.Defaults;
import jl95.util.*;

public abstract class RetriableIOStream<K> implements ManagedIOStreamSupplier {

    private final StrictMap<K, Function0<CloseableIOStreamSupplier>>
                                iosSupplierMap = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, CloseableIOStreamSupplier>
                                iosMap = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, Object>
                                iosReconnectSyncMap = strict(new ConcurrentHashMap<>());
    private final StrictSet<K>  reconnectingSet = strict(new HashSet<>());
    private       Boolean       toStopRetries       = false;
    private       Integer       retriesSoFar        = 0;
    // settings
    private       Function0<Integer>                    retryTimeoutMs;
    private       Function1<Boolean, Integer>           retryPredicate;
    private       Function0<Integer>                    reconnectTimeoutMs;
    private       Method2<K, CloseableIOStreamSupplier> onConnection;

    private <T> T   retried    (Function1<T, IOStreamSupplier> f) {
        while (!toStopRetries) {
            var key = nextIOKey();
            CloseableIOStreamSupplier ios = null;
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
                onIosException(key, ex);
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

    protected RetriableIOStream() {

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopRetries));
    }

    protected abstract K    nextIOKey();
    protected abstract void onIosException(K key, Exception ex);
    protected          void retryExecute(Method0 f) {
        new Thread(f::accept).start();
    }
    protected          void onToStopRetries() {}

    synchronized
    public final void           put               (K key, Function0<CloseableIOStreamSupplier> iosSupplier) {
        iosSupplierMap.put(key, iosSupplier);
        iosReconnectSyncMap.put(key, new Object());
        try {
            var ios = iosSupplierMap.get(key).apply();
            ifNull(onConnection, (key_, ios_) -> {}).accept(key, ios);
            iosMap.put(key, ios);
        }
        catch (Exception ex) {
            reconnect(key);
        }
    }
    public final CloseableIOStreamSupplier
                                get               (K key) {
        return iosMap.get(key);
    }
    public final Iterable<CloseableIOStreamSupplier>
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
            ios.close();
        }
    }
    synchronized
    public final void           forget            (K key) {
        iosReconnectSyncMap.remove(key);
        if (iosMap.containsKey(key)) {
            try {
                iosMap.get(key).close();
            }
            catch (Exception ex) {/* who cares */}
            iosMap.remove(key);
        }
    }
    synchronized
    public final void           reconnect         (K key) {
        if (reconnectingSet.contains(key)) /* already reconnecting */ {
            return;
        }
        forget(key);
        iosReconnectSyncMap.put(key, new Object());
        reconnectingSet.add(key);
        retryExecute(() -> {
            synchronized (getReconnectSync(key)) {
                while (!toStopRetries()) {
                    CloseableIOStreamSupplier ios;
                    try {
                        ios = iosSupplierMap.get(key).apply();
                        try {
                            ifNull(onConnection, (key_, ios_) -> {}).accept(key, ios);
                        }
                        catch (Exception ex) {
                            try { ios.close(); }
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
    public final Boolean        isConnected       (K key) {
        return iosMap.containsKey(key);
    }
    public final void           setOnConnection   (Method2<K, CloseableIOStreamSupplier> m) {
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

    @Override public final <T> T withInput (Function1<T, InputStream>  f) {
        return retried((ios) -> f.apply(ios.getInputStream ()));
    }
    @Override public final <T> T withOutput(Function1<T, OutputStream> f) {
        return retried((ios) -> f.apply(ios.getOutputStream()));
    }
    @Override public final <T> T withIo    (Function2<T, InputStream, OutputStream> f) { return retried((ios) -> f.apply(ios.getInputStream(), ios.getOutputStream())); }

    public static class NoMoreRetriesException extends RuntimeException {}
    public static class StopRetriesException   extends RuntimeException {}
}
