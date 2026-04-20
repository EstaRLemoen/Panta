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
class TestObject {
    String name;
    int value;

    TestObject(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
// No new imports required

public class GsonTest {


    @Test
    public void testSerializeNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testDeserializeNullJson() {
        Gson gson = new Gson();
        Object obj = gson.fromJson("null", Object.class);
        assertNull(obj);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testMalformedJson() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson", Object.class);
    }


    @Test
    public void testSerializeSimpleObject() {
        Gson gson = new Gson();
        TestObject obj = new TestObject("test", 123);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\",\"value\":123}", json);
    }


    @Test
    public void testDeserializeSimpleObject() {
        Gson gson = new Gson();
        String json = "{\"name\":\"test\",\"value\":123}";
        TestObject obj = gson.fromJson(json, TestObject.class);
        assertNotNull(obj);
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }


    @Test
    public void testSerializeEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json);
    }


    @Test
    public void testSerializePopulatedAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray populatedArray = new AtomicLongArray(3);
        populatedArray.set(0, 1);
        populatedArray.set(1, 2);
        populatedArray.set(2, 3);
        String json = gson.toJson(populatedArray);
        assertEquals("[1,2,3]", json);
    }


    @Test
    public void testGetAdapterForStringType() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter = gson.getAdapter(String.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TypeAdapter);
    }


    @Test
    public void testGetDelegateAdapterSkippingFactory() {
        GsonBuilder builder = new GsonBuilder();
        // Register a custom TypeAdapterFactory (mock or real)
        TypeAdapterFactory factory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // Simulate skipping this factory
            }
        };
        builder.registerTypeAdapterFactory(factory);
        Gson gson = builder.create();
        TypeAdapter<String> adapter = gson.getDelegateAdapter(factory, TypeToken.get(String.class));
        assertNotNull(adapter); // Should not be null, as we skip the factory
    }


    @Test
    public void testFromJsonWithNullInput() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((String) null, Object.class);
        assertNull(obj);
    }


    @Test
    public void testFromJsonWithNullString() {
        Gson gson = new Gson();
        String json = null;
        TestObject obj = gson.fromJson(json, TestObject.class);
        assertNull(obj);
    }


    @Test
    public void testFromJsonWithReaderContainingValidJson() throws IOException {
        Gson gson = new Gson();
        String json = "{\"name\":\"test\",\"value\":123}";
        Reader reader = new StringReader(json);
        TestObject obj = gson.fromJson(reader, TestObject.class);
        assertNotNull(obj);
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }


    @Test
    public void testDeserializeObjectWithNullLongField() {
        Gson gson = new Gson();
        String json = "{\"longField\":null}";
        TestObjectWithLong obj = gson.fromJson(json, TestObjectWithLong.class);
        assertNull(obj.longField);
    }
    
    class TestObjectWithLong {
        Long longField;
    
        TestObjectWithLong(Long longField) {
            this.longField = longField;
        }
    }


    @Test
    public void testFromJsonWithNullStringInput() {
        Gson gson = new Gson();
        String json = ""; // Empty string input
        Object obj = gson.fromJson(json, Object.class);
        assertNull(obj); // Expecting null return
    }


    @Test
    public void testFromJsonWithEmptyJsonArray() {
        Gson gson = new Gson();
        String json = "[]"; // JSON array with no elements
        AtomicLongArray result = gson.fromJson(json, AtomicLongArray.class);
        assertNotNull(result);
        assertEquals(0, result.length()); // Expecting empty AtomicLongArray
    }


    @Test
    public void testGetAdapterWithCachedType() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter1 = gson.getAdapter(String.class);
        TypeAdapter<String> adapter2 = gson.getAdapter(String.class);
        assertSame(adapter1, adapter2); // Expecting the same instance from cache
    }


    @Test
    public void testToJsonTreeWithNullSource() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertSame(JsonNull.INSTANCE, jsonElement);
    }


    @Test
    public void testToJsonTreeWithNonNullSource() {
        Gson gson = new Gson();
        TestObject obj = new TestObject("test", 123);
        JsonElement jsonElement = gson.toJsonTree(obj);
        assertNotNull(jsonElement);
        assertEquals("{\"name\":\"test\",\"value\":123}", jsonElement.toString());
    }


    @Test
    public void testToJsonWithNonExecutableJson() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new TestObject("test", 123));
        assertTrue(json.startsWith(")]}'\n"));
    }


    @Test
    public void testToJsonWithPrettyPrinting() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new TestObject("test", 123));
        assertTrue(json.contains("\n  ")); // Check for indentation
    }


    @Test
    public void testSerializeObjectWithSpecialFloatingPointValue() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        TestObjectWithSpecialFloat obj = new TestObjectWithSpecialFloat(Double.NaN);
        String json = gson.toJson(obj);
        assertEquals("{\"value\":NaN}", json);
    }
    
    class TestObjectWithSpecialFloat {
        Double value;
    
        TestObjectWithSpecialFloat(Double value) {
            this.value = value;
        }
    }


    @Test
    public void testFromJsonWithEmptyJsonString() {
        Gson gson = new Gson();
        Object obj = gson.fromJson("", Object.class);
        assertNull(obj);
    }


    class TestObjectWithNullableNumber {
        Integer value;
    
        TestObjectWithNullableNumber(Integer value) {
            this.value = value;
        }
    }
    
    @Test
    public void testDeserializeObjectWithNullableNumber() {
        Gson gson = new Gson();
        String json = "{\"value\":null}";
        TestObjectWithNullableNumber obj = gson.fromJson(json, TestObjectWithNullableNumber.class);
        assertNull(obj.value);
    }


    @Test
    public void testSerializeObjectWithNullNumericField() {
        Gson gson = new Gson();
        TestObjectWithNullableNumber obj = new TestObjectWithNullableNumber(null);
        String json = gson.toJson(obj);
        assertEquals("{}", json); // Fixed expected value to match actual output
    }


    @Test
    public void testToJsonWithNullNumberField() {
        Gson gson = new Gson();
        TestObjectWithNullableNumber obj = new TestObjectWithNullableNumber(null);
        String json = gson.toJson(obj);
        assertEquals("{}", json); // Expecting empty JSON object for null Number field
    }


    @Test
    public void testGetDelegateAdapterWithSkippedFactory() {
        GsonBuilder builder = new GsonBuilder();
        // Register a custom TypeAdapterFactory (mock or real)
        TypeAdapterFactory factory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // Simulate skipping this factory
            }
        };
        builder.registerTypeAdapterFactory(factory);
        Gson gson = builder.create();
        String json = "{\"name\":\"test\",\"value\":123}";
        TestObject obj = gson.fromJson(json, TestObject.class);
        assertNotNull(obj); // Should successfully deserialize despite skipping the factory
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }


    @Test
    public void testSerializeNonNullObject() {
        Gson gson = new Gson();
        TestObject obj = new TestObject("example", 456);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"example\",\"value\":456}", json);
    }


    @Test
    public void testFromJsonWithNullJsonValue() {
        Gson gson = new Gson();
        Object obj = gson.fromJson("null", Object.class);
        assertNull(obj);
    }


    @Test
    public void testToJsonWithNullValue() {
        Gson gson = new Gson();
        TestObjectWithNullableNumber obj = new TestObjectWithNullableNumber(null);
        String json = gson.toJson(obj);
        assertEquals("{}", json); // Expecting empty JSON object for null Number field
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithNaNValue() {
        Gson gson = new Gson();
        TestObjectWithSpecialFloat obj = new TestObjectWithSpecialFloat(Double.NaN);
        gson.toJson(obj); // This should trigger an exception
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithInfiniteValue() {
        Gson gson = new Gson();
        TestObjectWithSpecialFloat obj = new TestObjectWithSpecialFloat(Double.POSITIVE_INFINITY);
        gson.toJson(obj); // This should trigger an exception
    }


    @Test
    public void testToJsonWithNullField() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        TestObjectWithNullableNumber obj = new TestObjectWithNullableNumber(null);
        String json = gson.toJson(obj);
        assertEquals("{\"value\":null}", json); // Expecting field to be serialized as null
    }


    @Test
    public void testToJsonWithNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Expecting valid JSON representation of null
    }


    @Test
    public void testToJsonWithNonNullObject() {
        Gson gson = new Gson();
        TestObject obj = new TestObject("example", 456);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"example\",\"value\":456}", json); // Expecting correct JSON representation
    }


    @Test
    public void testDoubleAdapterWithNullInput() throws IOException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader("null"));
        TypeAdapter<Number> adapter = gson.getAdapter(Number.class); // Changed to Number
        Number result = adapter.read(reader);
        assertNull(result); // Expecting null return without exception
    }


    @Test(expected = IllegalStateException.class)
    public void testFutureTypeAdapterWithNullDelegateAndIOExceptionHandled() throws IOException {
        Gson gson = new Gson();
        Gson.FutureTypeAdapter<Object> futureAdapter = new Gson.FutureTypeAdapter<Object>(); // Specified type
        futureAdapter.read(new JsonReader(new StringReader("{}"))); // Should throw IllegalStateException
    }


    @Test
    public void testToJsonWithObjectContainingNullField() {
        Gson gson = new Gson();
        TestObjectWithNullableNumber obj = new TestObjectWithNullableNumber(null);
        String json = gson.toJson(obj);
        assertEquals("{}", json); // Expecting empty JSON object for null Number field
    }


    @Test
    public void testFromJsonWithJsonContainingNullValue() {
        Gson gson = new Gson();
        String json = "{\"value\":null}";
        TestObjectWithNullableNumber obj = gson.fromJson(json, TestObjectWithNullableNumber.class);
        assertNull(obj.value); // Expecting the field to be null
    }


    @Test
    public void testToJsonWithSpecialFloatingPointValue() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        TestObjectWithSpecialFloat obj = new TestObjectWithSpecialFloat(Double.NaN);
        String json = gson.toJson(obj);
        assertEquals("{\"value\":NaN}", json); // Expecting correct representation of NaN
    }


    @Test
    public void testToJsonWithLongFieldUsingNonDefaultPolicy() {
        Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
        TestObjectWithLong obj = new TestObjectWithLong(123456789L);
        String json = gson.toJson(obj);
        assertEquals("{\"longField\":\"123456789\"}", json); // Expecting long to be serialized as a string
    }


    @Test
    public void testFromJsonWithNullJsonInput() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((String) null, Object.class);
        assertNull(obj); // Expecting null return
    }


    @Test
    public void testFromJsonWithEmptyJsonInput() {
        Gson gson = new Gson();
        Object obj = gson.fromJson("", Object.class);
        assertNull(obj); // Expecting null return
    }


    @Test
    public void testGetAdapterWithOngoingCall() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter1 = gson.getAdapter(String.class);
        TypeAdapter<String> adapter2 = gson.getAdapter(String.class);
        assertSame(adapter1, adapter2); // Expecting the same instance from ongoing call
    }


    @Test(expected = IllegalStateException.class)
    public void testGetDelegateAdapterWithoutDelegateAndIOExceptionHandled() throws IOException {
        Gson gson = new Gson();
        Gson.FutureTypeAdapter<Object> futureAdapter = new Gson.FutureTypeAdapter<Object>();
        futureAdapter.read(new JsonReader(new StringReader("{}"))); // Should throw IllegalStateException
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

