package jl95.net.io;

import java.io.OutputStream;

import jl95.net.io.managed.ManagedOs;

public class BytesIStreamSender extends IStreamSender<byte[]> {

    public static BytesIStreamSender of(ManagedOs os) {
        return new BytesIStreamSender(os);
    }
    public static BytesIStreamSender of(OutputStream os) {
        return new BytesIStreamSender(ManagedOs.of(os));
    }

    private BytesIStreamSender(ManagedOs mos) {
        super(mos);
    }

    @Override protected byte[] serialize(byte[] data) {
        return data;
    }
}