/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.map.AbstractMapDecorator;
import org.apache.commons.collections4.map.AbstractSortedMapDecorator;
import org.apache.commons.collections4.map.FixedSizeMap;
import org.apache.commons.collections4.map.FixedSizeSortedMap;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.collections4.map.LazySortedMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.collections4.map.PredicatedMap;
import org.apache.commons.collections4.map.PredicatedSortedMap;
import org.apache.commons.collections4.map.TransformedMap;
import org.apache.commons.collections4.map.TransformedSortedMap;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.apache.commons.collections4.map.UnmodifiableSortedMap;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class MapUtilsTest {


    @Test
    public void testGetObjectWithNullMap() {
        assertNull(MapUtils.getObject(null, "key"));
    }


    @Test
    public void testGetStringWithNullMap() {
        assertNull(MapUtils.getString(null, "key"));
    }


    @Test
    public void testGetBooleanWithNullMap() {
        assertNull(MapUtils.getBoolean(null, "key"));
    }


    @Test
    public void testGetNumberWithNullMap() {
        assertNull(MapUtils.getNumber(null, "key"));
    }


    @Test
    public void testGetMapWithNullMap() {
        assertNull(MapUtils.getMap(null, "key"));
    }


    @Test
    public void testSafeAddToMapWithNullValue() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.safeAddToMap(map, "key", null);
        assertEquals("", map.get("key"));
    }


    @Test
    public void testSafeAddToMapWithNonNullValue() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.safeAddToMap(map, "key", "value");
        assertEquals("value", map.get("key"));
    }


    @Test
    public void testIsEmptyWithNullMap() {
        assertTrue(MapUtils.isEmpty(null));
    }


    @Test
    public void testIsNotEmptyWithNullMap() {
        assertFalse(MapUtils.isNotEmpty(null));
    }


    @Test
    public void testGetObjectReturnsNullForNullMap() {
        assertNull(MapUtils.getObject(null, "key"));
    }


    @Test
    public void testGetBooleanReturnsTrueForStringTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "true");
        assertTrue(MapUtils.getBoolean(map, "key"));
    }


    @Test
    public void testGetNumberReturnsDefaultOnParseFailure() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "invalid_number");
        assertEquals(Integer.valueOf(42), MapUtils.getNumber(map, "key", 42));
    }


    @Test
    public void testIsEmptyReturnsTrueForEmptyMap() {
        Map<String, Object> map = new HashMap<>();
        assertTrue(MapUtils.isEmpty(map));
    }


    @Test
    public void testSafeAddToMapAddsEmptyStringForNullValue() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.safeAddToMap(map, "key", null);
        assertEquals("", map.get("key"));
    }


    @Test
    public void testGetObjectReturnsNullForNonExistentKey() {
        Map<String, Object> map = new HashMap<>();
        assertNull(MapUtils.getObject(map, "nonExistentKey"));
    }


    @Test
    public void testGetBooleanReturnsTrueForNumericValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 1);
        assertTrue(MapUtils.getBoolean(map, "key"));
    }


    @Test
    public void testGetNumberReturnsNullForInvalidString() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "invalidNumber");
        assertNull(MapUtils.getNumber(map, "key"));
    }


    @Test
    public void testToPropertiesWithNullMap() {
        Properties properties = MapUtils.toProperties(null);
        assertTrue(properties.isEmpty());
    }


    @Test
    public void testGetBooleanReturnsTrueForStringTrueFixed() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "true");
        assertTrue(MapUtils.getBoolean(map, "key"));
    }


    @Test
    public void testGetBooleanReturnsExpectedValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", Boolean.TRUE);
        assertTrue(MapUtils.getBooleanValue(map, "key"));
    }


    @Test
    public void testGetNumberReturnsExpectedValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 123);
        assertEquals(Integer.valueOf(123), MapUtils.getNumber(map, "key"));
    }


    @Test
    public void testGetMapReturnsExpectedValue() {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("innerKey", "innerValue");
        Map<String, Object> map = new HashMap<>();
        map.put("key", innerMap);
        assertEquals(innerMap, MapUtils.getMap(map, "key"));
    }


    @Test
    public void testGetStringReturnsExpectedValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "expectedValue");
        assertEquals("expectedValue", MapUtils.getString(map, "key"));
    }


    @Test
    public void testGetStringReturnsNonNullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "nonNullValue");
        assertEquals("nonNullValue", MapUtils.getString(map, "key"));
    }


    @Test
    public void testGetBooleanReturnsTrueForNonZeroNumber() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 5);
        assertTrue(MapUtils.getBoolean(map, "key"));
    }


    @Test
    public void testGetMapReturnsNullForNullMap() {
        assertNull(MapUtils.getMap(null, "key"));
    }


    @Test
    public void testEmptyIfNullReturnsEmptyMapForNull() {
        Map<?, ?> result = MapUtils.emptyIfNull(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

