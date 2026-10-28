package jl95.net.io.managed;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import jl95.lang.variadic.Method1;
import jl95.net.io.Util;

import static jl95.lang.SuperPowers.*;

public class SimpleRetriableClientOStream extends SimpleRetriableOStream {

    public static SimpleRetriableClientOStream of(InetSocketAddress peerAddress, Method1<OutputStream> afterConnect) {
        return new SimpleRetriableClientOStream(peerAddress, afterConnect);
    }
    public static SimpleRetriableClientOStream of(InetSocketAddress peerAddress) {
        return of(peerAddress, os -> {});
    }

    protected SimpleRetriableClientOStream(InetSocketAddress peerAddress, Method1<OutputStream> afterConnect) {

        super(() -> {
            final OutputStream os = Util.getIoAsClient(peerAddress).getOutputStream();
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
