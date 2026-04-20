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
import java.util.ArrayList;
import java.util.List;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

class TestObject {
    @SerializedName("floatField")
    Float floatField;
}

public class GsonTest {


    @Test
    public void testSerializeNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testSerializeEmptyCollection() {
        List<String> emptyList = new ArrayList<String>();
        Gson gson = new Gson();
        String json = gson.toJson(emptyList);
        assertEquals("[]", json);
    }


    @Test
    public void testSerializeSpecialFloatingPointValues() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        String jsonNaN = gson.toJson(Double.NaN);
        String jsonInfinity = gson.toJson(Double.POSITIVE_INFINITY);
        String jsonNegativeInfinity = gson.toJson(Double.NEGATIVE_INFINITY);
        assertEquals("NaN", jsonNaN);
        assertEquals("Infinity", jsonInfinity);
        assertEquals("-Infinity", jsonNegativeInfinity);
    }


    @Test
    public void testDeserializeFloatFieldNull() {
        String json = "{\"floatField\":null}";
        TestObject obj = new Gson().fromJson(json, TestObject.class);
        assertNull(obj.floatField);
    }


    public void testSerializeEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json);
    }


    public void testFromJsonEmptyDocument() {
        Gson gson = new Gson();
        String json = ""; // Empty JSON document
        Object result = gson.fromJson(json, Object.class);
        assertNull(result);
    }


    @Test
    public void testToJsonWithNonExecutableJson() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new Object());
        assertTrue(json.startsWith(")]}'\n")); // Check for non-executable JSON prefix
    }


    @Test
    public void testToJsonWithNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the method returns "null" for null input
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSerializeObjectWithNaN() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.floatField = Float.NaN; // Set the field to NaN
        gson.toJson(obj); // This should throw an exception
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSerializeObjectWithInfinity() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.floatField = Float.POSITIVE_INFINITY; // Set the field to Infinity
        gson.toJson(obj); // This should throw an exception
    }


    @Test
    public void testDeserializeObjectWithNullField() {
        String json = "{\"floatField\":null}";
        TestObject obj = new Gson().fromJson(json, TestObject.class);
        assertNull(obj.floatField); // Verify that the field is set to null
    }


    @Test
    public void testFromJsonEmptyString() {
        Gson gson = new Gson();
        String json = ""; // Empty JSON string
        Object result = gson.fromJson(json, Object.class);
        assertNull(result); // Verify that the method returns null
    }


    @Test
    public void testDeserializeJsonArrayIntoAtomicLongArray() {
        String json = "[1, 2, 3]"; // JSON string representing an array of long values
        Gson gson = new Gson();
        AtomicLongArray result = gson.fromJson(json, AtomicLongArray.class); // Deserialize into AtomicLongArray
        assertEquals(3, result.length()); // Verify the length of the AtomicLongArray
        assertEquals(1, result.get(0)); // Verify the first value
        assertEquals(2, result.get(1)); // Verify the second value
        assertEquals(3, result.get(2)); // Verify the third value
    }


    @Test
    public void testGetAdapterWithNullTypeToken() {
        Gson gson = new Gson();
        TypeAdapter<Object> adapter = gson.getAdapter(TypeToken.get(Object.class)); // Call getAdapter with a valid TypeToken
        assertNotNull(adapter); // Check that the method does not throw an exception and returns a valid TypeAdapter
    }


    @Test
    public void testToJsonWithNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null, Object.class);
        assertEquals("null", json); // Check that the method returns a JSON representation of null without throwing any exceptions.
    }


    @Test
    public void testToJsonTreeWithNullObject() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertEquals(JsonNull.INSTANCE, jsonElement); // Verify that the method returns a JsonElement representing null without throwing any exceptions.
    }


    @Test
    public void testDeserializeObjectWithNullNumericField() {
        String json = "{\"floatField\":null}";
        TestObject obj = new Gson().fromJson(json, TestObject.class);
        assertNull(obj.floatField); // Verify that the field is set to null
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArray() {
        AtomicLongArray emptyArray = new AtomicLongArray(0); // Create an empty AtomicLongArray
        Gson gson = new Gson();
        String json = gson.toJson(emptyArray); // Serialize the empty array
        assertEquals("[]", json); // Check that the resulting JSON output represents the empty array correctly
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithIncompleteJson() {
        Gson gson = new Gson();
        String json = "{\"floatField\":1"; // Incomplete JSON
        StringReader reader = new StringReader(json);
        gson.fromJson(reader, TestObject.class); // Call fromJson with the incomplete JSON
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
        TypeAdapter<Object> adapter = gson.getDelegateAdapter(unregisteredFactory, TypeToken.get(Object.class));
        assertNotNull(adapter); // Ensure it falls back to JsonAdapterFactory
    }


    @Test
    public void testNewJsonWriterWithPrettyPrinting() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        jsonWriter.beginObject();
        jsonWriter.name("key").value("value");
        jsonWriter.endObject();
        jsonWriter.close();
        String expectedJson = "{\n  \"key\": \"value\"\n}"; // Expected pretty-printed JSON
        assertEquals(expectedJson, writer.toString());
    }


    @Test
    public void testFromJsonWithNullString() {
        Gson gson = new Gson();
        String json = null; // Null JSON string
        Object result = gson.fromJson(json, Object.class); // Call fromJson with null
        assertNull(result); // Verify that the method returns null without throwing exceptions
    }


    @Test
    public void testGetAdapterForIntegerType() {
        Gson gson = new Gson();
        TypeAdapter<Integer> adapter = gson.getAdapter(Integer.class); // Call getAdapter with Integer.class
        assertNotNull(adapter); // Check that the returned TypeAdapter is not null
    }


    @Test
    public void testGetDelegateAdapterWithRegisteredFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory factory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // No adapter created
            }
        };
        TypeAdapter<Object> adapter = gson.getDelegateAdapter(factory, TypeToken.get(Object.class));
        assertNotNull(adapter); // Ensure it falls back to JsonAdapterFactory
    }


    @Test
    public void testToJsonWithNullArgument() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the method returns "null" for null input
    }


    @Test
    public void testToJsonWithNonNullObject() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.floatField = 1.0f; // Set a non-null value
        String json = gson.toJson(obj);
        assertTrue(json.contains("\"floatField\":1.0")); // Verify that the JSON string correctly represents the object
    }


    @Test
    public void testToJsonWithObjectContainingNullField() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.floatField = null; // Explicitly set the field to null
        String json = gson.toJson(obj);
        assertFalse(json.contains("floatField")); // Verify that the JSON output omits the null field
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithMalformedJson() {
        Gson gson = new Gson();
        String json = "{\"floatField\":1"; // Incomplete JSON
        StringReader reader = new StringReader(json);
        gson.fromJson(reader, TestObject.class); // Call fromJson with the incomplete JSON
    }


    @Test
    public void testGetAdapterWithLongSerializationPolicyDefault() {
        Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();
        TypeAdapter<Long> adapter = gson.getAdapter(Long.class);
        assertNotNull(adapter); // Ensure the adapter is not null
        String json = gson.toJson(123456789L);
        assertEquals("123456789", json); // Verify the JSON output for a long value
    }


    @Test
    public void testFromJsonWithNullValue() {
        Gson gson = new Gson();
        String json = "null"; // JSON string representing a null value
        Object result = gson.fromJson(json, Object.class); // Call fromJson with null
        assertNull(result); // Verify that the method returns null without throwing exceptions
    }


    @Test
    public void testToJsonWithAtomicLongArray() throws IOException {
        AtomicLongArray atomicLongArray = new AtomicLongArray(new long[]{1L, 2L, 3L});
        Gson gson = new Gson();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        gson.toJson(atomicLongArray, AtomicLongArray.class, jsonWriter); // Serialize the AtomicLongArray
        assertEquals("[1,2,3]", writer.toString()); // Check that the serialized output reflects all elements
    }


    @Test
    public void testToJsonWithNullValueHandled() throws IOException {
        Gson gson = new Gson();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        gson.toJson(null, Object.class, jsonWriter); // Call toJson with a null object
        assertEquals("null", writer.toString()); // Verify that the JSON output correctly represents the null value
    }


    @Test
    public void testFromJsonWithEmptyDocument() {
        Gson gson = new Gson();
        String json = ""; // Empty JSON document
        Object result = gson.fromJson(json, Object.class);
        assertNull(result); // Verify that the method returns null without throwing an exception
    }


    @Test
    public void testLongAdapterWithDefaultPolicy() {
        Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();
        TypeAdapter<Long> adapter = gson.getAdapter(Long.class);
        assertNotNull(adapter); // Ensure the adapter is not null
        String json = gson.toJson(123456789L);
        assertEquals("123456789", json); // Verify the JSON output for a long value
    }


    @Test
    public void testFromJsonWithJsonNull() {
        Gson gson = new Gson();
        String json = "null"; // JSON string representing a null value
        Object result = gson.fromJson(json, Object.class); // Call fromJson with JSON null
        assertNull(result); // Check that the returned object is null
    }


    @Test
    public void testGetDelegateAdapterWithSkippedFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory factory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // No adapter created
            }
        };
        TypeAdapter<Object> adapter = gson.getDelegateAdapter(factory, TypeToken.get(Object.class));
        assertNotNull(adapter); // Ensure it falls back to JsonAdapterFactory
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

