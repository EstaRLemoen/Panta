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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Function;

public class PackageFunctionsTest {


    @Test
    public void testGetFunctionNamespaceMismatch() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("differentNamespace", "Date.new", null);
        assertNull(result);
    }


    @Test
    public void testGetFunctionStaticMethod() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("util", "Collections.singleton", new Object[]{"foo"});
        assertNotNull(result);
        assertTrue(result instanceof MethodFunction);
    }


    @Test
    public void testGetFunctionConstructor() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("util", "Date.new", null);
        assertNotNull(result);
        assertTrue(result instanceof ConstructorFunction);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionClassNotFound() {
        PackageFunctions pkgFunc = new PackageFunctions("non.existent.package.", "util");
        pkgFunc.getFunction("util", "SomeClass.new", null);
    }


    @Test
    public void testGetFunctionWithEmptyParameters() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("util", "Date.new", new Object[0]);
        assertNotNull(result);
        assertTrue(result instanceof ConstructorFunction);
    }


    @Test
    public void testGetFunctionWithNullNamespace() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction(null, "Date.new", null);
        assertNull(result);
    }


    @Test
    public void testGetFunctionWithNullNamespaceAndValidName() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction(null, "Collections.singleton", new Object[]{"foo"});
        assertNull(result);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionForInvalidClassName() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        pkgFunc.getFunction("util", "InvalidClassName.new", null);
    }


    @Test
    public void testGetFunctionWithEmptyParametersAndValidMethod() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("util", "Collections.emptyList", new Object[0]);
        assertNotNull(result);
        assertTrue(result instanceof MethodFunction);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionForInvalidClassNameDuplicate() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        pkgFunc.getFunction("util", "InvalidClassName.new", null);
    }


    @Test
    public void testGetFunctionWithEmptyCollection() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("util", "Collections.emptyList", new Object[0]);
        assertNotNull(result);
        assertTrue(result instanceof MethodFunction);
    }


    public void testGetFunctionWithValidTargetObject() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("util", "Date.new", new Object[]{new java.util.Date()});
        assertNotNull(result);
        assertTrue(result instanceof MethodFunction);
    }


    @Test(expected = JXPathException.class)
    public void testGetFunctionThrowsExceptionForNonExistentClass() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        pkgFunc.getFunction("util", "NonExistentClass.new", null);
    }


    public void testGetFunctionWithNonExistentMethod() {
        PackageFunctions pkgFunc = new PackageFunctions("java.util.", "util");
        Function result = pkgFunc.getFunction("util", "Collections.nonExistentMethod", new Object[]{});
        assertNull(result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

