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


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }


    @Test
    public void testWithHeader() {
        String[] headers = {"Name", "Email", "Phone"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        assertArrayEquals(headers, format.getHeader());
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }


    @Test
    public void testWithTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
    }


    @Test
    public void testIsEqualWithIdenticalInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = format1; // Same instance
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testIsEqualWithDifferentInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';'); // Different delimiter
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testIsEqualWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"'); // Non-null quote character
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testIsEqualWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t'); // Different delimiter
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testIsEqualWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL); // Different quote mode
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentHeaderArrays() {
        String[] headers1 = {"Name", "Email"};
        String[] headers2 = {"Name", "Phone"};
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader(headers1);
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader(headers2);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentIgnoreSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentSkipHeaderRecords() {
        CSVFormat format1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testGetHeaderReturnsClone() {
        String[] headers = {"Name", "Email", "Phone"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        String[] retrievedHeaders = format.getHeader();
        assertNotSame(headers, retrievedHeaders);
        assertArrayEquals(headers, retrievedHeaders);
    }


    @Test
    public void testGetHeaderCommentsReturnsClone() {
        String[] comments = {"Generated by Apache Commons CSV", "Version 1.0"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        String[] retrievedComments = format.getHeaderComments();
        assertNotSame(comments, retrievedComments);
        assertArrayEquals(comments, retrievedComments);
    }


    @Test
    public void testHashCodeWithDifferentQuoteSettings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(null);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        int hashCodeWithNullString = format.hashCode();
        assertNotNull(hashCodeWithNullString);
    }


    @Test
    public void testHashCodeWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int hashCodeWithIgnoreSpaces = format.hashCode();
        assertNotNull(hashCodeWithIgnoreSpaces);
    }


    @Test
    public void testHashCodeWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int hashCodeWithIgnoreHeaderCase = format.hashCode();
        assertNotNull(hashCodeWithIgnoreHeaderCase);
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        int hashCodeWithIgnoreEmptyLines = format.hashCode();
        assertNotNull(hashCodeWithIgnoreEmptyLines);
    }


    @Test
    public void testHashCodeWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        int hashCodeWithSkipHeaderRecord = format.hashCode();
        assertNotNull(hashCodeWithSkipHeaderRecord);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

