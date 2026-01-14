package jl95.net.io.collections;

import javax.json.JsonValue;

import jl95.net.io.Sender;
import jl95.net.io.util.SerdesDefaults;

public class SenderAdaptersCollections {

    private SenderAdaptersCollections() {}

    public static Sender<String> asStringSender(Sender<byte[]> sender) {

        return sender.adapted(SerdesDefaults.stringToBytes);
    }
    public static Sender<JsonValue> asJsonSender  (Sender<byte[]> sender) {

        return asStringSender(sender).adapted(SerdesDefaults.jsonToString);
    }
}
