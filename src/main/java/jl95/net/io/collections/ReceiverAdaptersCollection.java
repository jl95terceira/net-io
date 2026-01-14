package jl95.net.io.collections;

import javax.json.JsonValue;

import jl95.net.io.Receiver;
import jl95.net.io.util.SerdesDefaults;

public class ReceiverAdaptersCollection {

    private ReceiverAdaptersCollection() {}

    public static Receiver<String> asStringReceiver(Receiver<byte[]> receiver) {

        return receiver.adapted(SerdesDefaults.stringFromBytes);
    }
    public static Receiver<JsonValue> asJsonReceiver  (Receiver<byte[]> receiver) {

        return asStringReceiver(receiver).adapted(SerdesDefaults.jsonFromString);
    }
}
