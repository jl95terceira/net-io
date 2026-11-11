package jl95.net.io;

import static jl95.lang.SuperPowers.constant;

import java.math.BigInteger;

import jl95.lang.variadic.Function0;

public class Constants {

    public static final int    CONTENT_FRAME_SIZE = 4*1024;
    public static final byte[] SIZE_FRAME_FOR_FULL_CONTENT_FRAME = BigInteger.valueOf(CONTENT_FRAME_SIZE).toByteArray();
    public static final int    SIZE_FRAME_SIZE = SIZE_FRAME_FOR_FULL_CONTENT_FRAME.length;
    public static final byte[] SIZE_FRAME_FOR_EMPTY_CONTENT_FRAME = new byte[SIZE_FRAME_SIZE];
    public static final byte[] EMPTY_CONTENT_FRAME = new byte[CONTENT_FRAME_SIZE];

    public static final int    CONTENT_AHEAD_SIGNAL = 255;
    public static final int    NO_CONTENT_SIGNAL = 0;

    public static final Function0<Integer> RESWITCH_TIMEOUT_DEFAULT_MS = constant( 250);
    public static final Function0<Integer> SUPPLY_TIMEOUT_DEFAULT_MS = constant(2000);
    public static final Function0<Integer> RETRY_TIMEOUT_DEFAULT_MS = constant( 250);
    public static int RECEIVER_INPUT_TIMEOUT_DEFAULT_MS = 250;

    private Constants() {}
}
