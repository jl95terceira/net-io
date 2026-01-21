package jl95.net.io.managed;

import static jl95.lang.SuperPowers.I;

import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.net.io.Util;

public class SwitchingRetriableServerOStream extends SwitchingRetriableOStream {

    public static SwitchingRetriableServerOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableServerOStream(peerAddresses);
    }
    public static SwitchingRetriableServerOStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableServerOStream(I(peerAddresses));
    }

    protected SwitchingRetriableServerOStream(Iterable<InetSocketAddress> peerAddresses) {

        super(I.of(peerAddresses).map(addr -> () -> Util.getIoAsServer(addr).getOutputStream()));
    }
}
