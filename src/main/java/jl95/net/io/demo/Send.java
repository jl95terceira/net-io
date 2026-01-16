package jl95.net.io.demo;

import jl95.lang.P;
import jl95.net.io.BytesOStreamSender;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Send {
    public static void main(String[] args) throws Exception {
        var sock = new Socket();
        sock.connect(new InetSocketAddress("127.0.0.1", 4242));
        System.out.println("Connected");
        var sender = BytesOStreamSender.of(sock.getOutputStream());
        var scanner = new Scanner(System.in);
        var nrEmpty = new P<>(0);
        while (true) {
            System.out.print(">>> ");
            String message = scanner.nextLine();
            if (message.isEmpty()) {
                nrEmpty.set(x -> x+1);
                if (nrEmpty.get() >= 2) break;
                continue;
            }
            nrEmpty.set(0);
            try {
                sender.send(message.getBytes(StandardCharsets.UTF_8));
            }
            catch (Exception ex) {
                System.out.println(ex);
                break;
            }
        }
        sender.close();
    }
}
