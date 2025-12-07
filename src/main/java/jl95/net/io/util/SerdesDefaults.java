package jl95.net.io.util;

import jl95.lang.variadic.Function1;
import jl95.serdes.*;

import javax.json.JsonValue;
import java.util.Base64;
import java.util.List;

public class SerdesDefaults {

    public static final Function1<byte[], String>
                                    stringToBytes           = StringUTF8ToBytes.get();
    public static final Function1<String, byte[]>
                                    stringFromBytes         = StringUTF8FromBytes.get();
    public static final Function1<JsonValue, String>
                                    stringToJson            = StringToJson  .get();
    public static final Function1<String, JsonValue>
                                    stringFromJson          = StringFromJson.get();
    public static final Function1<JsonValue, Boolean>
                                    boolToJson              = BooleanToJson  .get();
    public static final Function1<Boolean, JsonValue>
                                    boolFromJson            = BooleanFromJson.get();
    public static final Function1<String, JsonValue>
                                    jsonToString            = JsonToString.get();
    public static final Function1<JsonValue, String>
                                    jsonFromString          = JsonFromString.get();
    public static final Function1<byte[], JsonValue>
                                    jsonToBytes             = json   -> stringToBytes.apply
                                                                       (jsonToString .apply(json));
    public static final Function1<JsonValue, byte[]>
                                    jsonFromBytes           = serial -> jsonFromString .apply
                                                                       (stringFromBytes.apply(serial));
    public static final Function1<String, byte[]>
                                    bytesToString           = Base64.getEncoder()::encodeToString;
    public static final Function1<byte[], String>
                                    bytesFromString         = Base64.getDecoder()::decode;
    public static final Function1<JsonValue, Iterable<String>>
                                    listOfStringToJson      = ListOfStringToJson.get();
    public static final Function1<List<String>, JsonValue>
                                    listOfStringFromJson    = ListOfStringFromJson.get();
}
