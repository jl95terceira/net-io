package jl95.net.io.managed;

import jl95.lang.variadic.Method1;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

import java.net.InetSocketAddress;

public class SimpleRetriableClientIOStream extends SimpleRetriableIOStream {

    public static SimpleRetriableClientIOStream of(InetSocketAddress peerAddress, Method1<CloseableIOStreamSupplier> afterConnect) {
        return new SimpleRetriableClientIOStream(peerAddress, afterConnect);
    }
    public static SimpleRetriableClientIOStream of(InetSocketAddress peerAddress) {
        return of(peerAddress, ios -> {});
    }

    protected SimpleRetriableClientIOStream(InetSocketAddress peerAddress, Method1<CloseableIOStreamSupplier> afterConnect) {

        super(() -> {
            final CloseableIOStreamSupplier ios = Util.getIoAsClient(peerAddress);
            try {
                afterConnect.accept(ios);
            }
            catch (Exception ex) {
                ios.close();
                throw ex;
            }
            return ios;
        });
    }
}
