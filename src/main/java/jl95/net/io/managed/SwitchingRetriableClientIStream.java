package jl95.net.io.managed;

import static jl95.lang.SuperPowers.I;

import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.net.io.Util;

public class SwitchingRetriableClientIStream extends SwitchingRetriableIStream {

    public static SwitchingRetriableClientIStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableClientIStream(peerAddresses);
    }
    public static SwitchingRetriableClientIStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableClientIStream(I(peerAddresses));
    }

    protected SwitchingRetriableClientIStream(Iterable<InetSocketAddress> peerAddresses) {

        super(I.of(peerAddresses).map(addr -> () -> Util.getIoAsClient(addr).getInputStream()));
    }
}
