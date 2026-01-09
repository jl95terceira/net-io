package jl95.net.io;

import jl95.net.Server;
import jl95.net.io.util.Defaults;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static jl95.lang.SuperPowers.*;

public class Util {

        public static final int    CONTENT_AHEAD_SIGNAL = 255;
        public static final int    NO_CONTENT_SIGNAL = 0;
        public static final int    CONTENT_FRAME_SIZE = 4*1024;
        public static final byte[] SIZE_FRAME_FOR_FULL_CONTENT_FRAME = BigInteger.valueOf(CONTENT_FRAME_SIZE).toByteArray();
        public static final int    SIZE_FRAME_SIZE = SIZE_FRAME_FOR_FULL_CONTENT_FRAME.length;

        public static CloseableIos getIoFromSocket(Socket            socket) {
            return new CloseableIos() {
                @Override public InputStream getInputStream() { return uncheck(socket::getInputStream); }
                @Override public OutputStream getOutputStream() { return uncheck(socket::getOutputStream); }
                @Override public void         close () {
                    if (!socket.isClosed()) {
                        uncheck(socket::close);
                    }
                }
            };
        }
        public static CloseableIos getIoAsClient  (InetSocketAddress addr) {
            var socket = new Socket();
            uncheck(() -> socket.connect(addr));
            return getIoFromSocket(socket);
        }
        public static CloseableIos getIoAsServer  (InetSocketAddress addr, Integer clientConnectionTimeoutMs) {
            var clientSocketFuture = new CompletableFuture<Socket>();
            var server = Server.fromSocket(jl95.net.Util.getSimpleServerSocket(addr, Defaults.acceptTimeoutMs));
            server.setAcceptCb((self, socket) -> {
                clientSocketFuture.complete(socket);
            });
            server.start();
            var clientSocket = uncheck(() -> clientConnectionTimeoutMs != null
                                           ? clientSocketFuture.get(clientConnectionTimeoutMs, TimeUnit.MILLISECONDS)
                                           : clientSocketFuture.get());
            server.stop().get(); // stop server right away - no need to accept more connections
            uncheck(server.getSocket()::close); // release bind address
            return getIoFromSocket(clientSocket);
        }
        public static CloseableIos getIoAsServer  (InetSocketAddress addr) { return getIoAsServer(addr, null); }
}
