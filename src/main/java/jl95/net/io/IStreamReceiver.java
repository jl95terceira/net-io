package jl95.net.io;

import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;
import static jl95.lang.SuperPowers.uncheck;
import static jl95.net.io.Constants.CONTENT_AHEAD_SIGNAL;
import static jl95.net.io.Constants.CONTENT_FRAME_SIZE;
import static jl95.net.io.Constants.NO_CONTENT_SIGNAL;
import static jl95.net.io.Constants.SIZE_FRAME_SIZE;

import java.io.InputStream;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jl95.lang.I;
import jl95.lang.P;
import jl95.lang.variadic.Function1;
import jl95.net.io.util.InputStreams;
import jl95.util.UVoidFuture;

public abstract class IStreamReceiver<T> implements Receiver<T> {

    public static class AlreadyReceivingException extends RuntimeException {}
    public static class NotReceivingException extends RuntimeException {}

    public static <T> IStreamReceiver<T> of(Function1<IStreamReceiver<T>, Managed<InputStream>> constructor, Managed<InputStream> is) {
        return constructor.apply(is);
    }
    public static <T> IStreamReceiver<T> of(Function1<IStreamReceiver<T>, Managed<InputStream>> constructor, InputStream is) {
        return constructor.apply(InputStreams.getSimpleManaged(is));
    }

    private final Managed<InputStream> mis;
    private final AtomicBoolean isReceiving;
    private final AtomicBoolean toStop;
    private final AtomicReference<CompletableFuture<Void>> startFuture;
    private final AtomicReference<CompletableFuture<Void>> stopFuture;

    private IStreamReceiver(Managed<InputStream> mis,
                            AtomicBoolean isReceiving,
                            AtomicBoolean toStop,
                            AtomicReference<CompletableFuture<Void>> startFuture,
                            AtomicReference<CompletableFuture<Void>> stopFuture) {
        this.mis          = mis;
        this.isReceiving  = isReceiving;
        this.toStop       = toStop;
        this.startFuture  = startFuture;
        this.stopFuture   = stopFuture;
    }

    public IStreamReceiver(Managed<InputStream> mis) {

        this(mis, new AtomicBoolean(false), new AtomicBoolean(false), new AtomicReference<>(new CompletableFuture<>()), new AtomicReference<>(new CompletableFuture<>()));
        flushInputStream();
    }

    private UVoidFuture recvStopUnchecked() {
        toStop.set(true); // to be checked in loop, after which the future above will be completed
        return waitStopped();
    }
    public InputStream getInputStream() {
        return mis.doWith(is -> is);
    }
    public void flushInputStream() {
        mis.doWith(is -> {});
    }

    protected abstract T deserialize(byte[] data);

    @Override public synchronized void recvWhile(Function1<Boolean, T> incomingCbToContinue,
                                                 RecvOptions options) {
        if (isReceiving.get()) {
            throw new AlreadyReceivingException();
        }
        toStop.set(false);
        stopFuture.set(new CompletableFuture<>());
        isReceiving.set(true);
        startFuture.get().complete(null);
        final var timeoutT0 = new P<>(Instant.now());
        final var incoming = new P<byte[]>(null);
        while (!toStop.get()) {
            try {
                try {
                    var continueLoop = mis.doWith(is -> { return uncheck(() -> {
                        if (is.available() == 0) {

                            options.onInputTimeout(new TimeoutInfo(Duration.between(timeoutT0.get(), Instant.now())));
                            sleep(options.inputRetryTimeoutMs());
                            return true;

                        }
                        timeoutT0.set(Instant.now());
                        incoming.set(old -> null);
                        final var contentPartsList = strict(new LinkedList<byte[]>());
                        while (!toStop.get()) {
                            final int signal = is.read();
                            switch (signal) {

                                case -1 -> {

                                    throw new RuntimeException("RECEIVER END OF STREAM");

                                }

                                case CONTENT_AHEAD_SIGNAL -> {

                                    final byte[] sizeFrame = is.readNBytes(SIZE_FRAME_SIZE);
                                    final int contentSize = new BigInteger(1, sizeFrame).intValue();
                                    final byte[] contentFrame = is.readNBytes(CONTENT_FRAME_SIZE);
                                    final byte[] contentPart = new byte[contentSize];
                                    if (contentSize > 0) {
                                        System.arraycopy(contentFrame, 0, contentPart, 0, contentSize);
                                    }
                                    contentPartsList.add(contentPart);

                                }

                                case NO_CONTENT_SIGNAL -> {

                                    if (!contentPartsList.isEmpty()) {
                                        final Integer N = I.of(contentPartsList).reduce(0, (sum,array) -> sum + array.length);
                                        incoming.set(new byte[N]);
                                        int i = 0;
                                        for (final byte[] p: contentPartsList) {
                                            System.arraycopy(p, 0, incoming.get(), i, p.length);
                                            i += p.length;
                                        }
                                    }
                                    return false;

                                }

                                default -> {

                                    System.out.printf("RECEIVER PROTOCOL EXCEPTION: UNKNOWN SIGNAL: %s%n", signal);

                                }
                            }
                        }
                        return false;
                    }); });
                    if (continueLoop) {
                        continue;
                    }
                }
                catch (Exception ex) {
                    options.onInputException(ex);
                    break;
                }
                try {
                    if (incoming.get() != null) {
                        var toContinue = incomingCbToContinue.apply(deserialize(incoming.get()));
                        incoming.set((byte[])null);
                        if (!toContinue) {
                            toStop.set(true);
                        }
                    }
                }
                catch (Exception ex) {
                    options.onHandlingException(ex);
                }
            }
            catch (Exception ex) {
                System.out.printf("RECEIVER UNHANDLED FOLLOW-UP EXCEPTION: %s%n", ex);
                break;
            }
        }
        isReceiving.set(false);
        startFuture.set(new CompletableFuture<>());
        stopFuture.get().complete(null);
    }
    @Override public UVoidFuture waitStarted() {
        return UVoidFuture.of(startFuture.get());
    }
    @Override public UVoidFuture stop() {

        if (!isReceiving.get()) {
            throw new NotReceivingException();
        }
        return recvStopUnchecked();
    }
    @Override public UVoidFuture waitStopped() {
        return UVoidFuture.of(stopFuture.get());
    }
    @Override public boolean isReceiving() {
        return isReceiving.get();
    }
    @Override public <U> IStreamReceiver<U> adapted(Function1<U,T> adapter) {

        return new IStreamReceiver<>(mis, isReceiving, toStop, startFuture, stopFuture) {

            @Override protected U deserialize(byte[] data) {
                return adapter.apply(IStreamReceiver.this.deserialize(data));
            }
        };
    }
    @Override public void close() {
        mis.close();
    }
}
