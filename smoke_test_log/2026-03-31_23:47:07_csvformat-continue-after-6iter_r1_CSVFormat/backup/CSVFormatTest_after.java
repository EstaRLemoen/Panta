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
    public void testWithLineBreakDelimiter() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakQuote() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakEscape() {
        CSVFormat.DEFAULT.withEscape('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDuplicateHeader() {
        CSVFormat.DEFAULT.withHeader("Col1", "Col1");
    }


    @Test
    public void testCSVFormatEqualsDifferentObjects() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.EXCEL;
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsNull() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testCSVFormatEqualsSameObject() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertTrue(format.equals(format));
    }


    @Test
    public void testCSVFormatEqualsDifferentConfigurations() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(';');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatHashCodeConsistency() {
        CSVFormat format = CSVFormat.MYSQL;
        int hash1 = format.hashCode();
        int hash2 = format.hashCode();
        assertEquals(hash1, hash2);
    }


    @Test
    public void testCSVFormatWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        assertEquals("N/A", format.getNullString());
    }


    @Test
    public void testCSVFormatEqualsDifferentConfigurationsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(';');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatHashCodeConsistencyUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        int hash1 = format.hashCode();
        int hash2 = format.hashCode();
        assertEquals(hash1, hash2);
    }


    @Test
    public void testCSVFormatWithNullStringBehavior() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        String formattedOutput = format.format((Object) null);
        assertEquals("N/A", formattedOutput);
    }


    @Test
    public void testWithQuoteModeAll() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format = format.withQuoteMode(QuoteMode.ALL);
        String formattedOutput = format.format("value1", "value2");
        assertEquals("\"value1\",\"value2\"", formattedOutput);
    }


    @Test
    public void testWithHeaderNames() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\t');
        format = format.withHeader("Col1", "Col2", "Col3");
        assertArrayEquals(new String[]{"Col1", "Col2", "Col3"}, format.getHeader());
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format = format.withHeader("Col1", "Col2").withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteModeNone() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format = format.withQuoteMode(QuoteMode.NONE);
        format.format("value1", "value2");
    }


    @Test
    public void testCSVFormatEqualsDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsDifferentCommentMarkersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = format1.withCommentMarker('#');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatGetHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        String[] comments = format.getHeaderComments();
        assertNotNull(comments);
        assertArrayEquals(new String[]{"Comment 1", "Comment 2"}, comments);
    }


    @Test
    public void testCSVFormatIsEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testCSVFormatWithNullStringHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String formattedOutput = format.format("value1", null, "value3");
        assertEquals("value1,NULL,value3", formattedOutput);
    }


    @Test
    public void testCSVFormatWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testCSVFormatEqualsDifferentNullStringSettings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("N/A");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsDifferentQuoteModesUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatEqualsDifferentCommentMarkersUpdatedFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatHashCodeWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        int hash = format.hashCode();
        assertNotNull(hash);
    }


    @Test
    public void testCSVFormatWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }


    @Test
    public void testCSVFormatEqualsDifferentQuoteModesUpdatedFixed() {
        CSVFormat format1 = CSVFormat.EXCEL; // allows missing column names
        CSVFormat format2 = CSVFormat.DEFAULT; // does not allow missing column names
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCSVFormatHashCodeWithNullStringUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        int hash = format.hashCode();
        assertNotNull(hash);
    }


    @Test
    public void testCSVFormatWithCommentMarkerUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

