package jl95.net.io.managed;

import static jl95.lang.SuperPowers.*;

import java.net.InetSocketAddress;

import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

public class SwitchingRetriableClientIOStream extends SwitchingRetriableIOStream {

    public static SwitchingRetriableClientIOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableClientIOStream(peerAddresses);
    }
    public static SwitchingRetriableClientIOStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableClientIOStream(I(peerAddresses));
    }

    @Override protected CloseableIOStreamSupplier connect(InetSocketAddress addr) {
        return Util.getIoAsClient(addr);
    }

    private SwitchingRetriableClientIOStream(Iterable<InetSocketAddress> peerAddresses) {

        super(peerAddresses);
    }
}
