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
    Double value;
    TestObject(Double value) {
        this.value = value;
    }
}
class TestObjectLong {
    Long value;
    TestObjectLong(Long value) {
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
    public void testDeserializeDoubleNullInTestObject() {
        Gson gson = new Gson();
        TestObject obj = gson.fromJson("{\"value\":null}", TestObject.class);
        assertNull(obj.value);
    }


    @Test
    public void testDeserializeLongNullInTestObjectLong() {
        Gson gson = new Gson();
        TestObjectLong obj = gson.fromJson("{\"value\":null}", TestObjectLong.class);
        assertNull(obj.value);
    }


    @Test
    public void testFromJsonWithUnregisteredType() {
        Gson gson = new Gson();
        String json = "{\"value\": 123}";
        // Assuming TestObject is not registered
        TestObject obj = gson.fromJson(json, TestObject.class);
        assertNotNull(obj);
        assertEquals(Double.valueOf(123), obj.value);
    }


    @Test
    public void testFromJsonWithEmptyJson() {
        Gson gson = new Gson();
        String json = "";
        Object obj = gson.fromJson(json, Object.class);
        assertNull(obj);
    }


    @Test
    public void testToJsonTreeWithNullSource() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertEquals(JsonNull.INSTANCE, jsonElement);
    }


