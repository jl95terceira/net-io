package jl95.net.io.managed;

import static jl95.lang.SuperPowers.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import jl95.lang.I;
import jl95.lang.variadic.Method1;
import jl95.net.io.Util;

public class SwitchingRetriableClientOStream extends SwitchingRetriableOStream {

    public static SwitchingRetriableClientOStream of(Iterable<InetSocketAddress> peerAddresses, Method1<OutputStream> afterConnect) {
        return new SwitchingRetriableClientOStream(peerAddresses, afterConnect);
    }
    public static SwitchingRetriableClientOStream of(Iterable<InetSocketAddress> peerAddresses) {
        return of(peerAddresses, os -> {});
    }
    public static SwitchingRetriableClientOStream of(InetSocketAddress...        peerAddresses) {
        return of(I(peerAddresses));
    }

    protected SwitchingRetriableClientOStream(Iterable<InetSocketAddress> peerAddresses, Method1<OutputStream> afterConnect) {

        super(I.of(peerAddresses).map(addr -> () -> {
            final OutputStream os = Util.getIoAsClient(addr).getOutputStream();
            try {
                afterConnect.accept(os);
            }
            catch (Exception ex) {
                uncheck(os::close);
                throw ex;
            }
            return os;
        }));
    }
}
