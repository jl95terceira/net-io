package jl95.net.io.managed;

import java.net.InetSocketAddress;

import jl95.net.io.Util;

public class SimpleRetriableClientIStream extends SimpleRetriableIStream {

    public static SimpleRetriableClientIStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableClientIStream(peerAddress);
    }

    protected SimpleRetriableClientIStream(InetSocketAddress peerAddress) {

        super(() -> Util.getIoAsClient(peerAddress).getInputStream());
    }
}
