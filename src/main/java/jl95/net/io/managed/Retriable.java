package jl95.net.io.managed;

import static jl95.lang.SuperPowers.constant;
import static jl95.lang.SuperPowers.ifNull;
import static jl95.lang.SuperPowers.method;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
                                  supplierMap = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, O> objectMap   = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, ReentrantLock>
                                  unsuppliedLockMap = strict(new ConcurrentHashMap<>());
    private final StrictSet<K>    unsuppliedSet     = strict(new HashSet<>());
    private       Boolean         toStopRetries     = false;
    private       Integer         retriesSoFar      = 0;
    // settings
    private       Function0<Integer>          retryTimeoutMs;
    private       Function1<Boolean, Integer> retryPredicate;
    private       Function0<Integer>          supplyTimeoutMs;
    private       Method2<K, O>               onSupplied;

    private <T> T   retried    (Function1<T, O> f) {
        while (!toStopRetries) {
            var key = next();
            O object;
            var gotSupplyError  = false;
            var deferOnError = method(() -> {});
            try {
                if (objectMap.containsKey(key)) {
                    deferOnError = ioKeyRemover(key);
                    object = objectMap.get(key);
                }
                else {
                    object = supplierMap.get(key).apply();
                    deferOnError = ioKeyRemover(key);
                }
                return f.apply(object);
            }
            catch (Exception ex) {
                gotSupplyError = true;
                deferOnError.accept();
                onException(key, ex);
                if (!ifNull(retryPredicate, n -> true).apply(retriesSoFar)) {
                    throw new NoMoreRetriesException();
                }
                sleep(ifNull(retryTimeoutMs, Defaults.retryTimeoutMs).apply());
                retriesSoFar += 1;
            }
            if (gotSupplyError) continue;
            retriesSoFar = 0;
        }
        throw new StopRetriesException();
    }
    private Method0 ioKeyRemover(K key) {
        return () -> {
            objectMap.remove(key);
        };
    }

    public Retriable() {

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopRetries));
    }

    protected abstract K    next();
    protected abstract void onException(K key, Exception ex);
    protected abstract void close(O object);
    protected          void retryExecute(Method0 f) {
        new Thread(f::accept).start();
    }
    protected          void onToStopRetries() {}

    synchronized
    public final void           put               (K key, Function0<O> supplier) {
        supplierMap.put(key, supplier);
        unsuppliedLockMap.put(key, new ReentrantLock());
        try {
            var object = supplierMap.get(key).apply();
            ifNull(onSupplied, (key_, object_) -> {}).accept(key, object);
            objectMap.put(key, object);
        }
        catch (Exception ex) {
            reset(key);
        }
    }
    public final O
                                get               (K key) {
        return objectMap.get(key);
    }
    public final Iterable<O>
                                getAll            () {return objectMap.values();}
    @Override
    synchronized
    public final void           close             () {
        stopRetries();
        for (var key: objectMap.keySet()) {
            var sync = getSupplySync(key);
            if (sync != null) {
                sync.lock(); /* wait stop */
                sync.unlock();
            }
        }
        for (var object: getAll()) {
            close(object);
        }
    }
    synchronized
    public final void           forget            (K key) {
        unsuppliedLockMap.remove(key);
        if (objectMap.containsKey(key)) {
            try {
                close(objectMap.get(key));
            }
            catch (Exception ex) {/* who cares */}
            objectMap.remove(key);
        }
    }
    synchronized
    public final void           reset             (K key) {
        if (unsuppliedSet.contains(key)) /* already supplying */ {
            return;
        }
        forget(key);
        unsuppliedLockMap.put(key, new ReentrantLock());
        unsuppliedSet.add(key);
        retryExecute(() -> {
            var sync = getSupplySync(key);
            sync.lock();
            while (!toStopRetries()) {
                O object;
                try {
                    object = supplierMap.get(key).apply();
                    try {
                        ifNull(onSupplied, (key_, object_) -> {}).accept(key, object);
                    }
                    catch (Exception ex) {
                        try {
                            close(object);
                        }
                        catch (Exception ex_) {/* the show must go on */}
                    }
                    objectMap.put(key, object);
                } catch (Exception ex) {
                    sleep(ifNull(supplyTimeoutMs, Defaults.supplyTimeoutMs).apply());
                    continue;
                }
                unsuppliedSet.remove(key);
                break;
            }
            sync.unlock();
        });
    }
    synchronized
    public final ReentrantLock  getSupplySync     (K key) {return unsuppliedLockMap.get(key);}
    public final Boolean        isAvailable       (K key) {
        return objectMap.containsKey(key);
    }
    public final void           setOnSupplied     (Method2<K, O> m) {
        onSupplied = m;
    }
    public final void           setRetryTimeoutMs (Function0<Integer> t) { this.retryTimeoutMs = t; }
    public final void           setRetryTimeoutMs (Integer t) { setRetryTimeoutMs(constant(t)); }
    public final void           setRetryPredicate (Function1<Boolean, Integer> f) { this.retryPredicate = f; }
    public final void           setRetryLimit     (Integer max) { setRetryPredicate(n -> n < max); }
    public final Integer        getRetriesSoFar   () {return retriesSoFar;}
    public final void           stopRetries       () {
        toStopRetries = true;
        onToStopRetries();
    }
    public final Boolean        toStopRetries     () { return toStopRetries; }
    public final void           setSupplyTimeoutMs(Function0<Integer> t) { this.supplyTimeoutMs = t; }
    public final void           setSupplyTimeoutMs(Integer t) { setSupplyTimeoutMs(constant(t)); }

    @Override
    public <U> U doWith(Function1<U, O> f) {
        return retried(f);
    }

    public static class NoMoreRetriesException extends RuntimeException {}
    public static class StopRetriesException   extends RuntimeException {}
}
