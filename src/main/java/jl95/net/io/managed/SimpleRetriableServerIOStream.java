package jl95.net.io.managed;

import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

import java.net.InetSocketAddress;

public class SimpleRetriableServerIOStream extends SimpleRetriableIOStream {

    public static SimpleRetriableServerIOStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableServerIOStream(peerAddress);
    }

    private SimpleRetriableServerIOStream(InetSocketAddress peerAddress) {

        super(() -> Util.getIoAsServer(peerAddress));
    }
}
