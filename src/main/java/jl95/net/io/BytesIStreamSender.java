package jl95.net.io;

import java.io.OutputStream;

import jl95.net.io.managed.ManagedOStreamSupplier;

public class BytesIStreamSender extends IStreamSender<byte[]> {

    public static BytesIStreamSender of(ManagedOStreamSupplier os) {
        return new BytesIStreamSender(os);
    }
    public static BytesIStreamSender of(OutputStream os) {
        return new BytesIStreamSender(ManagedOStreamSupplier.of(os));
    }

    private BytesIStreamSender(ManagedOStreamSupplier mos) {
        super(mos);
    }

    @Override protected byte[] serialize(byte[] data) {
        return data;
    }
}