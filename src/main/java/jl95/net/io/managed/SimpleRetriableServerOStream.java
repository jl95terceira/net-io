package jl95.net.io.managed;

import java.net.InetSocketAddress;

import jl95.net.io.Util;

public class SimpleRetriableServerOStream extends SimpleRetriableOStream {

    public static SimpleRetriableServerOStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableServerOStream(peerAddress);
    }

    private SimpleRetriableServerOStream(InetSocketAddress peerAddress) {

        super(() -> Util.getIoAsServer(peerAddress).getOutputStream());
    }
}
