package jl95.net.io.managed;

import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

import java.net.InetSocketAddress;

public class SimpleRetriableClientIOStream extends SimpleRetriableIOStream {

    public static SimpleRetriableClientIOStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableClientIOStream(peerAddress);
    }

    protected SimpleRetriableClientIOStream(InetSocketAddress peerAddress) {

        super(() -> Util.getIoAsClient(peerAddress));
    }
}
