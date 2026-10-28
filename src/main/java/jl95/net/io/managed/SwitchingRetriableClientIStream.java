package jl95.net.io.managed;

import static jl95.lang.SuperPowers.*;

import java.io.InputStream;
import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.lang.variadic.Method1;
import jl95.net.io.Util;

public class SwitchingRetriableClientIStream extends SwitchingRetriableIStream {

    public static SwitchingRetriableClientIStream of(Iterable<InetSocketAddress> peerAddresses, Method1<InputStream> afterConnect) {
        return new SwitchingRetriableClientIStream(peerAddresses, afterConnect);
    }
    public static SwitchingRetriableClientIStream of(Iterable<InetSocketAddress> peerAddresses) {
        return of(peerAddresses, is -> {});
    }
    public static SwitchingRetriableClientIStream of(InetSocketAddress...        peerAddresses) {
        return of(I(peerAddresses));
    }

    protected SwitchingRetriableClientIStream(Iterable<InetSocketAddress> peerAddresses, Method1<InputStream> afterConnect) {

        super(I.of(peerAddresses).map(addr -> () -> {
            final InputStream is = Util.getIoAsClient(addr).getInputStream();
            try {
                afterConnect.accept(is);
            }
            catch (Exception ex) {
                uncheck(is::close);
                throw ex;
            }
            return is;
        }));
    }
}
