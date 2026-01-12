package jl95.net.io;

import java.io.InputStream;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import static jl95.lang.SuperPowers.function;
import static jl95.lang.SuperPowers.self;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;
import static jl95.lang.SuperPowers.uncheck;
import static jl95.net.io.Constants.CONTENT_AHEAD_SIGNAL;
import static jl95.net.io.Constants.CONTENT_FRAME_SIZE;
import static jl95.net.io.Constants.SIZE_FRAME_SIZE;

import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.net.io.managed.ManagedIs;
import jl95.util.*;

public class Receiver implements ReceiverIf<byte[]> {

    public static class AlreadyReceivingException extends RuntimeException {}
    public static class NotReceivingException extends RuntimeException {}

    public static Receiver of(ManagedIs   is) {
        return new Receiver(is);
    }
    public static Receiver of(InputStream is) {
        return new Receiver(ManagedIs.of(is));
    }

    private final    ManagedIs               mis;
    private volatile Boolean                 isReceiving = false;
    private volatile Boolean                 toStop      = false;
    private          CompletableFuture<Void> startFuture = new CompletableFuture<>();
    private          CompletableFuture<Void> stopFuture  = new CompletableFuture<>();

    private Receiver(ManagedIs mis) {

        this.mis = mis;
        flushInputStream();
    }

    private UVoidFuture recvStopUnchecked() {
        toStop = true; // to be checked in loop, after which the future above will be completed
        return UVoidFuture.of(stopFuture);
    }

    public final void flushInputStream() {
        mis.withInput(is -> {});
    }

    @Override
    synchronized
    public final void recvWhile(Function1<Boolean, byte[]> incomingCbToContinue,
                                RecvOptions options) {
        if (isReceiving) {
            throw new AlreadyReceivingException();
        }
        toStop      = false;
        stopFuture  = new CompletableFuture<>();
        isReceiving = true;
        startFuture.complete(null);
        var timeouts     = new P<>(0);
        var timeoutT0    = new P<>(Instant.now());
        while (!toStop) {
            var incoming = new P<byte[]>(null);
            try {
                try {
                    var continueLoop = mis.withInput(is -> { return uncheck(() -> {
                        if (is.available() == 0) {
                            timeouts.set(v -> v + 1);
                            options.onInputTimeout(new TimeoutInfo(timeouts.get(), Duration.between(timeoutT0.get(), Instant.now())));
                            sleep(options.inputRetryTimeoutMs());
                            return true;
                        }
                        timeouts .set(0);
                        timeoutT0.set(Instant.now());
                        incoming.set(old -> null);
                        var contentPartsList = strict(new LinkedList<byte[]>());
                        while (!toStop) {
                            var signal = is.read();
                            if (signal != CONTENT_AHEAD_SIGNAL) {
                                if (!contentPartsList.isEmpty()) {
                                    var N = I.of(contentPartsList).reduce(0, (sum,array) -> sum + array.length);
                                    incoming.set(new byte[N]);
                                    var i = 0;
                                    for (var p: contentPartsList) {
                                        System.arraycopy(p, i, incoming.get(), 0, p.length);
                                        i += p.length;
                                    }
                                }
                                break;
                            }
                            var sizeFrame = is.readNBytes(SIZE_FRAME_SIZE);
                            var contentSize = new BigInteger(sizeFrame).intValue();
                            var contentFrame = is.readNBytes(CONTENT_FRAME_SIZE);
                            var contentPart = new byte[contentSize];
                            if (contentSize > 0) {
                                System.arraycopy(contentFrame, 0, contentPart, 0, contentSize);
                            }
                            contentPartsList.add(contentPart);
                        }
                        return false;
                    }); });
                    if (continueLoop) {
                        continue;
                    }
                }
                catch (Exception   ex) {
                    options.onInputException(ex);
                    break;
                }
                try {
                    if (incoming.get() != null) {
                        var toContinue = incomingCbToContinue.apply(incoming.get());
                        if (!toContinue) {
                            toStop = true;
                        }
                    }
                }
                catch (Exception ex) {
                    options.onHandlingException(ex);
                }
            }
            catch (Exception ex) {
                System.out.println("Receiver: UNHANDLED FOLLOW-UP EXCEPTION - stop recv");
                ex.printStackTrace();
                break;
            }
        }
        isReceiving = false;
        startFuture = new CompletableFuture<>();
        stopFuture.complete(null);
    }
    @Override
    public final UVoidFuture recvWaitStarted() {
        return UVoidFuture.of(startFuture);
    }
    @Override
    public final UVoidFuture recvStop() {

        if (!isReceiving) {
            throw new NotReceivingException();
        }
        return recvStopUnchecked();
    }
    @Override
    public final Boolean isReceiving() {
        return isReceiving;
    }
    @Override
    public final InputStream getInputStream() { return mis.getInputStream(); }
  }
