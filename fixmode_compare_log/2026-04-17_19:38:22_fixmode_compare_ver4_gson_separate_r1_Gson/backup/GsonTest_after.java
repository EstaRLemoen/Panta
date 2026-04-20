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
import com.google.gson.JsonSyntaxException;
import static org.junit.Assert.assertEquals;
import java.util.List;
import static org.junit.Assert.assertNull;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import static org.junit.Assert.assertFalse;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.TypeAdapter;

public class GsonTest {


    @Test
    public void testToJsonNull() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonInvalid() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson", Object.class);
    }


    @Test
    public void testFromJsonArray() {
        Gson gson = new Gson();
        String json = "[\"one\", \"two\", \"three\"]";
        List<String> list = gson.fromJson(json, List.class);
        assertEquals(3, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
    }


    @Test
    public void testFromJsonNullObject() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((JsonElement) null, Object.class);
        assertNull(obj);
    }


    @Test
    public void testFromJsonWithNullFloat() {
        Gson gson = new Gson();
        String json = "{\"floatValue\":null}";
        TestFloatObject obj = gson.fromJson(json, TestFloatObject.class);
        assertNull(obj.getFloatValue());
    }
    
    private static class TestFloatObject {
        private Float floatValue;
        public Float getFloatValue() {
            return floatValue;
        }
    }


    @Test
    public void testFromJsonWithNullLong() {
        Gson gson = new Gson();
        String json = "{\"longValue\":null}";
        TestLongObject obj = gson.fromJson(json, TestLongObject.class);
        assertNull(obj.getLongValue());
    }
    
    private static class TestLongObject {
        private Long longValue;
        public Long getLongValue() {
            return longValue;
        }
    }


    @Test
    public void testFromJsonWithNullString() {
        Gson gson = new Gson();
        String json = null;
        Object obj = gson.fromJson(json, Object.class);
        assertNull(obj);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithInvalidDoubleValue() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.setValue(Double.NaN);
        gson.toJson(obj);
    }
    private static class TestObject {
        private Double value;
        public void setValue(Double value) {
            this.value = value;
        }
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArray() {
        Gson gson = new Gson();
        TestAtomicLongArrayObject obj = new TestAtomicLongArrayObject();
        String json = gson.toJson(obj);
        assertEquals("{\"longArray\":[]}", json);
    }
    
    private static class TestAtomicLongArrayObject {
        private AtomicLongArray longArray = new AtomicLongArray(0);
    }


    @Test
    public void testFromJsonWithEmptyJsonObject() {
        Gson gson = new Gson();
        String json = "{}";
        TestEmptyObject obj = gson.fromJson(json, TestEmptyObject.class);
        assertNotNull(obj);
    }
    
    private static class TestEmptyObject {
    }


    @Test
    public void testFromJsonWithPopulatedObject() {
        Gson gson = new Gson();
        String json = "{\"name\":\"Test\", \"value\":123}";
        TestPopulatedObject obj = gson.fromJson(json, TestPopulatedObject.class);
        assertNotNull(obj);
        assertEquals("Test", obj.getName());
        assertEquals(123, obj.getValue());
    }
    
    private static class TestPopulatedObject {
        private String name;
        private int value;
    
        public String getName() {
            return name;
        }
    
        public int getValue() {
            return value;
        }
    }


    @Test
    public void testGetAdapterFromCache() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter = gson.getAdapter(String.class);
        assertNotNull(adapter);
        assertEquals(TypeAdapters.STRING, adapter);
    }


    @Test
    public void testGetDelegateAdapterSkippingFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory factory = new TestTypeAdapterFactory();
        gson.getDelegateAdapter(factory, TypeToken.get(String.class));
        // Assuming the factory is registered and we can verify the behavior
    }
    
    private static class TestTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return null; // Simulate skipping this factory
        }
    }


    @Test
    public void testToJsonWithNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testFromJsonWithNullInput() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((String) null, Object.class);
        assertNull(obj);
    }


    @Test
    public void testNewJsonWriterWithNonExecutableJson() throws IOException {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        jsonWriter.beginObject();
        jsonWriter.name("key").value("value");
        jsonWriter.endObject();
        String json = writer.toString();
        assertTrue(json.startsWith(")]}'\n"));
    }


    @Test
    public void testNewJsonWriterWithPrettyPrinting() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        jsonWriter.beginObject();
        jsonWriter.name("key").value("value");
        jsonWriter.endObject();
        String json = writer.toString();
        assertTrue(json.contains("\n  "));
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithExtraData() {
        Gson gson = new Gson();
        String json = "{\"name\":\"Test\"} extra data";
        gson.fromJson(json, TestPopulatedObject.class);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithPositiveInfinityDoubleValue() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.setValue(Double.POSITIVE_INFINITY);
        gson.toJson(obj);
    }


    @Test
    public void testToJsonWithNullNumberField() {
        Gson gson = new Gson();
        TestNumberObject obj = new TestNumberObject();
        obj.setNumberValue(null);
        String json = gson.toJson(obj);
        assertFalse(json.contains("numberValue")); // Ensure the null field is not included
    }
    
    private static class TestNumberObject {
        private Number numberValue;
        public void setNumberValue(Number numberValue) {
            this.numberValue = numberValue;
        }
    }


    @Test
    public void testFromJsonWithAtomicLongArray() {
        Gson gson = new Gson();
        String json = "[1, 2, 3]";
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(3, array.length()); // Ensure the array contains 3 elements
        assertEquals(1, array.get(0)); // Verify the first element
        assertEquals(2, array.get(1)); // Verify the second element
        assertEquals(3, array.get(2)); // Verify the third element
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArrayObject() {
        Gson gson = new Gson();
        TestAtomicLongArrayObject obj = new TestAtomicLongArrayObject();
        String json = gson.toJson(obj);
        assertEquals("{\"longArray\":[]}", json); // Ensure the JSON representation is an empty array
    }


    @Test
    public void testGetDelegateAdapterWithSkippingFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory factory = new TestTypeAdapterFactory();
        TypeAdapter<String> adapter = gson.getDelegateAdapter(factory, TypeToken.get(String.class));
        assertNotNull(adapter); // Ensure we get a valid adapter
    }


    @Test
    public void testToJsonWithSpecialFloatingPointValues() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        TestObject obj = new TestObject();
        obj.setValue(Double.NaN);
        String json = gson.toJson(obj);
        assertTrue(json.contains("NaN")); // Ensure NaN is serialized
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithIncompleteJson() {
        Gson gson = new Gson();
        String json = "{\"name\":\"Test\"} extra data";
        StringReader reader = new StringReader(json);
        gson.fromJson(reader, TestPopulatedObject.class);
    }


    @Test(expected = IllegalStateException.class)
    public void testFutureTypeAdapterWithoutDelegate() throws IOException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"Test\"}"));
        Gson.FutureTypeAdapter<Object> futureAdapter = new Gson.FutureTypeAdapter<Object>();
        futureAdapter.read(reader); // This should throw an exception
    }


    @Test
    public void testFromJsonWithNullValue() {
        Gson gson = new Gson();
        String json = "null";
        Double result = gson.fromJson(json, Double.class);
        assertNull(result); // Verify that the result is null
    }


    @Test
    public void testToJsonWithNullFloatField() {
        Gson gson = new Gson();
        TestFloatObject obj = new TestFloatObject();
        obj.floatValue = null; // Set the float field to null
        String json = gson.toJson(obj);
        assertFalse(json.contains("floatValue")); // Ensure the null field is not included
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithNaNValue() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.setValue(Double.NaN); // Set value to NaN
        gson.toJson(obj); // This should throw an exception
    }


    @Test
    public void testGetAdapterCacheBehavior() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter1 = gson.getAdapter(String.class);
        TypeAdapter<String> adapter2 = gson.getAdapter(String.class);
        assertSame(adapter1, adapter2); // Ensure the same adapter is returned from cache
    }


    @Test
    public void testFromJsonWithNullJsonElement() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((JsonElement) null, Object.class); // Pass null JsonElement
        assertNull(obj); // Verify that the method returns null without throwing an exception
    }


    @Test
    public void testToJsonWithNullDoubleField() {
        Gson gson = new Gson();
        TestDoubleObject obj = new TestDoubleObject();
        obj.setDoubleValue(null); // Set the double field to null
        String json = gson.toJson(obj);
        assertFalse(json.contains("doubleValue")); // Ensure the null field is not included
    }
    
    private static class TestDoubleObject {
        private Double doubleValue;
        public void setDoubleValue(Double doubleValue) {
            this.doubleValue = doubleValue;
        }
    }


    @Test
    public void testFromJsonWithEmptyAtomicLongArray() {
        Gson gson = new Gson();
        String json = "[]"; // JSON representation of an empty array
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(0, array.length()); // Ensure the array is empty
    }


    @Test
    public void testToJsonTreeWithNullSource() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null); // Pass null as the argument
        assertEquals(JsonNull.INSTANCE, jsonElement); // Check that the result is JsonNull.INSTANCE
    }


    @Test
    public void testFromJsonWithNullJson() {
        Gson gson = new Gson();
        Object obj = gson.fromJson((String) null, Object.class);
        assertNull(obj); // Verify that the method returns null without throwing an exception
    }


    @Test
    public void testFromJsonWithEmptyJson() {
        Gson gson = new Gson();
        Object obj = gson.fromJson("", Object.class);
        assertNull(obj); // Check that the method returns null without throwing an exception
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithMalformedJson() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson", Object.class); // This should throw a JsonSyntaxException
    }


    @Test
    public void testFromJsonWithJsonNullValue() {
        Gson gson = new Gson();
        String json = "null";
        Object obj = gson.fromJson(json, Object.class);
        assertNull(obj); // Check that the method returns null without throwing an exception
    }


    @Test
    public void testToJsonWithNullLongFieldUpdated() {
        Gson gson = new Gson();
        TestLongObject obj = new TestLongObject();
        obj.longValue = null; // Set the long field to null
        String json = gson.toJson(obj);
        assertFalse(json.contains("longValue")); // Ensure the null field is not included
    }


    @Test
    public void testToJsonWithNullFloatFieldUpdated() {
        Gson gson = new Gson();
        TestFloatObject obj = new TestFloatObject();
        obj.floatValue = null; // Set the float field to null
        String json = gson.toJson(obj);
        assertFalse(json.contains("floatValue")); // Ensure the null field is not included
    }


    @Test
    public void testToJsonTreeWithNullSourceUpdated() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null); // Pass null as the argument
        assertEquals(JsonNull.INSTANCE, jsonElement); // Check that the result is JsonNull.INSTANCE
    }


    @Test
    public void testToJsonWithNullArgument() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the returned JSON string is equivalent to the representation of JsonNull.
    }


    @Test(expected = AssertionError.class)
    public void testFutureTypeAdapterSetDelegateMultipleTimes() {
        Gson gson = new Gson();
        TypeAdapter<Object> adapter = gson.getAdapter(Object.class); // Get an adapter that requires a delegate
        Gson.FutureTypeAdapter<Object> futureAdapter = new Gson.FutureTypeAdapter<Object>();
        futureAdapter.setDelegate(adapter); // Set the delegate for the first time
        futureAdapter.setDelegate(adapter); // This should throw an AssertionError on the second call
    }


    @Test
    public void testDoubleAdapterWithNullValue() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<Number> doubleAdapter = gson.getAdapter(Number.class); // Get the number adapter
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        doubleAdapter.write(jsonWriter, null); // Pass a null value to the write method
        jsonWriter.flush();
        assertEquals("null", writer.toString()); // Verify that the method handles the null value correctly
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithInfinityValue() {
        Gson gson = new Gson();
        TestObject obj = new TestObject();
        obj.setValue(Double.POSITIVE_INFINITY); // Set value to Infinity
        gson.toJson(obj); // This should throw an exception
    }


    @Test
    public void testFromJsonWithNullJsonValue() {
        Gson gson = new Gson();
        String json = "null"; // JSON representation of null
        Object obj = gson.fromJson(json, Object.class);
        assertNull(obj); // Verify that the method returns null without throwing exceptions
    }


    private static class TestUncachedType {
        private String field;
        public String getField() {
            return field;
        }
        public void setField(String field) {
            this.field = field;
        }
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

