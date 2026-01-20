package jl95.net.io.managed;

import static jl95.lang.SuperPowers.*;

import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

public class SwitchingRetriableClientIOStream extends SwitchingRetriableIOStream {

    public static SwitchingRetriableClientIOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return new SwitchingRetriableClientIOStream(peerAddresses);
    }
    public static SwitchingRetriableClientIOStream of(InetSocketAddress...        peerAddresses) {
        return new SwitchingRetriableClientIOStream(I(peerAddresses));
    }

    protected SwitchingRetriableClientIOStream(Iterable<InetSocketAddress> peerAddresses) {

        super(I.of(peerAddresses).map(addr -> () -> Util.getIoAsClient(addr)));
    }
}