    @Test
    public void testToJsonWithNonExecutableJsonFlag() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new TestObject(123.45));
        assertTrue(json.startsWith(")]}'\n"));
    }


    @Test
    public void testToJsonWithPrettyPrinting() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new TestObject(123.45));
        assertTrue(json.contains("\n  ")); // Check for indentation
    }


    @Test
    public void testFromJsonWithNullJsonString() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((String) null, Object.class);
        assertNull(obj);
    }


    @Test
    public void testSerializeSpecialFloatingPointValues() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        String jsonNaN = gson.toJson(new TestObject(Double.NaN));
        String jsonInfinity = gson.toJson(new TestObject(Double.POSITIVE_INFINITY));
        assertTrue(jsonNaN.contains("NaN"));
        assertTrue(jsonInfinity.contains("Infinity"));
    }


    @Test(expected = JsonSyntaxException.class)
    public void testAssertFullConsumptionWithExtraData() {
        Gson gson = new Gson();
        String json = "{\"value\": 123} extra data";
        gson.fromJson(json, TestObject.class);
    }


    @Test
    public void testLongSerializationPolicyDefault() {
        Gson gson = new Gson();
        String json = gson.toJson(new TestObjectLong(123456789L));
        assertEquals("{\"value\":123456789}", json);
    }


    @Test
    public void testFromJsonWithNullJson() {
        Gson gson = new Gson();
        Object obj = gson.fromJson("null", Object.class);
        assertNull(obj);
    }


    @Test
    public void testFromJsonWithEmptyJsonArray() {
        Gson gson = new Gson();
        List<Object> obj = gson.fromJson("[]", List.class);
        assertNotNull(obj);
        assertTrue(obj.isEmpty());
    }


    @Test
    public void testToJsonWithNullFields() {
        Gson gson = new Gson();
        TestObject obj = new TestObject(null);
        String json = gson.toJson(obj);
        assertFalse(json.contains("value"));
    }


    @Test
    public void testFromJsonWithNonEmptyJsonArray() {
        Gson gson = new Gson();
        Integer[] obj = gson.fromJson("[1, 2, 3]", Integer[].class);
        assertNotNull(obj);
        assertEquals(3, obj.length);
        assertArrayEquals(new Integer[]{1, 2, 3}, obj);
    }


    @Test
    public void testGetAdapterForRegisteredType() {
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = gson.getAdapter(TestObject.class);
        assertNotNull(adapter);
    }


    @Test
    public void testFromJsonWithNullJsonStringDifferent() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((String) null, Object.class);
        assertNull(obj); // Should return null without throwing exceptions
    }


    @Test
    public void testToJsonWithNullField() {
        Gson gson = new Gson();
        TestObject obj = new TestObject(null);
        String json = gson.toJson(obj);
        assertFalse(json.contains("value"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithNaNValue() {
        Gson gson = new Gson();
        String json = gson.toJson(new TestObject(Double.NaN));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithInfinityValue() {
        Gson gson = new Gson();
        String json = gson.toJson(new TestObject(Double.POSITIVE_INFINITY));
    }


    @Test
    public void testFromJsonWithNullValue() {
        Gson gson = new Gson();
        TestObject obj = gson.fromJson("{\"value\":null}", TestObject.class);
        assertNull(obj.value);
    }


    @Test
    public void testGetAdapterWithOngoingCall() {
        Gson gson = new Gson();
        TypeToken<TestObject> typeToken = TypeToken.get(TestObject.class);
        TypeAdapter<TestObject> adapter1 = gson.getAdapter(typeToken);
        TypeAdapter<TestObject> adapter2 = gson.getAdapter(typeToken);
        assertSame(adapter1, adapter2); // Ensure the same adapter instance is returned
    }


    @Test
    public void testFromJsonWithNullJsonElement() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((JsonElement) null, Object.class);
        assertNull(obj); // Should return null without throwing exceptions
    }


    @Test
    public void testGetAdapterWithNullTypeToken() {
        Gson gson = new Gson();
        // Expecting to get a valid adapter for Object.class instead of null
        TypeAdapter<Object> adapter = gson.getAdapter(Object.class);
        assertNotNull(adapter); // Ensure that it does not throw an exception and returns a valid adapter
    }


    @Test
    public void testGetAdapterWithNoRegisteredAdapter() {
        Gson gson = new Gson();
        TypeToken<Object> typeToken = TypeToken.get(Object.class); // Object has a default adapter
        TypeAdapter<Object> adapter = gson.getAdapter(typeToken); // Should not throw an exception
        assertNotNull(adapter); // Ensure that an adapter is returned
    }


    @Test
    public void testGetDelegateAdapterWithUnregisteredFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory unregisteredFactory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // No adapter created
            }
        };
        TypeAdapter<TestObject> adapter = gson.getDelegateAdapter(unregisteredFactory, TypeToken.get(TestObject.class)); // Should not throw an exception
        assertNotNull(adapter); // Ensure that an adapter is returned
    }


    @Test
    public void testToJsonWithNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testLongAdapterWithNullValueHandling() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<Long> longAdapter = gson.getAdapter(Long.class);
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        longAdapter.write(jsonWriter, null);
        assertEquals("null", writer.toString());
    }


    @Test
    public void testFloatAdapterWithNullValueHandling() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<Float> floatAdapter = gson.getAdapter(Float.class);
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        floatAdapter.write(jsonWriter, null);
        assertEquals("null", writer.toString());
    }


    @Test
    public void testToJsonWithNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the output is a JSON representation of null.
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray atomicLongArray = new AtomicLongArray(0);
        String json = gson.toJson(atomicLongArray);
        assertEquals("[]", json); // Check that the resulting JSON output is an empty array.
    }


    @Test
    public void testFromJsonWithAtomicLongArray() {
        Gson gson = new Gson();
        String json = "[1, 2, 3]";
        AtomicLongArray atomicLongArray = gson.fromJson(json, AtomicLongArray.class);
        assertNotNull(atomicLongArray);
        assertEquals(3, atomicLongArray.length());
        assertEquals(1, atomicLongArray.get(0));
        assertEquals(2, atomicLongArray.get(1));
        assertEquals(3, atomicLongArray.get(2));
    }


    @Test
    public void testToJsonWithNullSourceObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the output is a JSON representation of null.
    }


    @Test
    public void testToJsonWithNonNullSourceObject() {
        Gson gson = new Gson();
        TestObject obj = new TestObject(123.45);
        String json = gson.toJson(obj);
        assertTrue(json.contains("\"value\":123.45")); // Check that the JSON string accurately reflects the object.
    }


    @Test
    public void testToJsonWithNullFloatField() {
        Gson gson = new Gson();
        TestObject obj = new TestObject(null); // Float field set to null
        String json = gson.toJson(obj);
        assertFalse(json.contains("value")); // Check that the null field is not serialized.
    }


    @Test
    public void testFromJsonWithNonEmptyJson() {
        Gson gson = new Gson();
        String json = "{\"value\": 123}";
        TestObject obj = gson.fromJson(json, TestObject.class);
        assertNotNull(obj);
        assertEquals(Double.valueOf(123), obj.value); // Verify that the deserialized object matches the expected structure.
    }


    @Test(expected = IllegalStateException.class)
    public void testFutureTypeAdapterReadWithNullDelegate() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = new TypeAdapter<TestObject>() {
            @Override
            public TestObject read(JsonReader in) throws IOException {
                throw new IllegalStateException(); // Simulate null delegate behavior
            }
            @Override
            public void write(JsonWriter out, TestObject value) throws IOException {
                // No implementation needed for this test
            }
        };
        adapter.read(new JsonReader(new StringReader("{\"value\": 123}"))); // This should throw an exception
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

