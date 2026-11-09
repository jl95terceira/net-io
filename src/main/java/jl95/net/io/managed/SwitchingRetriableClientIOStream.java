package jl95.net.io.managed;

import static jl95.lang.SuperPowers.*;

import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.lang.variadic.Method2;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

public class SwitchingRetriableClientIOStream extends SwitchingRetriableIOStream {

    public static SwitchingRetriableClientIOStream of(Iterable<InetSocketAddress> peerAddresses, Method2<CloseableIOStreamSupplier, InetSocketAddress> afterConnect) {
        return new SwitchingRetriableClientIOStream(peerAddresses, afterConnect);
    }
    public static SwitchingRetriableClientIOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return of(peerAddresses, (ios, addr) -> {});
    }
    public static SwitchingRetriableClientIOStream of(InetSocketAddress...        peerAddresses) {
        return of(I(peerAddresses));
    }

    protected SwitchingRetriableClientIOStream(Iterable<InetSocketAddress> peerAddresses, Method2<CloseableIOStreamSupplier, InetSocketAddress> afterConnect) {

        super(I.of(peerAddresses).map(addr -> () -> {
            final CloseableIOStreamSupplier ios = Util.getIoAsClient(addr);
            try {
                afterConnect.accept(ios, addr);
            }
            catch (Exception ex) {
                ios.close();
                throw ex;
            }
            return ios;
        }));
    }
}
