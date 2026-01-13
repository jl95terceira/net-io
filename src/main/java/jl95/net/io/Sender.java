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

public class Sender extends GenericSender<byte[]> {

    public static Sender of(ManagedOs    os) {
        return new Sender(os);
    }
    public static Sender of(OutputStream os) {
        return new Sender(ManagedOs.of(os));
    }

    private Sender(ManagedOs mos) {
        super(mos);
    }

    @Override protected byte[] serialize(byte[] data) {
        return data;
    }
}