package jl95.net.io.managed;

import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.ManagedIOStream;

public abstract class RetriableIOStream<K> extends CloseableRetriable<CloseableIOStreamSupplier, K> implements ManagedIOStream {
}
