package jl95.net.io.managed;

import jl95.lang.variadic.Method1;
import jl95.net.io.CloseableIOStreamSupplier;
import jl95.net.io.Util;

import java.net.InetSocketAddress;

public class SimpleRetriableServerIOStream extends SimpleRetriableIOStream {

    public static SimpleRetriableServerIOStream of(InetSocketAddress peerAddress, Method1<CloseableIOStreamSupplier> afterConnect) {
        return new SimpleRetriableServerIOStream(peerAddress, afterConnect);
    }
    public static SimpleRetriableServerIOStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableServerIOStream(peerAddress, (ios) -> {});
    }

    private SimpleRetriableServerIOStream(InetSocketAddress peerAddress, Method1<CloseableIOStreamSupplier> afterConnect) {

        super(() -> {
            final CloseableIOStreamSupplier ios = Util.getIoAsServer(peerAddress);
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
