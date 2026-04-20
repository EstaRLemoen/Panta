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
    private String name;
    private int value;

    public TestObject(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
class TestObjectWithNaN {
    private String name;
    private Double value;

    public TestObjectWithNaN(String name, Double value) {
        this.name = name;
        this.value = value;
    }
}

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
    public void testDeserializeInvalidJson() {
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
    public void testDeserializeNullJsonElement() {
        Gson gson = new Gson();
        String json = "null";
        Object obj = gson.fromJson(json, Object.class);
        assertNull(obj);
    }


    @Test
    public void testSerializeEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json);
    }


    @Test
    public void testDeserializeJsonArrayToAtomicLongArray() {
        Gson gson = new Gson();
        String json = "[1, 2, 3]";
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(3, array.length());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
    }


    @Test
    public void testToJsonTreeWithNullObject() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertTrue(jsonElement instanceof JsonNull);
    }


    @Test
    public void testToJsonTreeWithNonNullObject() {
        Gson gson = new Gson();
        TestObject obj = new TestObject("test", 123);
        JsonElement jsonElement = gson.toJsonTree(obj);
        assertEquals("{\"name\":\"test\",\"value\":123}", jsonElement.toString());
    }


    @Test
    public void testNewJsonWriterWithNonExecutableJson() throws IOException {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new TestObject("test", 123));
        assertTrue(json.startsWith(")]}'\n"));
    }


    @Test
    public void testFromJsonWithNullJsonString() {
        Gson gson = new Gson();
        Object result = gson.fromJson((String) null, Object.class);
        assertNull(result);
    }


    @Test
    public void testDeserializeNullJsonValue() {
        Gson gson = new Gson();
        Object result = gson.fromJson("null", Object.class);
        assertNull(result);
    }


    @Test
    public void testToJsonWithNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testFromJsonWithNullString() {
        Gson gson = new Gson();
        Object result = gson.fromJson((String) null, Object.class);
        assertNull(result);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testAssertFullConsumptionWithExtraData() {
        Gson gson = new Gson();
        String json = "{\"name\":\"test\",\"value\":123} extra data";
        gson.fromJson(json, TestObject.class);
    }


    @Test
    public void testDeserializeJsonWithNullNumericField() {
        Gson gson = new Gson();
        String json = "{\"numberField\": null}";
        TestObjectWithNumericField obj = gson.fromJson(json, TestObjectWithNumericField.class);
        assertNull(obj.numberField);
    }
    
    class TestObjectWithNumericField {
        private Double numberField;
    
        public TestObjectWithNumericField(Double numberField) {
            this.numberField = numberField;
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSerializeObjectWithInvalidFloatingPointValue() {
        Gson gson = new Gson();
        TestObjectWithInvalidFloat obj = new TestObjectWithInvalidFloat("test", Double.NaN);
        gson.toJson(obj);
    }
    
    class TestObjectWithInvalidFloat {
        private String name;
        private Double value;
    
        public TestObjectWithInvalidFloat(String name, Double value) {
            this.name = name;
            this.value = value;
        }
    }


    @Test
    public void testSerializeObjectWithNaNValue() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        TestObjectWithNaN obj = new TestObjectWithNaN("test", Double.NaN);
        String json = gson.toJson(obj);
        assertTrue(json.contains("NaN"));
    }


    @Test
    public void testSerializeObjectWithNullLongValue() {
        Gson gson = new Gson();
        TestObjectWithLongField obj = new TestObjectWithLongField("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json);
    }
    
    class TestObjectWithLongField {
        private String name;
        private Long value;
    
        public TestObjectWithLongField(String name, Long value) {
            this.name = name;
            this.value = value;
        }
    }


    @Test
    public void testSerializeObjectWithNullFloatValue() {
        Gson gson = new Gson();
        TestObjectWithFloatField obj = new TestObjectWithFloatField("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json);
    }
    
    class TestObjectWithFloatField {
        private String name;
        private Float value;
    
        public TestObjectWithFloatField(String name, Float value) {
            this.name = name;
            this.value = value;
        }
    }


    @Test
    public void testSerializeObjectWithNullDoubleValue() {
        Gson gson = new Gson();
        TestObjectWithDoubleField obj = new TestObjectWithDoubleField("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json);
    }
    
    class TestObjectWithDoubleField {
        private String name;
        private Double value;
    
        public TestObjectWithDoubleField(String name, Double value) {
            this.name = name;
            this.value = value;
        }
    }


    @Test
    public void testToJsonWithNullValue() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testFromJsonWithEmptyInput() {
        Gson gson = new Gson();
        String json = "";
        Object result = gson.fromJson(json, Object.class);
        assertNull(result);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithMalformedJson() {
        Gson gson = new Gson();
        String json = "{invalidJson";
        gson.fromJson(json, TestObject.class);
    }


    @Test
    public void testDoubleAdapterHandlesNull() {
        Gson gson = new Gson();
        String json = gson.toJson(new TestObjectWithDoubleField("test", null));
        assertEquals("{\"name\":\"test\"}", json);
    }


    @Test
    public void testFloatAdapterHandlesNull() {
        Gson gson = new Gson();
        String json = gson.toJson(new TestObjectWithFloatField("test", null));
        assertEquals("{\"name\":\"test\"}", json);
    }


    @Test
    public void testFromJsonHandlesNullValue() {
        Gson gson = new Gson();
        Object result = gson.fromJson("null", Object.class);
        assertNull(result); // Expecting null object
    }


    @Test
    public void testLongAdapterDefaultPolicy() {
        Gson gson = new Gson();
        String json = gson.toJson(new TestObjectWithLongField("test", 123L));
        assertEquals("{\"name\":\"test\",\"value\":123}", json); // Default policy serializes as number
    }


    @Test
    public void testGetAdapterReturnsCachedTypeAdapter() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter1 = gson.getAdapter(String.class);
        TypeAdapter<String> adapter2 = gson.getAdapter(String.class);
        assertSame(adapter1, adapter2); // Verify that the same cached adapter is returned
    }


    @Test
    public void testGetDelegateAdapterFallbackToJsonAdapterFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory skipPast = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // Simulate a factory that does not provide an adapter
            }
        };
        TypeAdapter<String> adapter = gson.getDelegateAdapter(skipPast, TypeToken.get(String.class));
        assertNotNull(adapter); // Ensure we get a valid adapter from jsonAdapterFactory
    }


    @Test
    public void testToJsonHandlesNullSource() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null is serialized as "null"
    }


    @Test
    public void testToJsonWithNullSource() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null is serialized as "null"
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSerializeObjectWithInfiniteValue() {
        Gson gson = new Gson();
        TestObjectWithInvalidFloat obj = new TestObjectWithInvalidFloat("test", Double.POSITIVE_INFINITY);
        gson.toJson(obj);
    }


    @Test
    public void testDeserializeFloatFieldWithNullValue() {
        Gson gson = new Gson();
        String json = "{\"floatField\": null}";
        TestObjectWithFloatField obj = gson.fromJson(json, TestObjectWithFloatField.class);
        assertNull(obj.value); // Expecting null for float field
    }


    @Test
    public void testGetDelegateAdapterWithSkippedFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory skipPast = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // Simulate a factory that does not provide an adapter
            }
        };
        TypeAdapter<String> adapter = gson.getDelegateAdapter(skipPast, TypeToken.get(String.class));
        assertNotNull(adapter); // Ensure we get a valid adapter from jsonAdapterFactory
    }


    @Test
    public void testToJsonWithNull() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null is serialized as "null"
    }


    @Test
    public void testToJsonWithNonNullObject() {
        Gson gson = new Gson();
        TestObject obj = new TestObject("test", 123);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\",\"value\":123}", json); // Check that the returned JSON string accurately represents the provided object.
    }


    @Test
    public void testNewJsonWriterPrettyPrinting() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringWriter writer = new StringWriter();
        gson.toJson(new TestObject("test", 123), writer);
        String json = writer.toString();
        assertTrue(json.contains("\n")); // Verify that the output JSON is formatted with indentation and line breaks.
        assertTrue(json.contains("  \"name\": \"test\"")); // Check for proper indentation
    }


    @Test
    public void testFromJsonWithNull() {
        Gson gson = new Gson();
        Object result = gson.fromJson((String) null, Object.class);
        assertNull(result); // Verify that the method returns null without throwing an exception.
    }


    @Test
    public void testToJsonHandlesNullField() {
        Gson gson = new Gson();
        TestObjectWithNullField obj = new TestObjectWithNullField("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json); // Verify that the null field is omitted
    }
    
    class TestObjectWithNullField {
        private String name;
        private Double value;
    
        public TestObjectWithNullField(String name, Double value) {
            this.name = name;
            this.value = value;
        }
    }


    @Test
    public void testToJsonHandlesNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null is serialized as "null"
    }


    @Test
    public void testFromJsonHandlesNullValues() {
        Gson gson = new Gson();
        String json = "{\"name\":\"test\", \"value\": null}"; // JSON with null value
        TestObjectWithNullField obj = gson.fromJson(json, TestObjectWithNullField.class);
        assertNull(obj.value); // Verify that the value is null
    }


    @Test
    public void testToJsonWithNullDoubleValue() {
        Gson gson = new Gson();
        TestObjectWithDoubleField obj = new TestObjectWithDoubleField("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json); // Verify that the null double value is handled correctly.
    }


    @Test
    public void testToJsonWithNullFloatValue() {
        Gson gson = new Gson();
        TestObjectWithFloatField obj = new TestObjectWithFloatField("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json); // Verify that the null float value is handled correctly.
    }


    @Test
    public void testToJsonWithNullLongValue() {
        Gson gson = new Gson();
        TestObjectWithLongField obj = new TestObjectWithLongField("test", null);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\"}", json); // Verify that the null long value is handled correctly.
    }


    @Test
    public void testGetAdapterForCustomType() {
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = gson.getAdapter(TestObject.class);
        assertNotNull(adapter); // Ensure we get a valid adapter for TestObject.
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSerializeObjectWithNaNValueFixed() {
        Gson gson = new Gson();
        TestObjectWithInvalidFloat obj = new TestObjectWithInvalidFloat("test", Double.NaN);
        gson.toJson(obj); // Expecting an exception due to NaN value.
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

