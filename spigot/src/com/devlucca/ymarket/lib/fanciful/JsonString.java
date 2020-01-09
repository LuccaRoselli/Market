package com.devlucca.ymarket.lib.fanciful;

import org.bukkit.configuration.serialization.*;
import com.google.gson.stream.*;
import java.io.*;
import java.util.*;

final class JsonString implements JsonRepresentedObject, ConfigurationSerializable
{
    private String _value;
    
    public JsonString(final CharSequence value) {
        this._value = ((value == null) ? null : value.toString());
    }
    
    @Override
    public void writeJson(final JsonWriter writer) throws IOException {
        writer.value(this.getValue());
    }
    
    public String getValue() {
        return this._value;
    }
    
    public Map<String, Object> serialize() {
        final HashMap<String, Object> theSingleValue = new HashMap<String, Object>();
        theSingleValue.put("stringValue", this._value);
        return theSingleValue;
    }
    
    public static JsonString deserialize(final Map<String, Object> map) {
        return new JsonString(map.get("stringValue").toString());
    }
    
    @Override
    public String toString() {
        return this._value;
    }
}
