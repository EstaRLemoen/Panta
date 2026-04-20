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
import static org.apache.commons.csv.CSVFormat.Predefined;

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
    public void testNewFormatWithNullHeader() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithNull() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClass() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals("NotACSVFormat"));
    }


    @Test
    public void testEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithIdenticalInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = CSVFormat.DEFAULT;
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCommentMarkers() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(formatWithComment.equals(formatWithoutComment));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithDifferentHeaderArrays() {
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Header1", "Header2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader("Header3", "Header4");
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
    }


    @Test
    public void testEqualsWithDifferentSurroundingSpaceHandling() {
        CSVFormat formatIgnoringSpaces = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat formatNotIgnoringSpaces = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(formatIgnoringSpaces.equals(formatNotIgnoringSpaces));
    }


    @Test
    public void testHashCodeWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        int hashCode = format.hashCode(); // This should not throw an exception
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithNullNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        int hashCode = format.hashCode(); // This should not throw an exception
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreSurroundingSpacesTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int hashCode = format.hashCode(); // This should not throw an exception
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreHeaderCaseTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int hashCode = format.hashCode(); // This should not throw an exception
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLinesTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int hashCode = format.hashCode(); // This should not throw an exception
        assertNotNull(hashCode);
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
        String formatted = format.format("value1", null, "value3");
        assertEquals("value1,\\N,value3", formatted);
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", null, "value3");
        assertEquals("value1,\\N,value3" + CRLF, writer.toString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithInvalidDelimiter() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test
    public void testPrintRecordWithSpecialCharacterValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value,2", "value\"3");
        assertEquals("value1,\"value,2\",\"value\"\"3\"" + CRLF, writer.toString());
    }


    @Test
    public void testPrintRecordWithTrailingDelimiter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2");
        assertEquals("value1,value2," + CRLF, writer.toString());
    }


    @Test
    public void testPrintRecordWithNullValueHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, null, "value2", "value3");
        assertEquals("\\N,value2,value3" + CRLF, writer.toString());
    }


    @Test
    public void testPrintRecordWithDelimiterAndLineBreak() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value\n2", "value,3");
        assertEquals("value1,\"value\n2\",\"value,3\"" + CRLF, writer.toString());
    }


    @Test
    public void testPrintWithNullRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2");
        assertEquals("value1,value2", writer.toString().trim());
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharacterMatchesDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEscapeCharacterMatchesDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCommentMarkerMatchesDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withCommentMarker(',');
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

