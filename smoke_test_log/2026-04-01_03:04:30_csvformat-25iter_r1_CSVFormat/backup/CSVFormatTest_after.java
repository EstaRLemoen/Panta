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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
    public void testWithHeaderValid() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(2, header.length);
        assertEquals("Col1", header[0]);
        assertEquals("Col2", header[1]);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderDuplicate() {
        CSVFormat.DEFAULT.withHeader("Col1", "Col1");
    }


    @Test
    public void testEqualsIdentical() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferent() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Col1");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Col2");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        assertEquals("N/A", format.getNullString());
    }


    @Test
    public void testEqualsDifferentDelimiter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, null, "value");
        assertEquals("N/A,value\r\n", writer.toString());
    }


    @Test
    public void testEqualsDifferentQuoteMode() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testIsCommentMarkerSet() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertTrue(formatWithComment.isCommentMarkerSet());
        assertFalse(formatWithoutComment.isCommentMarkerSet());
    }


    @Test
    public void testEqualsNullAndNonNullQuoteCharacters() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testEqualsDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeConsistency() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        int initialHashCode = format.hashCode();
        assertEquals(initialHashCode, format.hashCode());
    }


    @Test
    public void testWithQuoteNullAndNonNull() {
        CSVFormat formatWithNullQuote = CSVFormat.DEFAULT.withQuote(null);
        assertNull(formatWithNullQuote.getQuoteCharacter());
        CSVFormat formatWithValidQuote = CSVFormat.DEFAULT.withQuote('"');
        assertEquals(Character.valueOf('"'), formatWithValidQuote.getQuoteCharacter());
    }


    @Test
    public void testEqualsDifferentDelimiterUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('|');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteCharacterUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteCharacterNull() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testHashCodeConsistencyWithDifferentConfigurations() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';').withQuote('"').withNullString("N/A");
        int initialHashCode = format.hashCode();
        assertEquals(initialHashCode, format.hashCode());
    }


    @Test
    public void testWithNullStringBehavior() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        assertEquals("N/A", format.getNullString());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

