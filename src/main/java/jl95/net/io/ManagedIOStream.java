package jl95.net.io;

import static jl95.lang.SuperPowers.function;
import static jl95.lang.SuperPowers.uncheck;

import java.io.InputStream;
import java.io.OutputStream;

import jl95.lang.variadic.*;
import jl95.net.io.util.InputStreams;
import jl95.net.io.util.OutputStreams;

public interface ManagedIOStream {

    Managed<InputStream> input();
    Managed<OutputStream> output();

    public static ManagedIOStream of(IOStreamSupplier ios) {
        var mis = InputStreams .getSimpleManaged(ios.getInputStream ());
        var mos = OutputStreams.getSimpleManaged(ios.getOutputStream());
        return new ManagedIOStream() {

            @Override
            public Managed<InputStream> input() {
                return mis;
            }

            @Override
            public Managed<OutputStream> output() {
                return mos;
            }
        };
    }
}
