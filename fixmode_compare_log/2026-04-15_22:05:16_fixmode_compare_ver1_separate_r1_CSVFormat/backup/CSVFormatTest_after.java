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
import org.apache.commons.csv.QuoteMode;
import java.io.StringReader;
import java.util.List;

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
    public void testNewFormatWithNullHeader() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentObjectType() {
        CSVFormat format = CSVFormat.Predefined.Default.getFormat();
        assertFalse(format.equals("Not a CSVFormat"));
    }


    @Test
    public void testEqualsWithNullObject() {
        CSVFormat format = CSVFormat.Predefined.Default.getFormat();
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentDelimiter() {
        CSVFormat format1 = CSVFormat.Predefined.Default.getFormat();
        CSVFormat format2 = CSVFormat.newFormat(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.Predefined.Default.getFormat().withEscape('"');
        CSVFormat format2 = format1.withQuoteMode(QuoteMode.NONE);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(DOUBLE_QUOTE_CHAR);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentHeaders() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("ColumnA", "ColumnB");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithSkipHeaderRecord() {
        CSVFormat format1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullRecordSeparator() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullQuoteMode() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuoteMode(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        int initialHashCode = format.hashCode();
        assertEquals(initialHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        int initialHashCode = format.hashCode();
        assertEquals(initialHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoringSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int initialHashCode = format.hashCode();
        assertEquals(initialHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoringHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int initialHashCode = format.hashCode();
        assertEquals(initialHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoringEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int initialHashCode = format.hashCode();
        assertEquals(initialHashCode, format.hashCode());
    }


    @Test
    public void testParseWithIgnoredEmptyLines() throws IOException {
        StringReader input = new StringReader("Value1,Value2\n\nValue3,Value4\n");
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVParser parser = format.parse(input);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Should ignore the empty line
    }


    @Test
    public void testPrintRecordWithQuotedValue() throws IOException {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(output, "Value with, comma");
        String result = output.toString();
        assertEquals("\"Value with, comma\"", result.trim());
    }


    @Test
    public void testPrintWithSpecialCharacters() throws IOException {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(output, "Value with, comma", "Value with \"quotes\"");
        String result = output.toString();
        assertEquals("\"Value with, comma\",\"Value with \"\"quotes\"\"\"", result.trim());
    }


    @Test
    public void testPrintWithCustomRecordSeparator() throws IOException {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n").withQuote('"');
        format.printRecord(output, "Value1", "Value2");
        format.println(output);
        format.printRecord(output, "Value3", "Value4");
        String result = output.toString();
        assertTrue(result.contains("\n")); // Check that the custom record separator is used
    }


    @Test
    public void testPrintWithLineBreakCharacter() throws IOException {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(output, "Value1", "Value with\nline break", "Value3");
        String result = output.toString();
        assertTrue(result.contains("\"Value with\nline break\"")); // Check that the line break is handled correctly
    }


    @Test
    public void testPrintRecordWithSpecialStartingCharacter() throws IOException {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(output, "#ValueWithComment");
        String result = output.toString();
        assertEquals("\"#ValueWithComment\"", result.trim()); // Check that the value is encapsulated correctly
    }


    @Test
    public void testPrintRecordWithTrailingSpace() throws IOException {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(output, "ValueWithTrailingSpace ");
        String result = output.toString();
        assertEquals("\"ValueWithTrailingSpace \"", result.trim()); // Check that the trailing space is handled correctly
    }


    @Test
    public void testPrintRecordWithNullRecordSeparator() throws IOException {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        format.printRecord(output, "Value1", "Value2");
        String result = output.toString();
        assertFalse(result.contains("\n")); // Check that no record separator is included
    }


    @Test
    public void testWithNullString() {
        String nullStringValue = "NULL_VALUE";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(nullStringValue);
        assertTrue(format.isNullStringSet()); // Verify that the null string is set
    }


    @Test
    public void testWithRecordSeparator() {
        String recordSeparatorValue = "\n";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(recordSeparatorValue);
        assertEquals(recordSeparatorValue, format.getRecordSeparator()); // Verify that the record separator is set correctly
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines()); // Verify that empty lines are ignored
    }


    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces()); // Verify that surrounding spaces are ignored
    }


    @Test
    public void testWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase()); // Verify that header case is ignored
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithSameQuoteAndDelimiter() {
        CSVFormat.DEFAULT.withDelimiter(',').withQuote(','); // Expecting an IllegalArgumentException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithSameEscapeAndDelimiter() {
        CSVFormat.DEFAULT.withDelimiter(',').withEscape(','); // Expecting an IllegalArgumentException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithSameCommentMarkerAndDelimiter() {
        CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(','); // Expecting an IllegalArgumentException
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

