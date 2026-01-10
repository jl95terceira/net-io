package jl95.net.io;

import static jl95.lang.SuperPowers.uncheck;

import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import jl95.net.io.collections.ReceiverAdaptersCollection;
import jl95.net.io.collections.SenderAdaptersCollections;

public class Test {

    private static java.net.InetSocketAddress addr = new java.net.InetSocketAddress("127.0.0.1", 42422);
    private static Boolean toStop = false;

    static { Runtime.getRuntime().addShutdownHook(new Thread(() -> { toStop = true; })); }

    private ReceiverIf<String> receiver;
    private SenderIf<String> sender;

    @org.junit.Before
    public void setUp() throws Exception {
        var serversock = new ServerSocket();
        serversock.bind(addr);
        CompletableFuture<ReceiverIf<String>> receiverFuture = new CompletableFuture<>();
        new Thread(() -> {
            try {
                var sock = serversock.accept();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        sock.close();
                    }
                    catch(Exception ex) {}
                }));
                serversock.close();
                receiverFuture.complete(ReceiverAdaptersCollection.asStringReceiver(Receiver.of(Ios.fromSocketLazy(sock).getInputStream())));
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).start();
        var clientSocket = new java.net.Socket();
        clientSocket.connect(addr);
        sender   = SenderAdaptersCollections.asStringSender(Sender.of(Ios.fromSocketLazy(clientSocket).getOutputStream()));
        receiver = receiverFuture.get();
    }
    @org.junit.After
    public void tearDown() throws Exception {
        sender.getOutputStream().close();
        if (receiver.isReceiving()) {
            receiver.recvStop().get();
        }
        receiver.getInputStream().close();
    }

    public static void threaded(Runnable runnable) {
        new Thread(runnable).start();
    }

    @org.junit.Test public void testStartStop() {

        org.junit.Assert.assertFalse(receiver.isReceiving());
        threaded(() -> receiver.recv(x -> {}));
        receiver.recvWaitStarted().get();
        org.junit.Assert.assertTrue(receiver.isReceiving());
        receiver.recvStop().get();
        org.junit.Assert.assertFalse(receiver.isReceiving());
    }
    private void testSendMessages(List<String> messages) {

        System.out.printf("Testing send-receive (through localhost) for %s messages\n", messages.size());
        int[] charsReceivedNr = { 0 };
        var messagesSendIterator = messages.iterator();
        threaded(() -> receiver.recv(message -> {
            charsReceivedNr[0] += message.length();
            org.junit.Assert.assertTrue  (messagesSendIterator.hasNext());
            org.junit.Assert.assertEquals(messagesSendIterator.next(), message);
        }));
        receiver.recvWaitStarted().get();
        for (var message: messages) {
             sender.send(message);
        }
        System.out.println("Exchanged a total of "+charsReceivedNr[0]+" characters");
        receiver.recvStop().get();
    }
    @org.junit.Test public void test() {

        var N = 1000; // nr of messages
        var R = 10;  // size of each message = R * size of a UUID
        List<String> messages = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            messages.add(UUID.randomUUID().toString().repeat(R));
        }
        testSendMessages(messages);
    }
    @org.junit.Test public void testException() {
        threaded(() -> receiver.recv(x -> { throw new RuntimeException(); }));
        receiver.recvWaitStarted().get();
        sender.send("abc");
        receiver.recvStop().get();
    }
}
