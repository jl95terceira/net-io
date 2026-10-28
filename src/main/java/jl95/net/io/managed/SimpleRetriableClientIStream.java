package jl95.net.io.managed;

import java.io.InputStream;
import java.net.InetSocketAddress;

import jl95.lang.variadic.Method1;
import jl95.net.io.Util;

import static jl95.lang.SuperPowers.*;

public class SimpleRetriableClientIStream extends SimpleRetriableIStream {

    public static SimpleRetriableClientIStream of(InetSocketAddress peerAddress, Method1<InputStream> afterConnect) {
        return new SimpleRetriableClientIStream(peerAddress, afterConnect);
    }
    public static SimpleRetriableClientIStream of(InetSocketAddress peerAddress) {
        return of(peerAddress, is -> {});
    }

    protected SimpleRetriableClientIStream(InetSocketAddress peerAddress, Method1<InputStream> afterConnect) {

        super(() -> {
            final InputStream is = Util.getIoAsClient(peerAddress).getInputStream();
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
