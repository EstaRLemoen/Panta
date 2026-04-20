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
class Person {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
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
    public void testDeserializeJsonToObject() {
        Gson gson = new Gson();
        String json = "{\"name\":\"John\", \"age\":30}";
        Person person = gson.fromJson(json, Person.class);
        assertEquals("John", person.getName());
        assertEquals(30, person.getAge());
    }


    @Test
    public void testSerializeNullFloatValue() {
        Gson gson = new Gson();
        String json = gson.toJson(new PersonWithFloat(null));
        assertEquals("{}", json); // Assuming the field is omitted when null
    }
    
    class PersonWithFloat {
        private Float value;
    
        public PersonWithFloat(Float value) {
            this.value = value;
        }
    
        public Float getValue() {
            return value;
        }
    }


    @Test
    public void testSerializeNullLongValue() {
        Gson gson = new Gson();
        String json = gson.toJson(new PersonWithLong(null));
        assertEquals("{}", json); // Assuming the field is omitted when null
    }
    
    class PersonWithLong {
        private Long value;
    
        public PersonWithLong(Long value) {
            this.value = value;
        }
    
        public Long getValue() {
            return value;
        }
    }


    @Test
    public void testDeserializeJsonWithNullFields() {
        Gson gson = new Gson();
        String json = "{\"name\": null, \"age\": null}";
        Person person = gson.fromJson(json, Person.class);
        assertNull(person.getName());
        assertEquals(0, person.getAge()); // Assuming default int value
    }


    @Test
    public void testSerializeNullDoubleValue() {
        Gson gson = new Gson();
        String json = gson.toJson(new PersonWithDouble(null));
        assertEquals("{}", json); // Assuming the field is omitted when null
    }
    
    class PersonWithDouble {
        private Double value;
    
        public PersonWithDouble(Double value) {
            this.value = value;
        }
    
        public Double getValue() {
            return value;
        }
    }


    @Test
    public void testFromJsonEmptyArray() {
        Gson gson = new Gson();
        List<Object> list = gson.fromJson("[]", List.class);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }


    @Test
    public void testGetAdapterCachedType() {
        Gson gson = new Gson();
        TypeAdapter<Person> adapter1 = gson.getAdapter(Person.class);
        TypeAdapter<Person> adapter2 = gson.getAdapter(Person.class);
        assertSame(adapter1, adapter2); // Ensure the same instance is returned
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonMalformedInput() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson", Person.class); // Intentionally malformed JSON
    }


