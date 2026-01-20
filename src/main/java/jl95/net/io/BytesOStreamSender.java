package jl95.net.io;

import java.io.OutputStream;

import jl95.net.io.util.OutputStreams;

public class BytesOStreamSender extends OStreamSender<byte[]> {

    public static BytesOStreamSender of(Managed<OutputStream> os) {
        return new BytesOStreamSender(os);
    }
    public static BytesOStreamSender of(OutputStream os) {
        return new BytesOStreamSender(OutputStreams.getSimpleManaged(os));
    }

    private BytesOStreamSender(Managed<OutputStream> mos) {
        super(mos);
    }

    @Override protected byte[] serialize(byte[] data) {
        return data;
    }
}