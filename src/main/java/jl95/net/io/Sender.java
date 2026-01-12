package jl95.net.io;

import static jl95.lang.SuperPowers.*;
import static jl95.net.io.Constants.CONTENT_FRAME_SIZE;
import static jl95.net.io.Constants.CONTENT_AHEAD_SIGNAL;
import static jl95.net.io.Constants.EMPTY_CONTENT_FRAME;
import static jl95.net.io.Constants.NO_CONTENT_SIGNAL;
import static jl95.net.io.Constants.SIZE_FRAME_FOR_EMPTY_CONTENT_FRAME;
import static jl95.net.io.Constants.SIZE_FRAME_FOR_FULL_CONTENT_FRAME;
import static jl95.net.io.Constants.SIZE_FRAME_SIZE;

import java.io.OutputStream;
import java.math.BigInteger;

import jl95.lang.I;
import jl95.net.io.managed.ManagedOs;

public class Sender implements SenderIf<byte[]> {

    public static class SendException          extends RuntimeException {
        public SendException(Exception ex) {super(ex);}
    }

    public static Sender of(ManagedOs    os) {return new Sender(os);}
    public static Sender of(OutputStream os) {return new Sender(ManagedOs.of(os));}

    private final ManagedOs mos;

    private Sender(ManagedOs mos) {

        this.mos = mos;
        flushOutputStream();
    }

    public final void flushOutputStream() {
        mos.withOutput(os -> {});
    }

    @Override
    synchronized public final void send(byte[] outgoing) {
        mos.withOutput(os -> { uncheck(() -> {
            try {
                os.write(CONTENT_AHEAD_SIGNAL);
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
                    os.write(sizeFrame);
                    os.write(contentFrame);
                }
                else {
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

    @Override
    public final OutputStream getOutputStream() { return mos.getOutputStream(); }
}