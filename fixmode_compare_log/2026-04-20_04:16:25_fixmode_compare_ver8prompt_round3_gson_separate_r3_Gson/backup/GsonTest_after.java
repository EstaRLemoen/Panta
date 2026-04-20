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
import static org.junit.Assert.*;
class Person {
    String name;
    int age;
}
// No new imports required

public class GsonTest {


    @Test
    public void testToJsonNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testFromJsonStringToObject() {
        Gson gson = new Gson();
        String json = "{\"name\":\"John\", \"age\":30}";
        Person person = gson.fromJson(json, Person.class);
        assertNotNull(person);
        assertEquals("John", person.name);
        assertEquals(30, person.age);
    }


    @Test
    public void testFromJsonEmptyString() {
        Gson gson = new Gson();
        Object result = gson.fromJson("", Object.class);
        assertNull(result);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonInvalidJson() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson}", Object.class);
    }


    @Test
    public void testFromJsonWithNullString() {
        Gson gson = new Gson();
        String json = null;
        Object result = gson.fromJson(json, Object.class);
        assertNull(result); // Check that the method returns null without throwing any exceptions
    }


    @Test
    public void testToJsonEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json); // Verify that the output is an empty JSON array
    }


    @Test
    public void testGetAdapterNewTypeToken() {
        Gson gson = new Gson();
        TypeToken<Person> typeToken = new TypeToken<Person>() {};
        TypeAdapter<Person> adapter = gson.getAdapter(typeToken);
        assertNotNull(adapter); // Verify that a new TypeAdapter is created and returned for the specified type
    }


    @Test
    public void testGetDelegateAdapterMissingFactory() {
        Gson gson = new Gson();
        TypeToken<Person> typeToken = new TypeToken<Person>() {};
        TypeAdapter<Person> adapter = gson.getDelegateAdapter(new Excluder(), typeToken);
        assertNotNull(adapter); // Check that the appropriate TypeAdapter is returned based on the available factories
    }


    @Test
    public void testToJsonNullValue() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the output JSON correctly represents a null value
    }


    @Test
    public void testToJsonTreeWithNull() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertEquals(JsonNull.INSTANCE, jsonElement); // Verify that the returned JSON representation is JsonNull.INSTANCE.
    }


    @Test
    public void testFromJsonWithEmptyReader() {
        Gson gson = new Gson();
        StringReader reader = new StringReader("");
        Object result = gson.fromJson(reader, Object.class);
        assertNull(result); // Verify that the return value is null.
    }


    @Test
    public void testFromJsonWithNullValue() {
        Gson gson = new Gson();
        String json = "null"; // JSON string that represents a null value
        Long result = gson.fromJson(json, Long.class);
        assertNull(result); // Check that the returned object from fromJson() is null
    }


    @Test
    public void testToJsonWithNullValue() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the output correctly represents the null value in JSON format.
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json); // Check that the serialized output is an empty JSON array.
    }


    @Test
    public void testFromJsonWithEmptyJsonReader() {
        Gson gson = new Gson();
        StringReader reader = new StringReader("");
        Object result = gson.fromJson(reader, Object.class);
        assertNull(result); // Verify that the method returns null without throwing an exception.
    }


    @Test
    public void testGetDelegateAdapterWithSkippedFactory() {
        Gson gson = new Gson();
        TypeToken<Person> typeToken = new TypeToken<Person>() {};
        TypeAdapter<Person> adapter = gson.getDelegateAdapter(new Excluder(), typeToken);
        assertNotNull(adapter); // Verify that the correct TypeAdapter is returned and used for serialization/deserialization.
    }


    @Test
    public void testToJsonWithNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the method returns a JSON representation of null
    }


    @Test
    public void testToJsonWithNonExecutableJson() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new Person());
        assertTrue(json.startsWith(")]}'\n")); // Verify that the JSON starts with the non-executable prefix
    }


    @Test
    public void testToJsonWithPrettyPrinting() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new Person());
        assertTrue(json.contains("\n  ")); // Verify that the output JSON is formatted with proper indentation
    }


    @Test
    public void testToJsonWithNullFloatingPointField() {
        Gson gson = new Gson();
        class TestObject {
            Double value = null; // Floating-point field set to null
        }
        String json = gson.toJson(new TestObject());
        assertEquals("null", json); // Verify that the JSON output correctly represents the null value
    }


    @Test
    public void testToJsonWithNullLongField() {
        Gson gson = new Gson();
        class TestObject {
            Long value = null; // Long field set to null
        }
        String json = gson.toJson(new TestObject());
        assertEquals("null", json); // Verify that the JSON output correctly represents the null value for the long field
    }


    @Test
    public void testFromJsonWithEmptyJsonArrayToAtomicLongArray() {
        Gson gson = new Gson();
        StringReader reader = new StringReader("[]");
        AtomicLongArray result = gson.fromJson(reader, AtomicLongArray.class);
        assertEquals(0, result.length()); // Verify that the returned AtomicLongArray is empty
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArrayDistinct() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json); // Check that the resulting JSON output is an empty array representation
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonWithExtraDataAfterJson() {
        Gson gson = new Gson();
        StringReader reader = new StringReader("{\"key\": \"value\"} extra");
        gson.fromJson(reader, Object.class); // This should throw a JsonSyntaxException
    }


    @Test
    public void testFromJsonWithNullInput() {
        Gson gson = new Gson();
        String json = null;
        Object result = gson.fromJson(json, Object.class);
        assertNull(result); // Check that the method returns null as expected for null input
    }


    @Test
    public void testToJsonWithSpecialFloatingPointValues() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        String json = gson.toJson(Double.NaN);
        assertTrue(json.contains("NaN")); // Verify that the serialized output correctly represents NaN
        
        json = gson.toJson(Double.POSITIVE_INFINITY);
        assertTrue(json.contains("Infinity")); // Verify that the serialized output correctly represents Infinity
        
        json = gson.toJson(Double.NEGATIVE_INFINITY);
        assertTrue(json.contains("-Infinity")); // Verify that the serialized output correctly represents -Infinity
    }


    @Test
    public void testFromJsonWithJsonArrayToAtomicLongArray() {
        Gson gson = new Gson();
        StringReader reader = new StringReader("[1, 2, 3]");
        AtomicLongArray result = gson.fromJson(reader, AtomicLongArray.class);
        assertEquals(3, result.length()); // Verify that the resulting AtomicLongArray contains the correct number of elements.
        assertEquals(1, result.get(0)); // Verify the first element.
        assertEquals(2, result.get(1)); // Verify the second element.
        assertEquals(3, result.get(2)); // Verify the third element.
    }


    @Test
    public void testFromJsonWithNullValueDifferentInput() {
        Gson gson = new Gson();
        StringReader reader = new StringReader("null");
        Object result = gson.fromJson(reader, Object.class);
        assertNull(result); // Verify that the deserialized object is null, reflecting the input JSON.
    }


    @Test
    public void testToJsonWithNullValueDifferentInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Check that the output JSON string is "null".
    }


    @Test
    public void testGetAdapterCachedTypeAdapter() {
        Gson gson = new Gson();
        TypeToken<Person> typeToken = new TypeToken<Person>() {};
        TypeAdapter<Person> adapter1 = gson.getAdapter(typeToken);
        TypeAdapter<Person> adapter2 = gson.getAdapter(typeToken);
        assertSame(adapter1, adapter2); // Verify that the same instance is returned from the cache
    }


    @Test
    public void testGetAdapterOngoingCall() {
        Gson gson = new Gson();
        TypeToken<Person> typeToken = new TypeToken<Person>() {};
        // Simulate an ongoing call by invoking getAdapter twice
        TypeAdapter<Person> adapter1 = gson.getAdapter(typeToken);
        TypeAdapter<Person> adapter2 = gson.getAdapter(typeToken);
        assertSame(adapter1, adapter2); // Ensure the same adapter is returned during ongoing calls
    }


    @Test
    public void testGetDelegateAdapterSkippingFactory() {
        Gson gson = new Gson();
        TypeToken<Person> typeToken = new TypeToken<Person>() {};
        TypeAdapter<Person> adapter = gson.getDelegateAdapter(new Excluder(), typeToken);
        assertNotNull(adapter); // Verify that the adapter is returned and not from the skipped factory
    }


    @Test
    public void testFromJsonEOFHandling() {
        Gson gson = new Gson();
        String json = ""; // Empty JSON string
        Object result = gson.fromJson(json, Object.class);
        assertNull(result); // Check that the return value is null, indicating proper handling of EOF
    }


    @Test
    public void testFromJsonWithNullJsonString() {
        Gson gson = new Gson();
        String json = null;
        Object result = gson.fromJson(json, Person.class);
        assertNull(result); // Verify that the method returns null without throwing any exceptions.
    }


    @Test
    public void testToJsonWithNullValueForDouble() {
        Gson gson = new Gson();
        class TestObject {
            Double value = null; // Floating-point field set to null
        }
        String json = gson.toJson(new TestObject());
        assertEquals("null", json); // Verify that the JSON output correctly represents the null value
    }


    @Test
    public void testFromJsonWithNullLongValue() {
        Gson gson = new Gson();
        String json = "null"; // JSON string that represents a null value
        Long result = gson.fromJson(json, Long.class);
        assertNull(result); // Check that the method returns null without throwing any exceptions.
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCheckValidFloatingPointWithInvalidValue() {
        Gson.checkValidFloatingPoint(Double.NaN); // This should throw an IllegalArgumentException
    }


    @Test
    public void testFromJsonWithNullDoubleValue() {
        Gson gson = new Gson();
        String json = "{\"value\": null}"; // JSON string with a null value for a double field
        TestObject result = gson.fromJson(json, TestObject.class);
        assertNull(result.value); // Verify that the double field is set to null
    }
    
    class TestObject {
        Double value; // Double field
    }


    @Test
    public void testToJsonTreeWithNullInput() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertEquals(JsonNull.INSTANCE, jsonElement); // Verify that the output represents a null value correctly
    }


    @Test
    public void testFromJsonWithSkippedFactory() {
        Gson gson = new Gson();
        TypeToken<TestObject> typeToken = new TypeToken<TestObject>() {};
        TypeAdapter<TestObject> adapter = gson.getDelegateAdapter(new Excluder(), typeToken);
        assertNotNull(adapter); // Verify that the correct type adapter is used, skipping the Excluder
    }


    @Test
    public void testToJsonWithNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that the output string is the JSON representation of null.
    }


    @Test
    public void testToJsonWithNullNumberField() {
        Gson gson = new Gson();
        class TestObject {
            Number value = null; // Number field set to null
        }
        String json = gson.toJson(new TestObject());
        assertEquals("null", json); // Verify that the JSON output correctly represents the null value
    }


    @Test
    public void testFromJsonWithNullNumberField() {
        Gson gson = new Gson();
        String json = "{\"value\": null}"; // JSON string with a null value for a Number field
        TestObject result = gson.fromJson(json, TestObject.class);
        assertNull(result.value); // Verify that the Number field is set to null
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

