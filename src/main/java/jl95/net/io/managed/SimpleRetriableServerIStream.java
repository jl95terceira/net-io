package jl95.net.io.managed;

import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;
import java.net.InetSocketAddress;

import jl95.lang.variadic.Method1;
import jl95.net.io.Util;

public class SimpleRetriableServerIStream extends SimpleRetriableIStream {

    public static SimpleRetriableServerIStream of(InetSocketAddress peerAddress, Method1<InputStream> afterConnect) {
        return new SimpleRetriableServerIStream(peerAddress, afterConnect);
    }
    public static SimpleRetriableServerIStream of(InetSocketAddress peerAddress) {
        return new SimpleRetriableServerIStream(peerAddress, (is) -> {});
    }

    private SimpleRetriableServerIStream(InetSocketAddress peerAddress, Method1<InputStream> afterConnect) {

        super(() -> {
            final InputStream is = Util.getIoAsServer(peerAddress).getInputStream();
            try {
                afterConnect.accept(is);
            }
            catch (Exception ex) {
                uncheck(is::close);
                throw ex;
            }
            return is;
        });
    }
}
