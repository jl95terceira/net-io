package jl95.net.io.managed;

import static jl95.lang.SuperPowers.I;

import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.net.io.Util;

public class SwitchingRetriableClientOStream extends SwitchingRetriableOStream {

    public static SwitchingRetriableClientOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableClientOStream(peerAddresses);
    }
    public static SwitchingRetriableClientOStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableClientOStream(I(peerAddresses));
    }

    protected SwitchingRetriableClientOStream(Iterable<InetSocketAddress> peerAddresses) {

        super(I.of(peerAddresses).map(addr -> () -> Util.getIoAsClient(addr).getOutputStream()));
    }
}
