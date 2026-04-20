/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.google.gson.internal.bind.CollectionTypeAdapterFactory;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.internal.bind.SqlDateTypeAdapter;
import com.google.gson.internal.bind.TimeTypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
class SimpleObject {
    String name;
    int value;

    SimpleObject(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
class SimpleObjectWithFloat {
    String name;
    Float value;

    SimpleObjectWithFloat(String name, Float value) {
        this.name = name;
        this.value = value;
    }
}
class SimpleObjectWithLong {
    String name;
    Long value;

    SimpleObjectWithLong(String name, Long value) {
        this.name = name;
        this.value = value;
    }
}

public class GsonTest {


    @Test
    public void testToJsonNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testToJsonSimpleObject() {
        Gson gson = new Gson();
        String json = gson.toJson(new SimpleObject("test", 123));
        assertEquals("{\"name\":\"test\",\"value\":123}", json);
    }


    @Test
    public void testFromJsonSimpleObject() {
        Gson gson = new Gson();
        String json = "{\"name\":\"test\",\"value\":123}";
        SimpleObject obj = gson.fromJson(json, SimpleObject.class);
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }


    @Test
    public void testFromJsonWithNullFields() {
        Gson gson = new Gson();
        String json = "{\"name\":\"test\",\"value\":null}";
        SimpleObjectWithDouble obj = gson.fromJson(json, SimpleObjectWithDouble.class);
        assertEquals("test", obj.name);
        assertNull(obj.value);
    }
    
    class SimpleObjectWithDouble {
        String name;
        Double value;
    
        SimpleObjectWithDouble(String name, Double value) {
            this.name = name;
            this.value = value;
        }
    }


    @Test
    public void testFromJsonEmptyArray() {
        Gson gson = new Gson();
        String json = "[]";
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(0, array.length());
    }


    @Test
    public void testFromJsonWithNullToken() {
        Gson gson = new Gson();
        String json = "{\"field\": null}";
        SimpleObjectWithDouble obj = gson.fromJson(json, SimpleObjectWithDouble.class);
        assertNull(obj.value);
    }


    @Test
    public void testToJsonWithNonExecutableJson() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new SimpleObject("test", 123));
        assertTrue(json.startsWith(")]}'\n"));
    }


    @Test
    public void testFromJsonNullJsonElement() {
        Gson gson = new Gson();
        SimpleObject obj = gson.fromJson((JsonElement) null, SimpleObject.class);
        assertNull(obj);
    }


    @Test
    public void testNewJsonWriterPrettyPrinting() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new SimpleObject("test", 123));
        assertTrue(json.contains("\n"));
        assertTrue(json.contains("  ")); // Check for indentation
    }


    @Test
    public void testFromJsonReaderEOFHandling() {
        Gson gson = new Gson();
        StringReader reader = new StringReader(""); // Simulate EOF
        SimpleObject obj = gson.fromJson(reader, SimpleObject.class);
        assertNull(obj);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithInvalidDoubleValue() {
        Gson gson = new Gson();
        SimpleObjectWithDouble obj = new SimpleObjectWithDouble("test", Double.NaN);
        gson.toJson(obj);
    }


    @Test
    public void testFromJsonWithFloatFieldSetToNullAndHandleNullValue() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        String json = "{\"name\":\"test\",\"value\":null}";
        SimpleObjectWithFloat obj = gson.fromJson(json, SimpleObjectWithFloat.class);
        assertNull(obj.value);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithInvalidLongValueAndHandleError() {
        Gson gson = new Gson();
        String json = "{\"value\":\"not_a_long\"}";
        gson.fromJson(json, SimpleObjectWithLong.class);
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray array = new AtomicLongArray(0);
        String json = gson.toJson(array);
        assertEquals("[]", json);
    }


    @Test
    public void testFromJsonWithEmptyAtomicLongArray() {
        Gson gson = new Gson();
        String json = "[]";
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(0, array.length());
    }


    @Test
    public void testFromJsonWithNullJsonElement() {
        Gson gson = new Gson();
        SimpleObject obj = gson.fromJson((JsonElement) null, SimpleObject.class);
        assertNull(obj); // Ensure that null JsonElement returns null
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCheckValidFloatingPointWithNaN() {
        Gson gson = new Gson();
        SimpleObjectWithDouble obj = new SimpleObjectWithDouble("test", Double.NaN);
        gson.toJson(obj);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCheckValidFloatingPointWithInfinite() {
        Gson gson = new Gson();
        SimpleObjectWithDouble obj = new SimpleObjectWithDouble("test", Double.POSITIVE_INFINITY);
        gson.toJson(obj);
    }


    @Test
    public void testFromJsonEmptyJsonDocument() {
        Gson gson = new Gson();
        StringReader reader = new StringReader("{}"); // Simulate empty JSON object
        SimpleObject obj = gson.fromJson(reader, SimpleObject.class);
        assertNotNull(obj); // The object should not be null
        assertNull(obj.name); // Default value for name should be null
        assertEquals(0, obj.value); // Default value for int should be 0
    }


    @Test
    public void testToJsonWithLongFieldSetToNull() {
        Gson gson = new Gson();
        SimpleObjectWithLong obj = new SimpleObjectWithLong("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json); // Verify that the Long field is not included
    }


    @Test
    public void testToJsonWithFloatFieldSetToNull() {
        Gson gson = new Gson();
        SimpleObjectWithFloat obj = new SimpleObjectWithFloat("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json); // Verify that the Float field is not included
    }


    @Test
    public void testFromJsonArrayWithNullValues() {
        Gson gson = new Gson();
        String json = "[null, 1, 2]";
        List<Object> result = gson.fromJson(json, List.class);
        assertEquals(3, result.size());
        assertNull(result.get(0)); // Verify that the first element is null
        assertEquals(1.0, result.get(1)); // Verify second element
        assertEquals(2.0, result.get(2)); // Verify third element
    }


    @Test
    public void testToJsonWithNullJsonElement() {
        Gson gson = new Gson();
        String json = gson.toJson((JsonElement) null);
        assertEquals("null", json);
    }


    @Test
    public void testFromJsonWithEmptyJsonString() {
        Gson gson = new Gson();
        String json = ""; // Simulate EOF
        SimpleObject obj = gson.fromJson(json, SimpleObject.class);
        assertNull(obj);
    }


    @Test
    public void testToJsonWithNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testFromJsonWithEmptyJsonDocument() {
        Gson gson = new Gson();
        String json = ""; // Provide an empty string as the JSON input
        SimpleObject obj = gson.fromJson(json, SimpleObject.class);
        assertNull(obj); // Verify that the method returns null without throwing any exceptions
    }


    @Test
    public void testFromJsonWithNullValueInJsonReader() {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader("null")); // Use a JsonReader that reads a null value
        SimpleObject obj = gson.fromJson(reader, SimpleObject.class);
        assertNull(obj); // Check that the method returns null without throwing any exceptions
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

