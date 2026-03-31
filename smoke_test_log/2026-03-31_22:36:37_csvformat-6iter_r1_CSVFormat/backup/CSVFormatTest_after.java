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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class CSVFormatTest {


    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakDelimiter() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakQuote() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakEscape() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test
    public void testCSVFormatEqualsWithDifferentObjects() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.EXCEL;
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatWithDifferentDelimiters() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
    }


    @Test
    public void testCSVFormatEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsWithSameDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsWithSameQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsWithSameEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testWithHeaderAndSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2").withSkipHeaderRecord(true);
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testCSVFormatEqualsWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withNullString("");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsWithDifferentIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

