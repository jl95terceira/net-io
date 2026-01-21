package jl95.net.io.managed;

import static jl95.lang.SuperPowers.constant;
import static jl95.lang.SuperPowers.ifNull;
import static jl95.lang.SuperPowers.method;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;
import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import jl95.lang.variadic.*;
import jl95.net.io.Closeable;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.IOStreamSupplier;
import jl95.net.io.Managed;
import jl95.net.io.ManagedIOStream;
import jl95.net.io.managed.util.Defaults;
import jl95.util.*;

public abstract class RetriableIOStream<K> extends CloseableRetriable<CloseableIOStreamSupplier, K> implements ManagedIOStream {

    @Override
    public Managed<InputStream> input() {
        return new Managed<>() {
            @Override
            public void close() {
                /* not supported */
            }
            @Override
            public <U> U doWith(Function1<U, InputStream> f) {
                return RetriableIOStream.this.doWith((CloseableIOStreamSupplier ios) -> f.apply(ios.getInputStream()));
            }
        };
    }
    @Override
    public Managed<OutputStream> output() {
        return new Managed<>() {
            @Override
            public <U> U doWith(Function1<U, OutputStream> f) {
                return RetriableIOStream.this.doWith((CloseableIOStreamSupplier ios) -> f.apply(ios.getOutputStream()));
            }
            @Override
            public void close() {
                /* not supported */
            }
        };
    }
}
