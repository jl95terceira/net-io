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
import jl95.net.io.Constants;
import jl95.net.io.Managed;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

public abstract class Retriable<T, K> implements Managed<T>, Closeable {

    private final StrictMap<K, Function0<T>>
                                  supplierMap = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, T> objectMap   = strict(new ConcurrentHashMap<>());
    private final StrictMap<K, ReentrantLock>
                                  unsuppliedLockMap = strict(new ConcurrentHashMap<>());
    private final StrictSet<K>    unsuppliedSet     = strict(new HashSet<>());
    private       Boolean         toStopRetries     = false;
    private       Integer         retriesSoFar      = 0;
    // settings
    private       Function0<Integer>          retryTimeoutMs;
    private       Function1<Boolean, Integer> retryPredicate;
    private       Function0<Integer>          supplyTimeoutMs;
    private       Method2<K, T>               onSupplied;

    private <R> R retried    (Function1<R, T> f) {
        while (!toStopRetries) {
            var key = next();
            T object;
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
                final R re = f.apply(object);
                return re;
            }
            catch (Exception ex) {
                gotSupplyError = true;
                deferOnError.accept();
                onException(key, ex);
                if (!ifNull(retryPredicate, n -> true).apply(retriesSoFar)) {
                    throw new NoMoreRetriesException();
                }
                sleep(ifNull(retryTimeoutMs, Constants.RETRY_TIMEOUT_DEFAULT_MS).apply());
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
    protected abstract void close(T object);
    protected          void retryExecute(Method0 f) {
        new Thread(f::accept).start();
    }
    protected          void onToStopRetries() {}

    public synchronized final void put(K key, Function0<T> supplier) {
        supplierMap.put(key, supplier);
        unsuppliedLockMap.put(key, new ReentrantLock());
        try {
            final T object = supplierMap.get(key).apply();
            ifNull(onSupplied, (key_, object_) -> {}).accept(key, object);
            objectMap.put(key, object);
        }
        catch (Exception ex) {
            reset(key);
        }
    }
    public synchronized final void forget(K key) {
        unsuppliedLockMap.remove(key);
        if (objectMap.containsKey(key)) {
            try {
                close(objectMap.get(key));
            }
            catch (Exception ex) {/* who cares */}
            objectMap.remove(key);
        }
    }
    public synchronized final void reset(K key) {
        if (unsuppliedSet.contains(key)) /* already supplying */ {
            return;
        }
        forget(key);
        unsuppliedLockMap.put(key, new ReentrantLock());
        unsuppliedSet.add(key);
        retryExecute(() -> {
            final ReentrantLock sync = getSupplySync(key);
            sync.lock();
            while (!toStopRetries()) {
                final T object;
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
                    sleep(ifNull(supplyTimeoutMs, Constants.SUPPLY_TIMEOUT_DEFAULT_MS).apply());
                    continue;
                }
                unsuppliedSet.remove(key);
                break;
            }
            sync.unlock();
        });
    }
    public synchronized final ReentrantLock getSupplySync(K key) {return unsuppliedLockMap.get(key);}
    public              final T get(K key) {
        return objectMap.get(key);
    }
    public              final Iterable<T>   getAll            () {return objectMap.values();}
    public              final Boolean       isAvailable       (K key) {
        return objectMap.containsKey(key);
    }
    public              final void          setOnSupplied     (Method2<K, T> m) {
        onSupplied = m;
    }
    public              final void          setRetryTimeoutMs (Function0<Integer> t) { this.retryTimeoutMs = t; }
    public              final void          setRetryTimeoutMs (Integer t) { setRetryTimeoutMs(constant(t)); }
    public              final void          setRetryPredicate (Function1<Boolean, Integer> f) { this.retryPredicate = f; }
    public              final void          setRetryLimit     (Integer max) { setRetryPredicate(n -> n < max); }
    public              final Integer       getRetriesSoFar   () {return retriesSoFar;}
    public              final void          stopRetries       () {
        toStopRetries = true;
        onToStopRetries();
    }
    public              final Boolean       toStopRetries     () { return toStopRetries; }
    public              final void          setSupplyTimeoutMs(Function0<Integer> t) { this.supplyTimeoutMs = t; }
    public              final void          setSupplyTimeoutMs(Integer t) { setSupplyTimeoutMs(constant(t)); }

    @Override
    public <U> U doWith(Function1<U, T> f) {
        return retried(f);
    }
    @Override
    public synchronized final void close() {
        stopRetries();
        for (final K key: objectMap.keySet()) {
            final ReentrantLock sync = getSupplySync(key);
            if (sync != null) {
                sync.lock(); /* wait stop */
                sync.unlock();
            }
        }
        for (final T object: getAll()) {
            close(object);
        }
    }

    public static class NoMoreRetriesException extends RuntimeException {}
    public static class StopRetriesException   extends RuntimeException {}
}
