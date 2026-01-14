package jl95.net.io.managed;

import static jl95.lang.SuperPowers.I;

import java.net.InetSocketAddress;

import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

public class SwitchingRetriableServerIOStream extends SwitchingRetriableIOStream {

    public static SwitchingRetriableServerIOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableServerIOStream(peerAddresses);
    }
    public static SwitchingRetriableServerIOStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableServerIOStream(I(peerAddresses));
    }

    @Override protected CloseableIOStreamSupplier connect(InetSocketAddress addr) {
        return Util.getIoAsServer(addr);
    }

    private SwitchingRetriableServerIOStream(Iterable<InetSocketAddress> peerAddresses) {

        super(peerAddresses);
    }
}
