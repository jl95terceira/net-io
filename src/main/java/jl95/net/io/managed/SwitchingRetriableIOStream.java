package jl95.net.io.managed;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Method1;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.ManagedIOStream;

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
