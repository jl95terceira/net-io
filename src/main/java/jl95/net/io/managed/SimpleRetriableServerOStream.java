package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import jl95.lang.variadic.Method1;
import jl95.net.io.Util;

public class SimpleRetriableServerOStream extends SimpleRetriableOStream {

    public static SimpleRetriableServerOStream of(InetSocketAddress peerAddress, Method1<OutputStream> afterConnect) {
        return new SimpleRetriableServerOStream(peerAddress, afterConnect);
    }
    public static SimpleRetriableServerOStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableServerOStream(peerAddress, (os) -> {});
    }

    private SimpleRetriableServerOStream(InetSocketAddress peerAddress, Method1<OutputStream> afterConnect) {

        super(() -> {
            final OutputStream os = Util.getIoAsServer(peerAddress).getOutputStream();
            try {
                afterConnect.accept(os);
            }
            catch (Exception ex) {
                uncheck(os::close);
                throw ex;
            }
            return os;
        });
    }
}
