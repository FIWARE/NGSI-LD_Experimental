package org.fiware;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 *
 *   Json utilities
 *
 *   Copyright (c) 2017-18 FIWARE Foundation e.V.
 *
 *   LICENSE: MIT
 *
 *
 */
public class JsonUtilities {
    public static void addValue(JsonObjectBuilder obj, String key, Object value) {
        if (value instanceof Integer) {
            obj.add(key, (Integer)value);
        }
        else if (value instanceof String) {
            obj.add(key, (String)value);
        }
        else if (value instanceof Float) {
            obj.add(key, (Float)value);
        }
        else if (value instanceof Double) {
            obj.add(key, (Double)value);
        }
        else if (value instanceof Boolean) {
            obj.add(key, (Boolean)value);
        }
        else if (value instanceof JsonValue) {
            JsonValue val = (JsonValue)value;
            obj.add(key, val);
        }
        // Add more cases here
    }
}
