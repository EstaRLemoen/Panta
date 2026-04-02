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

package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.BACKSLASH;
import static org.apache.commons.csv.Constants.COMMA;
import static org.apache.commons.csv.Constants.COMMENT;
import static org.apache.commons.csv.Constants.EMPTY;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.DOUBLE_QUOTE_CHAR;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.PIPE;
import static org.apache.commons.csv.Constants.SP;
import static org.apache.commons.csv.Constants.TAB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class CSVFormatTest {


    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }


    @Test
    public void testWithHeaderNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testPrintAndEscape() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.print("value with \\ escape", writer, true);
        assertEquals("value with \\ escape", writer.toString());
    }


    @Test
    public void testEqualsDifferentDelimitersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithCommentMarkerUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithQuoteNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format.isQuoteCharacterSet());
    }


    @Test
    public void testWithHeaderNullUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreakUpdated() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test
    public void testEqualsDifferentDelimitersUpdatedFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithHeaderNullBehavior() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsDifferentCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithEscapeNull() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format.isEscapeCharacterSet());
    }


    @Test
    public void testWithQuoteNullFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format.isQuoteCharacterSet());
    }


    @Test
    public void testEqualsWithNullObject() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClassType() {
        CSVFormat format = CSVFormat.DEFAULT;
        Object differentClassObject = new Object();
        assertFalse(format.equals(differentClassObject));
    }


    @Test
    public void testWithHeaderNullBehaviorFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testWithEscapeNullFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format.isEscapeCharacterSet());
    }


    @Test
    public void testEqualsDifferentDelimitersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithHeaderNullInitialization() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsDelimiter() {
        CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test
    public void testEqualsDifferentCSVFormatsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiter() {
        CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test
    public void testWithHeaderNullInitializationFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsDifferentDelimitersFixedUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithQuoteNullFixedUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format.isQuoteCharacterSet());
    }


    @Test
    public void testWithQuoteNullUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format.isQuoteCharacterSet());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreakFixed() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

