package jl95.net.io.managed;

import static jl95.lang.SuperPowers.I;

import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

public class SwitchingRetriableServerIOStream extends SwitchingRetriableIOStream {

    public static SwitchingRetriableServerIOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableServerIOStream(peerAddresses);
    }
    public static SwitchingRetriableServerIOStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableServerIOStream(I(peerAddresses));
    }

    protected SwitchingRetriableServerIOStream(Iterable<InetSocketAddress> peerAddresses) {

        super(I.of(peerAddresses).map(addr -> () -> Util.getIoAsServer(addr)));
    }
}
