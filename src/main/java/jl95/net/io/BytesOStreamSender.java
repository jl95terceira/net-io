package jl95.net.io;

import java.io.OutputStream;

public class BytesOStreamSender extends OStreamSender<byte[]> {

    public static BytesOStreamSender of(ManagedOStream os) {
        return new BytesOStreamSender(os);
    }
    public static BytesOStreamSender of(OutputStream os) {
        return new BytesOStreamSender(ManagedOStream.of(os));
    }

    private BytesOStreamSender(ManagedOStream mos) {
        super(mos);
    }

    @Override protected byte[] serialize(byte[] data) {
        return data;
    }
}