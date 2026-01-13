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

public class Receiver extends GenericReceiver<byte[]> {

    public static Receiver of(ManagedIs   is) {
        return new Receiver(is);
    }
    public static Receiver of(InputStream is) {
        return new Receiver(ManagedIs.of(is));
    }

    private Receiver(ManagedIs mis) {
        super(mis);
    }

    @Override protected byte[] deserialize(byte[] data) {
        return data;
    }
}
