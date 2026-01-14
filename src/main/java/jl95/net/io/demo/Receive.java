package jl95.net.io.demo;

import jl95.lang.P;
import jl95.net.io.BytesIStreamReceiver;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

public class Receive {
    public static void main(String[] args) throws Exception {
        var server = new ServerSocket();
        server.bind(new InetSocketAddress("127.0.0.1", 4242));
        System.out.println("Bound");
        var sock = server.accept();
        System.out.println("Accepted");
        server.close();
        var recv = BytesIStreamReceiver.of(sock.getInputStream());
        var toStop = new P<>(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> toStop.set(true)));
        recv.recvWhile(data -> {
            System.out.println("<<< " + new String(data, StandardCharsets.UTF_8));
            return !toStop.get();
        });
    }
}
