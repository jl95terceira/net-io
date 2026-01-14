package jl95.net.io;

import java.io.InputStream;

import static jl95.lang.SuperPowers.function;
import static jl95.lang.SuperPowers.self;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;
import static jl95.lang.SuperPowers.uncheck;

import jl95.net.io.managed.ManagedIStreamSupplier;

public class BytesIStreamReceiver extends IStreamReceiver<byte[]> {

    public static BytesIStreamReceiver of(ManagedIStreamSupplier is) {
        return new BytesIStreamReceiver(is);
    }
    public static BytesIStreamReceiver of(InputStream is) {
        return new BytesIStreamReceiver(ManagedIStreamSupplier.of(is));
    }

    private BytesIStreamReceiver(ManagedIStreamSupplier mis) {
        super(mis);
    }

    @Override protected byte[] deserialize(byte[] data) {
        return data;
    }
}
