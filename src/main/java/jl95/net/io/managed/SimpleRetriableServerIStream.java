package jl95.net.io.managed;

import java.net.InetSocketAddress;

import jl95.net.io.Util;

public class SimpleRetriableServerIStream extends SimpleRetriableIStream {

    public static SimpleRetriableServerIStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableServerIStream(peerAddress);
    }

    private SimpleRetriableServerIStream(InetSocketAddress peerAddress) {

        super(() -> Util.getIoAsServer(peerAddress).getInputStream());
    }
}
