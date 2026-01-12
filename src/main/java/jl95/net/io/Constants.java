package jl95.net.io;

import java.math.BigInteger;

public class Constants {

    public static final int    CONTENT_FRAME_SIZE = 4*1024;
    public static final byte[] SIZE_FRAME_FOR_FULL_CONTENT_FRAME = BigInteger.valueOf(CONTENT_FRAME_SIZE).toByteArray();
    public static final int    SIZE_FRAME_SIZE = SIZE_FRAME_FOR_FULL_CONTENT_FRAME.length;
    public static final byte[] SIZE_FRAME_FOR_EMPTY_CONTENT_FRAME = new byte[SIZE_FRAME_SIZE];
    public static final byte[] EMPTY_CONTENT_FRAME = new byte[CONTENT_FRAME_SIZE];

    private Constants() {}

    public static final int    CONTENT_AHEAD_SIGNAL = 255;
    public static final int    NO_CONTENT_SIGNAL = 0;
}
