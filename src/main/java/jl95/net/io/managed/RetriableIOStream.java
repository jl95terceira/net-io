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
import jl95.net.io.managed.util.Defaults;
import jl95.util.*;

public abstract class RetriableIOStream<K> implements ManagedIOStreamSupplier {

    private final StrictMap<K, Function0<CloseableIOStreamSupplier>>
                                                            iosSupplierMapByAddr= strict(new ConcurrentHashMap<>());
    private final StrictMap<K, CloseableIOStreamSupplier>   iosMapByAddr        = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, Object>                      iosReconnectSyncMap = strict(new ConcurrentHashMap<>());
    private final StrictSet<K>                              addrsReconnecting   = strict(new HashSet<>());
    private       Boolean                                   toStopRetries       = false;
    private       Integer                                   retriesSoFar        = 0;
    // settings
    private       Function0<Integer>                        retryTimeoutMs;
    private       Function1<Boolean, Integer>               retryPredicate;
    private       Function0<Integer>                        reconnectTimeoutMs;
    private       Method2<K, CloseableIOStreamSupplier>     onConnection;

    private <T> T   retried    (Function1<T, IOStreamSupplier> f) {
        while (!toStopRetries) {
            var addr = loadAddress();
            CloseableIOStreamSupplier ios = null;
            var gotIosError  = false;
            var deferOnError = method(() -> {});
            try {
                if (iosMapByAddr.containsKey(addr)) {
                    deferOnError = addrRemover(addr);
                    ios = iosMapByAddr.get(addr);
                }
                else {
                    ios = iosSupplierMapByAddr.get(addr).apply();
                    deferOnError = addrRemover(addr);
                }
                return f.apply(ios);
            }
            catch (Exception ex) {
                gotIosError = true;
                deferOnError.accept();
                onIosException(addr, ex);
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
    private Method0 addrRemover(K addr) {
        return () -> {
            iosMapByAddr.remove(addr);
        };
    }

    protected RetriableIOStream() {

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopRetries));
    }

    protected abstract K    loadAddress();
    protected abstract void onIosException(K addr, Exception ex);
    protected          void retryExecute(Method0 f) {
        new Thread(f::accept).start();
    }
    protected          void onToStopRetries() {}

    synchronized
    public final void           put               (K addr, Function0<CloseableIOStreamSupplier> iosSupplier) {
        iosSupplierMapByAddr.put(addr, iosSupplier);
        iosReconnectSyncMap.put(addr, new Object());
        try {
            var ios = iosSupplierMapByAddr.get(addr).apply();
            ifNull(onConnection, (addr_, ios_) -> {}).accept(addr, ios);
            iosMapByAddr.put(addr, ios);
        }
        catch (Exception ex) {
            reconnect(addr);
        }
    }
    public final CloseableIOStreamSupplier
                                get               (K addr) {
        return iosMapByAddr.get(addr);
    }
    public final Iterable<CloseableIOStreamSupplier>
                                getAll            () {return iosMapByAddr.values();}
    @Override
    synchronized
    public final void           close             () {
        stopRetries();
        for (var addr: iosMapByAddr.keySet()) {
            var sync = getReconnectSync(addr);
            if (sync != null) {
                synchronized (getReconnectSync(addr)) {/* wait stop */}
            }
        }
        for (var ios: getAll()) {
            ios.close();
        }
    }
    synchronized
    public final void           forget            (K addr) {
        iosReconnectSyncMap.remove(addr);
        if (iosMapByAddr.containsKey(addr)) {
            try {
                iosMapByAddr.get(addr).close();
            }
            catch (Exception ex) {/* who cares */}
            iosMapByAddr.remove(addr);
        }
    }
    synchronized
    public final void           reconnect         (K addr) {
        if (addrsReconnecting.contains(addr)) /* already reconnecting */ {
            return;
        }
        forget(addr);
        iosReconnectSyncMap.put(addr, new Object());
        addrsReconnecting.add(addr);
        retryExecute(() -> {
            synchronized (getReconnectSync(addr)) {
                while (!toStopRetries()) {
                    CloseableIOStreamSupplier ios;
                    try {
                        ios = iosSupplierMapByAddr.get(addr).apply();
                        try {
                            ifNull(onConnection, (addr_, ios_) -> {}).accept(addr, ios);
                        }
                        catch (Exception ex) {
                            try { ios.close(); }
                            catch (Exception ex_) {/* the show must go on */}
                        }
                        iosMapByAddr.put(addr, ios);
                    } catch (Exception ex) {
                        sleep(ifNull(reconnectTimeoutMs, Defaults.reconnectTimeoutMs).apply());
                        continue;
                    }
                    addrsReconnecting.remove(addr);
                    break;
                }
            }
        });
    }
    synchronized
    public final Object         getReconnectSync  (K addr) {return iosReconnectSyncMap.get(addr);}
    public final Boolean        isConnected       (K addr) {
        return iosMapByAddr.containsKey(addr);
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
