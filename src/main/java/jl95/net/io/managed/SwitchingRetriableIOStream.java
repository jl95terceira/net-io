package jl95.net.io.managed;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import jl95.lang.I;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Method0;
import jl95.lang.variadic.Method1;
import jl95.lang.variadic.Method2;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Managed;
import jl95.net.io.ManagedIOStream;
import jl95.net.io.managed.util.Defaults;

public class SwitchingRetriableIOStream extends SwitchingRetriable<CloseableIOStreamSupplier> implements ManagedIOStream {

    public SwitchingRetriableIOStream(Iterable<Function0<CloseableIOStreamSupplier>> suppliers) {
        super(suppliers);
    }

    @Override
    public void doWith(Method1<CloseableIOStreamSupplier> f) {
        super.doWith(f);
    }
    @Override
    protected void close(CloseableIOStreamSupplier ios) {
        ios.close();
    }
}
