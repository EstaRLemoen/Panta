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
    private String name;
    private int value;

    public MyClass(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
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
    public void testMalformedJson() {
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


    @Test
    public void testFromJsonEmptyInput() {
        Gson gson = new Gson();
        Object obj = gson.fromJson("", Object.class);
        assertNull(obj);
    }


    @Test(expected = JsonSyntaxException.class)
    public void testFromJsonMalformedInput() {
        Gson gson = new Gson();
        gson.fromJson("{invalidJson", Object.class);
    }


    @Test
    public void testSerializeEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray emptyArray = new AtomicLongArray(0);
        String json = gson.toJson(emptyArray);
        assertEquals("[]", json);
    }


    @Test
    public void testSerializeNonEmptyAtomicLongArray() {
        Gson gson = new Gson();
        AtomicLongArray array = new AtomicLongArray(3);
        array.set(0, 1L);
        array.set(1, 2L);
        array.set(2, 3L);
        String json = gson.toJson(array);
        assertEquals("[1,2,3]", json);
    }


    @Test
    public void testSerializeObjectWithNullField() {
        Gson gson = new Gson();
        MyClass obj = new MyClass(null, 123);
        String json = gson.toJson(obj);
        assertEquals("{\"value\":123}", json);
    }


    @Test
    public void testDeserializeNullJsonString() {
        Gson gson = new Gson();
        MyClass obj = gson.fromJson("null", MyClass.class);
        assertNull(obj);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

