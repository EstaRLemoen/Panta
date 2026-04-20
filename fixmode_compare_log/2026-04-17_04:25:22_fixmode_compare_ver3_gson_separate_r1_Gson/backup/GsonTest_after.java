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
class MyClass {
    String name;
    int value;

    MyClass(String name, int value) {
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
    public void testMalformedJsonException() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson", Object.class);
    }


    @Test
    public void testSerializeSimpleObject() {
        Gson gson = new Gson();
        MyClass obj = new MyClass("test", 123);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\",\"value\":123}", json);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testDeserializeInvalidDouble() {
        Gson gson = new Gson();
        gson.fromJson("{\"value\":NaN}", MyClass.class);
    }


    @Test
    public void testDeserializeNullValue() {
        Gson gson = new Gson();
        MyClass obj = gson.fromJson("null", MyClass.class);
        assertNull(obj);
    }


    @Test
    public void testSerializeEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray atomicLongArray = new AtomicLongArray(0);
        String json = gson.toJson(atomicLongArray);
        assertEquals("[]", json);
    }


    @Test
    public void testFromJsonWithNullValue() {
        Gson gson = new Gson();
        MyClass obj = gson.fromJson("{\"name\":null,\"value\":123}", MyClass.class);
        assertNull(obj.name);
        assertEquals(123, obj.value);
    }


    @Test
    public void testToJsonTreeWithNullSource() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(null);
        assertTrue(jsonElement.isJsonNull());
    }


    @Test
    public void testNewJsonWriterWithNonExecutableJson() throws IOException {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();
        StringWriter writer = new StringWriter();
        gson.toJson(new MyClass("test", 123), writer);
        String jsonOutput = writer.toString();
        assertTrue(jsonOutput.startsWith(")]}'\n"));
    }


    @Test
    public void testDeserializeEmptyJsonArray() {
        Gson gson = new Gson();
        List<MyClass> obj = gson.fromJson("[]", new TypeToken<List<MyClass>>(){}.getType());
        assertNotNull(obj);
        assertTrue(obj.isEmpty());
    }


    @Test
    public void testDeserializeNonEmptyJsonArray() {
        Gson gson = new Gson();
        String json = "[{\"name\":\"test1\",\"value\":1}, {\"name\":\"test2\",\"value\":2}]";
        List<MyClass> obj = gson.fromJson(json, new TypeToken<List<MyClass>>(){}.getType());
        assertNotNull(obj);
        assertEquals(2, obj.size());
        assertEquals("test1", obj.get(0).name);
        assertEquals(1, obj.get(0).value);
        assertEquals("test2", obj.get(1).name);
        assertEquals(2, obj.get(1).value);
    }


    @Test
    public void testToJsonWithNull() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        assertEquals("null", json);
    }


    @Test
    public void testNewJsonWriterWithPrettyPrinting() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringWriter writer = new StringWriter();
        gson.toJson(new MyClass("test", 123), writer);
        String jsonOutput = writer.toString();
        assertTrue(jsonOutput.contains("\n  "));
    }


    @Test
    public void testFromJsonWithNullJsonElement() {
        Gson gson = new Gson();
        JsonElement jsonElement = null;
        MyClass obj = gson.fromJson(jsonElement, MyClass.class);
        assertNull(obj);
    }


    @Test
    public void testFromJsonWithEmptyJsonDocument() {
        Gson gson = new Gson();
        MyClass obj = gson.fromJson("", MyClass.class);
        assertNull(obj);
    }


    @Test
    public void testToJsonWithNormalDoubleValue() {
        Gson gson = new Gson();
        MyClass obj = new MyClass("test", 123);
        String json = gson.toJson(obj);
        assertEquals("{\"name\":\"test\",\"value\":123}", json);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

