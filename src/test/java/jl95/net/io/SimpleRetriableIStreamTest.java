package jl95.net.io;

import static jl95.lang.SuperPowers.function;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.uncheck;
import static jl95.net.Util.getSocketByConnect;
import static jl95.net.Util.getSocketByConnectFuture;
import static jl95.net.io.Test.threaded;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import jl95.lang.I;
import jl95.lang.P;
import jl95.net.io.managed.SimpleRetriableIStream;
import jl95.net.io.managed.SimpleRetriableServerIStream;

public class SimpleRetriableIStreamTest {

    public static InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 42432);

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
    public CompletableFuture<SimpleRetriableIStream> retriableIosFuture = new CompletableFuture<>();
    public Integer restartsNr = 0;

    private void assertReceivesPayload(byte[] payload) throws Exception {
        System.out.println("Payload to test: "+repr(payload));
        System.out.println("Sending");
        sender.send(payload);
        System.out.println("Waiting for payload back");
        var payloadBack = payloadBackPromise.get(2000L, TimeUnit.MILLISECONDS);
        System.out.println("Got payload back - OK");
        try {
            Assert.assertArrayEquals(payload, payloadBack);
        }
        catch (AssertionError ex) {
            System.out.println("Expected : "+repr(payload));
            System.out.println("Got      : "+repr(payloadBack));
            throw ex;
        }
    }
    private void expectPayload() {
        payloadBackPromise = new CompletableFuture<>();
        threaded(() -> receiver.recv(payloadBackPromise::complete));
        receiver.waitStarted().get();
    }
    private void restartSender() {
        restartsNr += 1;
        System.out.printf ("Sender restarting (nr of restart = %s)\n", restartsNr);
        System.out.println("  close (connection closed - receiver will have to reconnect)");
        uncheck(sender::close);
        System.out.println("  new");
        threaded(() -> {
            sender = BytesOStreamSender.of(uncheck(() -> getSocketByConnect(addr).getOutputStream()));
            System.out.println("Sender restarted OK");
        });
    }

    @org.junit.After
    public void tearDown() throws Exception {
        if (receiver != null)
        {
            receiver.ensureStopped();
            receiver.close();
        }
        sender.close();
    }
    @org.junit.AfterClass
    public static void tearDownStatic() {
        sleep(1000);
    }

    //TODO: fix this test when we have implemented proper retriable receiver, that detects a dead connection in order to re-initialize
//    @org.junit.Test
    public void test() throws Exception {
        var senderSocketFuture = getSocketByConnectFuture(addr);
        final P<Integer> afterConnectCallsNr = new P<>(0);
        threaded(() -> {
            retriableIosFuture.complete(SimpleRetriableServerIStream.of(addr, ios -> {
                afterConnectCallsNr.set(i -> i + 1);
            }));
        });
        sleep(500);
        receiver = BytesIStreamReceiver.of(retriableIosFuture.get());
        sender   = BytesOStreamSender.of(senderSocketFuture.get().getOutputStream());
        System.out.println("Connected");
        expectPayload();
        var payloadMaker = function(() -> String.join("", I.range(10).map(i -> UUID.randomUUID().toString())).getBytes());
        assertReceivesPayload(payloadMaker.apply());
        Assert.assertEquals(1, afterConnectCallsNr.get().intValue());
        // restart many times
        final int N_RECONNECTS = 5;
        for (var r: I.range(N_RECONNECTS)) {
            restartSender();
            sleep(500);
            assertReceivesPayload(payloadMaker.apply());
        }
        Assert.assertEquals(1 /* first connection.*/ + N_RECONNECTS, afterConnectCallsNr.get().intValue());
    }
}
