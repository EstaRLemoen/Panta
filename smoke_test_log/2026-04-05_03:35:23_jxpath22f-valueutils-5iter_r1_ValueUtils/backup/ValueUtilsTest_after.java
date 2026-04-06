/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.util;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathException;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ValueUtilsTest {


    @Test
    public void testIsCollectionWithNull() {
        assertFalse(ValueUtils.isCollection(null));
    }


    @Test
    public void testIsCollectionWithEmptyArray() {
        assertTrue(ValueUtils.isCollection(new Object[0]));
    }


    @Test
    public void testIsCollectionWithEmptyCollection() {
        assertTrue(ValueUtils.isCollection(new ArrayList<>()));
    }


    @Test
    public void testGetCollectionHintWithPrimitive() {
        assertEquals(-1, ValueUtils.getCollectionHint(int.class));
    }


    @Test
    public void testGetLengthWithNonCollection() {
        assertEquals(1, ValueUtils.getLength("Not a collection"));
    }


    @Test
    public void testExpandCollectionWithNull() {
        assertNull(ValueUtils.expandCollection(null, 5));
    }


    @Test
    public void testGetValueWithNullCollection() {
        assertNull(ValueUtils.getValue(null, 0));
    }


    @Test
    public void testSetValueWithNullCollection() {
        ValueUtils.setValue(null, 0, "value"); // Should not throw
    }


    @Test
    public void testGetCollectionHintWithInterface() {
        assertEquals(1, ValueUtils.getCollectionHint(List.class));
    }


    @Test
    public void testIsCollectionWithNullCollection() {
        assertFalse(ValueUtils.isCollection(null));
    }


    @Test(expected = JXPathException.class)
    public void testExpandCollectionToSmallerSize() {
        List<Object> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        ValueUtils.expandCollection(list, 1); // Attempting to shrink the size
    }


    @Test
    public void testIsCollectionWithValidCollection() {
        assertTrue(ValueUtils.isCollection(new ArrayList<>()));
    }


    @Test
    public void testGetCollectionHintWithArrayClass() {
        assertEquals(1, ValueUtils.getCollectionHint(String[].class));
    }


    @Test
    public void testGetCollectionHintWithPrimitiveType() {
        assertEquals(-1, ValueUtils.getCollectionHint(double.class));
    }


    @Test
    public void testGetCollectionHintWithInterfaceClass() {
        assertEquals(0, ValueUtils.getCollectionHint(Iterable.class));
    }


    @Test
    public void testExpandCollectionToLargerSize() {
        List<Object> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        Object expanded = ValueUtils.expandCollection(list, 3); // Correctly expanding to a larger size
        assertEquals(3, ValueUtils.getLength(expanded));
    }


    @Test
    public void testGetLengthWithNullCollection() {
        assertEquals(0, ValueUtils.getLength(null));
    }


    @Test
    public void testIterateWithEmptyArray() {
        Iterator<Object> iterator = ValueUtils.iterate(new Object[0]);
        assertFalse(iterator.hasNext());
    }


    @Test(expected = JXPathException.class)
    public void testRemoveElementWithOutOfBoundsIndex() {
        List<Object> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        ValueUtils.remove(list, 3); // Attempting to remove an index that does not exist
    }


    @Test(expected = JXPathException.class)
    public void testExpandCollectionWithNonCollectionObject() {
        ValueUtils.expandCollection("Not a collection", 5); // Attempting to expand a non-collection
    }


    @Test
    public void testGetLengthWithEmptyArray() {
        assertEquals(0, ValueUtils.getLength(new String[0]));
    }


    @Test
    public void testIterateWithNonCollection() {
        Iterator<Object> iterator = ValueUtils.iterate("Not a collection");
        assertTrue(iterator.hasNext());
        assertEquals("Not a collection", iterator.next());
    }


    @Test(expected = JXPathException.class)
    public void testRemoveElementFromEmptyCollection() {
        List<Object> emptyList = new ArrayList<>();
        ValueUtils.remove(emptyList, 0);
    }


    @Test
    public void testGetLengthWithNullCollectionUnique() {
        assertEquals(0, ValueUtils.getLength(null));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

