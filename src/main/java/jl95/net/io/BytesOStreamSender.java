package jl95.net.io;

import java.io.OutputStream;

import jl95.net.io.managed.ManagedOStreamSupplier;

public class BytesOStreamSender extends OStreamSender<byte[]> {

    public static BytesOStreamSender of(ManagedOStreamSupplier os) {
        return new BytesOStreamSender(os);
    }
    public static BytesOStreamSender of(OutputStream os) {
        return new BytesOStreamSender(ManagedOStreamSupplier.of(os));
    }

    private BytesOStreamSender(ManagedOStreamSupplier mos) {
        super(mos);
    }

    @Override protected byte[] serialize(byte[] data) {
        return data;
    }
}