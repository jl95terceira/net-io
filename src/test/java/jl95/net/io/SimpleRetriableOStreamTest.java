package jl95.net.io;

import jl95.lang.I;
import jl95.lang.P;
import jl95.net.io.managed.SimpleRetriableClientOStream;
import jl95.net.io.managed.SimpleRetriableOStream;

import org.junit.Assert;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static jl95.lang.SuperPowers.*;
import static jl95.net.Util.getSocketByAccept;
import static jl95.net.Util.getSocketByAcceptFuture;
import static jl95.net.io.Test.threaded;

public class SimpleRetriableOStreamTest {

    public static InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 42431);

    private static Byte[] box(byte[] bb) {
        var bb2 = new Byte[bb.length];
        var i = 0;
        for (var b: bb) {
            bb2[i] = b;
            i = i+1;
        }
        return bb2;
    }
    private static String repr(byte[] bb) {
        return String.format("[%s]", String.join(",", I.ofArray(box(bb)).map(b -> Byte.toString(b))));
    }

    public BytesIStreamReceiver receiver;
    public BytesOStreamSender sender;
    public CompletableFuture<byte[]> payloadBackPromise;
    public SimpleRetriableOStream retriableIos;
    public Integer restartsNr = 0;
    public CompletableFuture<Void> restartingFuture;

    private void assertReceivesPayload(byte[] payload) throws Exception {
        System.out.println("Payload to test: "+repr(payload));
        System.out.println("Sending");
        sender.send(payload);
        System.out.println("Sent");
        if (restartingFuture != null) {
            restartingFuture.get();
        }
        System.out.println("Waiting for payload back");
        var payloadBack = payloadBackPromise.get(2000L, TimeUnit.MILLISECONDS);
        System.out.println("Got payload back - OK");
        try {
            org.junit.Assert.assertArrayEquals(payload, payloadBack);
        }
        catch (AssertionError ex) {
            System.out.println("Expected : "+repr(payload));
            System.out.println("Got      : "+repr(payloadBack));
            throw ex;
        }
    }
    private void expectPayload() {
        payloadBackPromise = new CompletableFuture<>();
        final var recvOptions = new Receiver.RecvOptions.Editable();
        recvOptions.inputTimeoutHandler = info -> {
            System.out.print(".");System.out.flush();
        };
        recvOptions.handlingExcHandler = ex -> {
            System.out.print("Receiver handling exception: "+ex.getMessage());System.out.flush();
        };
        threaded(() -> receiver.recv(payloadBackPromise::complete, recvOptions));
        receiver.waitStarted().get();
        System.out.println("Receiver expecting payload");
    }
    private void restartReceiver() {
        restartingFuture = new CompletableFuture<>();
        restartsNr += 1;
        System.out.printf ("Receiver restarting (nr of restart = %s)\n", restartsNr);
        System.out.println("  stop");
        receiver.ensureStopped();
        System.out.println("  close (connection closed - sender will have to reconnect)");
        uncheck(receiver::close);
        System.out.println("  new");
        threaded(() -> {
            receiver = BytesIStreamReceiver.of(uncheck(getSocketByAccept(addr)::getInputStream));
            System.out.println("Receiver restarted OK");
            expectPayload();
            restartingFuture.complete(null);
        });
    }

    @org.junit.After
    public void tearDown() throws Exception {
        System.out.println("Test OK.");
        if (receiver != null)
        {
            receiver.ensureStopped();
            receiver.close();
        }
        sender.close();
        System.out.println("Done.");
    }

    @org.junit.Test
    public void test() throws Exception {
        var receiverSocketFuture = getSocketByAcceptFuture(addr);
        final P<Integer> afterConnectCallsNr = new P<>(0);
        retriableIos = SimpleRetriableClientOStream.of(addr, ios -> {
            afterConnectCallsNr.set(i -> i + 1);
        });
        sleep(300);
        sender   = BytesOStreamSender.of(retriableIos);
        receiver = BytesIStreamReceiver.of(receiverSocketFuture.get().getInputStream());
        System.out.println("Connected");
        expectPayload();
        var payloadMaker = function(() -> String.join("", I.range(10).map(i -> UUID.randomUUID().toString())).getBytes());
        assertReceivesPayload(payloadMaker.apply());
        Assert.assertEquals(1, afterConnectCallsNr.get().intValue());
        // restart many times
        final int N_RECONNECTS = 10;
        for (var r: I.range(N_RECONNECTS)) {
            System.out.println("\nReconnect nr %s\n".formatted(1+r));
            restartReceiver();
            sleep(300);
            assertReceivesPayload(payloadMaker.apply());
        }
        Assert.assertEquals(1 /* first connection.*/ + N_RECONNECTS, afterConnectCallsNr.get().intValue());
    }
}
