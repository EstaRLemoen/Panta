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
import java.util.Arrays;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;

public class GsonTest {


    @Test
    public void testSerializeNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testMalformedJson() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson", Object.class);
    }


    @Test
    public void testSerializeIntegerList() {
        Gson gson = new Gson();
        List<Integer> integers = Arrays.asList(1, 2, 3);
        String json = gson.toJson(integers);
        assertEquals("[1,2,3]", json);
    }


    @Test
    public void testDeserializeObjectWithNullField() {
        Gson gson = new Gson();
        String json = "{\"longField\":null}";
        TestObject obj = gson.fromJson(json, TestObject.class);
        assertNull(obj.getLongField());
    }
    
    private static class TestObject {
        private Long longField;
    
        public Long getLongField() {
            return longField;
        }
    
        public void setLongField(Long longField) {
            this.longField = longField;
        }
    }


    @Test
    public void testSerializeEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json);
    }


    @Test
    public void testFromJsonWithEmptyJson() {
        Gson gson = new Gson();
        String json = "";
        Object result = gson.fromJson(json, Object.class);
        assertNull(result);
    }


    @Test
    public void testToJsonTreeWithNullInput() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertEquals(JsonNull.INSTANCE, jsonElement);
    }


    @Test
    public void testToJsonWithNonExecutableJson() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new TestObject());
        assertTrue(json.startsWith(")]}'\n"));
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithUnexpectedEOF() {
        Gson gson = new Gson();
        gson.fromJson("{", Object.class); // Malformed JSON to trigger EOFException
    }


    @Test
    public void testSerializeObjectWithNullDoubleField() {
        Gson gson = new Gson();
        TestDoubleObject obj = new TestDoubleObject();
        obj.setDoubleField(null);
        String json = gson.toJson(obj);
        assertFalse(json.contains("doubleField"));
    }
    
    private static class TestDoubleObject {
        private Double doubleField;
    
        public Double getDoubleField() {
            return doubleField;
        }
    
        public void setDoubleField(Double doubleField) {
            this.doubleField = doubleField;
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSerializeObjectWithNaNDoubleField() {
        Gson gson = new Gson();
        TestDoubleObject obj = new TestDoubleObject();
        obj.setDoubleField(Double.NaN);
        gson.toJson(obj);
    }


    @Test
    public void testSerializeLongValueWithDefaultPolicy() {
        Gson gson = new Gson();
        TestLongObject obj = new TestLongObject();
        obj.setLongField(123456789L);
        String json = gson.toJson(obj);
        assertEquals("{\"longField\":123456789}", json);
    }
    
    private static class TestLongObject {
        private Long longField;
    
        public Long getLongField() {
            return longField;
        }
    
        public void setLongField(Long longField) {
            this.longField = longField;
        }
    }


    @Test
    public void testFromJsonWithNullValue() {
        Gson gson = new Gson();
        String json = "null";
        TestObject result = gson.fromJson(json, TestObject.class);
        assertNull(result);
    }


    @Test
    public void testFromJsonWithNullString() {
        Gson gson = new Gson();
        String json = "null";
        TestObject result = gson.fromJson(json, TestObject.class);
        assertNull(result);
    }


    @Test
    public void testToJsonWithNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testFromJsonWithEmptyJsonArray() {
        Gson gson = new Gson();
        String json = "[]";
        List<Object> result = gson.fromJson(json, List.class);
        assertTrue(result.isEmpty());
    }


    @Test
    public void testToJsonWithAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray atomicLongArray = new AtomicLongArray(new long[]{1L, 2L, 3L});
        String json = gson.toJson(atomicLongArray);
        assertEquals("[1,2,3]", json);
    }


    @Test
    public void testToJsonWithPrettyPrinting() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        TestObject obj = new TestObject();
        obj.setLongField(123L);
        String json = gson.toJson(obj);
        assertTrue(json.contains("\n")); // Check for new lines indicating pretty printing
        assertTrue(json.contains("  ")); // Check for indentation
    }


    @Test
    public void testToJsonWithNullJsonElement() {
        Gson gson = new Gson();
        JsonWriter writer = new JsonWriter(new StringWriter());
        gson.toJson(null, writer); // Should handle null without exceptions
        // No assertion needed, just ensure it completes without throwing
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithExtraData() {
        Gson gson = new Gson();
        String json = "{\"longField\": 123} extra data"; // Valid JSON followed by extra data
        gson.fromJson(json, TestObject.class); // Should trigger an exception
    }


    @Test(expected = JsonSyntaxException.class)
    public void testAssertFullConsumptionWithExtraData() {
        Gson gson = new Gson();
        String json = "{\"longField\": 123} extra data"; // Valid JSON followed by extra data
        gson.fromJson(json, TestObject.class); // Should trigger an exception
    }


    @Test(expected = JsonSyntaxException.class)
    public void testAssertFullConsumptionWithEOF() {
        Gson gson = new Gson();
        String json = "{\"longField\": 123"; // Malformed JSON to trigger EOFException
        gson.fromJson(json, TestObject.class); // Should trigger an exception
    }


    @Test
    public void testFromJsonWithNullJsonString() {
        Gson gson = new Gson();
        String json = "null";
        TestObject result = gson.fromJson(json, TestObject.class);
        assertNull(result);
    }


    @Test
    public void testFromJsonWithJsonArray() {
        Gson gson = new Gson();
        String json = "[1, 2, 3]";
        AtomicLongArray result = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(3, result.length());
        assertEquals(1L, result.get(0));
        assertEquals(2L, result.get(1));
        assertEquals(3L, result.get(2));
    }


    @Test
    public void testToJsonSerializesNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null is serialized to "null"
    }


    @Test
    public void testFromJsonReturnsNullForNullInputWithExplicitCall() {
        Gson gson = new Gson();
        Object result = gson.fromJson(new JsonReader(new StringReader("null")), Object.class); // Use a valid JsonReader
        assertNull(result); // Verify that null input returns null
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

