package jl95.net.io;

import static jl95.lang.SuperPowers.uncheck;
import static jl95.net.io.Constants.CONTENT_AHEAD_SIGNAL;
import static jl95.net.io.Constants.CONTENT_FRAME_SIZE;
import static jl95.net.io.Constants.EMPTY_CONTENT_FRAME;
import static jl95.net.io.Constants.NO_CONTENT_SIGNAL;
import static jl95.net.io.Constants.SIZE_FRAME_FOR_EMPTY_CONTENT_FRAME;
import static jl95.net.io.Constants.SIZE_FRAME_FOR_FULL_CONTENT_FRAME;
import static jl95.net.io.Constants.SIZE_FRAME_SIZE;

import java.io.OutputStream;
import java.math.BigInteger;

import jl95.lang.I;
import jl95.lang.variadic.Function1;
import jl95.net.io.managed.ManagedOs;

public abstract class IStreamSender<T> implements Sender<T> {

    public static class SendException          extends RuntimeException {
        public SendException(Exception ex) {super(ex);}
    }

    public static <T> IStreamSender<T> of(Function1<IStreamSender<T>, ManagedOs> constructor, ManagedOs os) {
        return constructor.apply(os);
    }
    public static <T> IStreamSender<T> of(Function1<IStreamSender<T>, ManagedOs> constructor, OutputStream os) {
        return constructor.apply(ManagedOs.of(os));
    }

    private final ManagedOs mos;

    public IStreamSender(ManagedOs mos) {

        this.mos = mos;
        flushOutputStream();
    }

    public OutputStream getOutputStream() { return mos.getOutputStream(); }
    public void flushOutputStream() {
        mos.withOutput(os -> {});
    }

    protected abstract byte[] serialize(T data);

    @Override public synchronized void send(T data) {
        var outgoing = serialize(data);
        mos.withOutput(os -> { uncheck(() -> {
            try {
                if (outgoing.length > 0) {
                    byte[] sizeFrame;
                    byte[] contentFrame;
                    var N = (outgoing.length - 1) / CONTENT_FRAME_SIZE + 1;
                    for (var i: I.range(N-1)) {
                        sizeFrame    = SIZE_FRAME_FOR_FULL_CONTENT_FRAME;
                        contentFrame = new byte[CONTENT_FRAME_SIZE];
                        System.arraycopy(outgoing, i* CONTENT_FRAME_SIZE, contentFrame, 0, CONTENT_FRAME_SIZE);
                        os.write(CONTENT_AHEAD_SIGNAL);
                        os.write(sizeFrame);
                        os.write(contentFrame);
                    }
                    sizeFrame    = new byte[SIZE_FRAME_SIZE];
                    contentFrame = new byte[CONTENT_FRAME_SIZE];
                    var lastFrameContentSize = outgoing.length - (N-1)* CONTENT_FRAME_SIZE;
                    var lastFrameContentSizeAsBytes = BigInteger.valueOf(lastFrameContentSize).toByteArray();
                    System.arraycopy(lastFrameContentSizeAsBytes,
                                     0,
                                     sizeFrame,
                                     SIZE_FRAME_SIZE - lastFrameContentSizeAsBytes.length,
                                     lastFrameContentSizeAsBytes.length);
                    System.arraycopy(outgoing,
                                     (N-1)*CONTENT_FRAME_SIZE,
                                     contentFrame,
                                     0,
                                     lastFrameContentSize);
                    os.write(CONTENT_AHEAD_SIGNAL);
                    os.write(sizeFrame);
                    os.write(contentFrame);
                }
                else {
                    os.write(CONTENT_AHEAD_SIGNAL);
                    os.write(SIZE_FRAME_FOR_EMPTY_CONTENT_FRAME);
                    os.write(EMPTY_CONTENT_FRAME);
                }
                os.write(NO_CONTENT_SIGNAL);
            }
            catch (Exception ex) {
                throw new SendException(ex);
            }
        }); });
    }
    @Override public <U> IStreamSender<U> adapted(Function1<T,U> adapter) {
        return new IStreamSender<U>(mos) {
            @Override protected byte[] serialize(U data) {
                return IStreamSender.this.serialize(adapter.apply(data));
            }
        };
    }
}