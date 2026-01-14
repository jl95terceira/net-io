package jl95.net.io.managed;

import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

import java.net.InetSocketAddress;

public class SimpleRetriableServerIOStream extends SimpleRetriableIOStream {

    public static SimpleRetriableServerIOStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableServerIOStream(peerAddress);
    }

    @Override protected CloseableIOStreamSupplier connect(InetSocketAddress addr) {
        return Util.getIoAsServer(addr);
    }

    private SimpleRetriableServerIOStream(InetSocketAddress peerAddress) {

        super(peerAddress);
    }
}
