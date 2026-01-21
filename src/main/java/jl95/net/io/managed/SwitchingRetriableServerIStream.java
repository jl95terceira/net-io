package jl95.net.io.managed;

import static jl95.lang.SuperPowers.I;

import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.net.io.Util;

public class SwitchingRetriableServerIStream extends SwitchingRetriableIStream {

    public static SwitchingRetriableServerIStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableServerIStream(peerAddresses);
    }
    public static SwitchingRetriableServerIStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableServerIStream(I(peerAddresses));
    }

    protected SwitchingRetriableServerIStream(Iterable<InetSocketAddress> peerAddresses) {

        super(I.of(peerAddresses).map(addr -> () -> Util.getIoAsServer(addr).getInputStream()));
    }
}
