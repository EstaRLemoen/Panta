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
package org.apache.commons.jxpath;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.jxpath.functions.ConstructorFunction;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.apache.commons.jxpath.util.ClassLoaderUtil;
import org.apache.commons.jxpath.util.MethodLookupUtils;
import org.apache.commons.jxpath.util.TypeUtils;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.functions.MethodFunction;

public class PackageFunctionsTest {


    @Test
    public void testGetFunctionWithDifferentNamespace() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction("differentNamespace", "Date.new", null);
        assertNull(result);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionOnClassNotFound() {
        PackageFunctions packageFunctions = new PackageFunctions("invalid.package.", "util");
        packageFunctions.getFunction("util", "SomeClass.new", null);
    }


    @Test
    public void testGetFunctionReturnsMethodFunction() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction("util", "Collections.singleton", new Object[]{"foo"});
        assertNotNull(result);
        assertTrue(result instanceof MethodFunction);
    }


    @Test
    public void testGetFunctionWithNullNamespace() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction(null, "Date.new", null);
        assertNull(result);
    }


    @Test
    public void testGetFunctionWithNullNamespaceAndValidPrefix() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction(null, "Date.new", null);
        assertNull(result);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionOnInvalidClass() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        packageFunctions.getFunction("util", "InvalidClass.new", null);
    }


    @Test
    public void testGetFunctionWithNullNamespaceExpected() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction(null, "Date.new", null);
        assertNull(result);
    }


    @Test
    public void testGetFunctionWithValidConstructor() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction("util", "Date.new", null);
        assertNotNull(result);
        assertTrue(result instanceof ConstructorFunction);
    }


    @Test
    public void testGetFunctionWithNullNamespaceFixed() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction(null, "Date.new", null);
        assertNull(result);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionOnClassNotFoundFixed() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        packageFunctions.getFunction("util", "NonExistentClass.new", null);
    }


    @Test
    public void testGetFunctionWithNullNamespaceAndValidName() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction(null, "Date.new", null);
        assertNull(result);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionOnInvalidClassDuringMethodLookup() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        packageFunctions.getFunction("util", "InvalidClass.new", null);
    }


    @Test
    public void testGetFunctionWithValidConstructorAndParameters() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction("util", "Date.new", new Object[]{});
        assertNotNull(result);
        assertTrue(result instanceof ConstructorFunction);
    }


    @Test
    public void testGetFunctionWithNullNamespaceAndValidMethod() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction(null, "Date.new", null);
        assertNull(result);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionOnClassNotFoundDuringMethodLookup() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        packageFunctions.getFunction("util", "NonExistentClass.new", null);
    }


    @Test
    public void testGetFunctionWithNullNamespaceAndNonNullPrefix() {
        PackageFunctions packageFunctions = new PackageFunctions("java.util.", "util");
        Function result = packageFunctions.getFunction(null, "Date.new", null);
        assertNull(result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