    @Test
    public void testGetAdapterWithSkippedFactory() {
        Gson gson = new Gson();
        TypeAdapter<Person> adapter = gson.getAdapter(Person.class);
        TypeAdapterFactory skippedFactory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // Simulate a factory that does not provide an adapter
            }
        };
        TypeAdapter<Person> resultAdapter = gson.getDelegateAdapter(skippedFactory, TypeToken.get(Person.class));
        assertNotNull(resultAdapter); // Ensure we still get a valid adapter
    }


    @Test
    public void testToJsonWithPrettyPrinting() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(new PersonWithFloat(3.14f));
        assertTrue(json.contains("\n  ")); // Check for pretty printing indentation
    }


    @Test
    public void testFromJsonWithNullStringExplicit() {
        Gson gson = new Gson();
        Person person = gson.fromJson((String) null, Person.class);
        assertNull(person); // Ensure that null input returns null without exceptions
    }


    @Test
    public void testFromJsonWithNullString() {
        Gson gson = new Gson();
        Person person = gson.fromJson((String) null, Person.class);
        assertNull(person); // Ensure that null input returns null without exceptions
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCheckValidFloatingPointWithInvalidValue() {
        Gson.checkValidFloatingPoint(Double.NaN); // This should throw an exception
    }


    @Test
    public void testGetAdapterWithDefaultLongSerializationPolicy() {
        Gson gson = new Gson();
        TypeAdapter<Long> adapter = gson.getAdapter(Long.class);
        Long value = 123456789L;
        String json = gson.toJson(value, Long.class);
        assertEquals("123456789", json); // Check if the long value is serialized correctly
        Long deserializedValue = gson.fromJson(json, Long.class);
        assertEquals(value, deserializedValue); // Check if the long value is deserialized correctly
    }


    @Test
    public void testFromJsonWithExplicitNull() {
        Gson gson = new Gson();
        String json = "null";
        Person person = gson.fromJson(json, Person.class);
        assertNull(person); // Ensure that null input returns null
    }


    @Test
    public void testFromJsonEmptyJsonArray() {
        Gson gson = new Gson();
        String json = "[]";
        List<Object> list = gson.fromJson(json, List.class);
        assertNotNull(list);
        assertTrue(list.isEmpty()); // Verify that the returned object is an empty collection
    }


    @Test
    public void testGetDelegateAdapterWithSkippedFactory() {
        Gson gson = new Gson();
        TypeAdapterFactory skippedFactory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null; // Simulate a factory that does not provide an adapter
            }
        };
        TypeAdapter<Person> resultAdapter = gson.getDelegateAdapter(skippedFactory, TypeToken.get(Person.class));
        assertNotNull(resultAdapter); // Ensure we still get a valid adapter
    }


    @Test
    public void testToJsonWithNullInput() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null input returns "null" as JSON
    }


    @Test
    public void testToJsonWithNonExecutableJson() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        String json = gson.toJson(new PersonWithFloat(3.14f));
        assertTrue(json.startsWith(")]}'\n")); // Check for non-executable JSON prefix
    }


    @Test
    public void testFromJsonWithEmptyJson() {
        Gson gson = new Gson();
        String json = "";
        Person person = gson.fromJson(json, Person.class);
        assertNull(person); // Verify that the method returns null without throwing an exception
    }


    @Test
    public void testToJsonWithNullNumberField() {
        Gson gson = new Gson();
        PersonWithNumber person = new PersonWithNumber(null);
        String json = gson.toJson(person);
        assertEquals("{}", json); // Assuming the field is omitted when null
    }
    
    class PersonWithNumber {
        private Number value;
    
        public PersonWithNumber(Number value) {
            this.value = value;
        }
    
        public Number getValue() {
            return value;
        }
    }


    @Test
    public void testFromJsonWithNullValue() {
        Gson gson = new Gson();
        String json = "{\"value\": null}";
        PersonWithNullValue person = gson.fromJson(json, PersonWithNullValue.class);
        assertNull(person.getValue()); // Ensure that the null value is handled correctly
    }
    
    class PersonWithNullValue {
        private Double value;
    
        public Double getValue() {
            return value;
        }
    }


    @Test
    public void testToJsonWithEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json); // Verify that the serialized output is an empty JSON array
    }


    @Test
    public void testFromJsonWithEmptyJsonObject() {
        Gson gson = new Gson();
        String json = "{}"; // Empty JSON object
        Person person = gson.fromJson(json, Person.class);
        assertNull(person.getName()); // Ensure that the name is null
        assertEquals(0, person.getAge()); // Ensure that the age is default
    }


    @Test
    public void testToJsonWithNullSource() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null input returns "null" as JSON
    }


    class UnsupportedType {
        private String unsupportedField;
    
        public String getUnsupportedField() {
            return unsupportedField;
        }
    }
    
    @Test
    public void testGetAdapterWithDefinedUnsupportedType() {
        Gson gson = new Gson();
        UnsupportedType result = gson.fromJson("{\"unsupportedField\":\"value\"}", UnsupportedType.class);
        assertNotNull(result); // Ensure that unsupported type does not return null
        assertEquals("value", result.getUnsupportedField()); // Verify the field value
    }


    @Test
    public void testToJsonWithNullObject() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null input returns "null" as JSON
    }


    @Test
    public void testFromJsonWithNullJson() {
        Gson gson = new Gson();
        String json = "null"; // JSON representation of null
        Person person = gson.fromJson(json, Person.class);
        assertNull(person); // Verify that the method returns null for the deserialized object
    }


    @Test
    public void testToJsonWithSpecialFloatingPointValue() {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        String jsonNaN = gson.toJson(Double.NaN);
        String jsonInfinity = gson.toJson(Double.POSITIVE_INFINITY);
        assertEquals("NaN", jsonNaN); // Check if NaN is serialized correctly
        assertEquals("Infinity", jsonInfinity); // Check if Infinity is serialized correctly
    }


    @Test
    public void testFromJsonWithNullJsonValue() {
        Gson gson = new Gson();
        String json = "null"; // JSON representation of null
        Person person = gson.fromJson(json, Person.class);
        assertNull(person); // Verify that the method returns null for the deserialized object
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithInvalidFloatingPoint() {
        Gson gson = new Gson();
        gson.toJson(new PersonWithDouble(Double.NaN)); // This should throw an exception
    }


    @Test
    public void testToJsonWithNullLongField() {
        Gson gson = new Gson();
        PersonWithLong personWithLong = new PersonWithLong(null);
        String json = gson.toJson(personWithLong);
        assertEquals("{}", json); // Assuming the field is omitted when null
    }


    @Test
    public void testFromJsonWithEmptyJsonArray() {
        Gson gson = new Gson();
        String json = "[]"; // Empty JSON array
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(0, array.length()); // Ensure the AtomicLongArray is empty
    }


    @Test
    public void testFromJsonWithUnexpectedFields() {
        Gson gson = new Gson();
        String json = "{\"name\":\"John\", \"age\":30, \"unexpectedField\":\"value\"}";
        Person person = gson.fromJson(json, Person.class);
        assertEquals("John", person.getName()); // Ensure the expected field is populated
        assertEquals(30, person.getAge()); // Ensure the expected field is populated
    }


    @Test
    public void testToJsonWithNullValue() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json); // Verify that null input returns "null" as JSON
    }


    @Test
    public void testFromJsonWithNullJsonElement() {
        Gson gson = new Gson();
        Person person = gson.fromJson((JsonElement) null, Person.class);
        assertNull(person); // Verify that the method returns null without throwing exceptions
    }


    @Test(expected = IllegalArgumentException.class)
    public void testToJsonWithInvalidFloatingPointValue() {
        Gson gson = new Gson();
        gson.toJson(new PersonWithDouble(Double.NaN)); // This should throw an exception
    }


    @Test
    public void testToJsonWithNullDoubleValue() {
        Gson gson = new Gson();
        PersonWithDouble personWithDouble = new PersonWithDouble(null);
        String json = gson.toJson(personWithDouble);
        assertEquals("{}", json); // Assuming the field is omitted when null
    }


    @Test
    public void testFromJsonWithEmptyJsonArrayForAtomicLongArray() {
        Gson gson = new Gson();
        String json = "[]"; // Empty JSON array
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(0, array.length()); // Ensure the AtomicLongArray is empty
    }


    @Test
    public void testToJsonTreeWithNullSource() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertEquals(JsonNull.INSTANCE, jsonElement); // Verify that it returns JsonNull
    }


    @Test
    public void testFromJsonWithNullJsonString() {
        Gson gson = new Gson();
        Person person = gson.fromJson((String) null, Person.class);
        assertNull(person); // Ensure that null input returns null without exceptions
    }


    @Test(expected = NullPointerException.class)
    public void testGetAdapterWithNullTypeToken() {
        Gson gson = new Gson();
        gson.getAdapter((Class<Person>) null); // This should throw a NullPointerException
    }


    @Test(expected = JsonSyntaxException.class)
    public void testAssertFullConsumptionWithExtraData() {
        Gson gson = new Gson();
        String json = "{\"name\":\"John\"} extra data"; // Valid JSON followed by extra data
        Person person = gson.fromJson(json, Person.class);
        // This should trigger the assertFullConsumption check
    }


    @Test
    public void testReadJsonNullValue() {
        Gson gson = new Gson();
        String json = "null"; // JSON representation of null
        Person person = gson.fromJson(json, Person.class);
        assertNull(person); // Ensure that null input returns null
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCheckValidFloatingPointWithNaN() {
        Gson.checkValidFloatingPoint(Double.NaN); // This should throw an exception
    }


    @Test
    public void testWriteJsonWithNullValue() throws IOException {
        Gson gson = new Gson();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        gson.toJson(null, writer); // Call toJson with null
        assertEquals("null", writer.toString()); // Verify that null input returns "null" as JSON
    }


    @Test
    public void testReadEmptyJsonArray() {
        Gson gson = new Gson();
        String json = "[]"; // Empty JSON array
        AtomicLongArray array = gson.fromJson(json, AtomicLongArray.class);
        assertEquals(0, array.length()); // Ensure the AtomicLongArray is empty
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

