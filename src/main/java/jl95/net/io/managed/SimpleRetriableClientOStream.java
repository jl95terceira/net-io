package jl95.net.io.managed;

import java.net.InetSocketAddress;

import jl95.net.io.Util;

public class SimpleRetriableClientOStream extends SimpleRetriableOStream {

    public static SimpleRetriableClientOStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableClientOStream(peerAddress);
    }

    protected SimpleRetriableClientOStream(InetSocketAddress peerAddress) {

        super(() -> Util.getIoAsClient(peerAddress).getOutputStream());
    }
}
