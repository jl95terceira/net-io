package jl95.net.io;

import java.io.InputStream;

import static jl95.lang.SuperPowers.function;
import static jl95.lang.SuperPowers.self;
import static jl95.lang.SuperPowers.sleep;
import static jl95.lang.SuperPowers.strict;
import static jl95.lang.SuperPowers.uncheck;

public class BytesIStreamReceiver extends IStreamReceiver<byte[]> {

    public static BytesIStreamReceiver of(ManagedIStream is) {
        return new BytesIStreamReceiver(is);
    }
    public static BytesIStreamReceiver of(InputStream is) {
        return new BytesIStreamReceiver(ManagedIStream.of(is));
    }

    private BytesIStreamReceiver(ManagedIStream mis) {
        super(mis);
    }

    @Override protected byte[] deserialize(byte[] data) {
        return data;
    }
}
