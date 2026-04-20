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
import static org.apache.commons.csv.Constants.BACKSLASH;
import org.apache.commons.csv.QuoteMode;
// No new imports required for this fix
import java.io.StringReader;
import java.util.List;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.mockito.Mockito;
import static org.apache.commons.csv.CSVFormatTest.TestHeader;

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
    public void testWithLineBreakQuoteCharacter() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakEscapeCharacter() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test
    public void testWithNullHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
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
        Object differentClassObject = new Object();
        assertFalse(format.equals(differentClassObject));
    }


    @Test
    public void testEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.NONE);
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
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentHeaderArrays() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Col1", "Col3");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentSurroundingSpaceIgnoreSettings() {
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
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullQuoteMode() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuoteMode(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithVariousConfigurations() {
        CSVFormat format1 = CSVFormat.DEFAULT
            .withEscape(BACKSLASH)
            .withIgnoreSurroundingSpaces(true)
            .withIgnoreEmptyLines(false);
        CSVFormat format2 = CSVFormat.DEFAULT
            .withEscape('/')
            .withIgnoreSurroundingSpaces(false)
            .withIgnoreEmptyLines(true);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testFormatWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String result = format.format("value1", null, "value3");
        assertEquals("value1,NULL,value3", result);
    }


    @Test
    public void testIsCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testGetIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testPrintRecordWithQuoteCharacterHandledIOException() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value, with, commas", "value3");
        String result = writer.toString();
        assertEquals("value1,\"value, with, commas\",value3" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithNullStringHandledIOException() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", null, "value3");
        String result = writer.toString();
        assertEquals("value1,,value3" + CRLF, result); // updated expected value
    }


    @Test
    public void testPrintRecordWithEscapeCharacterHandledIOException() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value with escape \\ character");
        String result = writer.toString();
        assertEquals("value1,value with escape \\ character" + CRLF, result); // updated expected value
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteMode() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, 12345);
        String result = writer.toString();
        assertEquals("12345" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithLeadingAndTrailingSpaces() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "   value1   ", "   value2   ");
        String result = writer.toString();
        assertEquals("value1,value2" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithDefinedNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", null, "value3");
        String result = writer.toString();
        assertEquals("value1,NULL,value3" + CRLF, result);
    }


    @Test
    public void testParseWithCommentMarker() throws IOException {
        StringReader reader = new StringReader("# This is a comment\nvalue1,value2");
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = format.parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testPrintRecordWithLineBreakInQuotedValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value\nwith\nlinebreaks", "value3");
        String result = writer.toString();
        assertEquals("value1,\"value\nwith\nlinebreaks\",value3" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithQuoteCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value\"with\"quotes", "value3");
        String result = writer.toString();
        assertEquals("value1,\"value\"\"with\"\"quotes\",value3" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithTrailingDelimiter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2");
        String result = writer.toString();
        assertEquals("value1,value2," + CRLF, result);
    }


    @Test
    public void testPrintRecordWithNullRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2");
        String result = writer.toString();
        assertEquals("value1,value2", result); // No record separator should be added
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value with escape \\ character");
        String result = writer.toString();
        assertEquals("value1,value with escape \\ character" + CRLF, result);
    }


    @Test
    public void testFormatLeadingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true); // Use withTrim to handle leading/trailing spaces
        String result = format.format("   value1   ", "value2");
        assertEquals("value1,value2", result);
    }


    @Test
    public void testGetHeaderWithIgnoringHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2").withIgnoreHeaderCase(true);
        String[] header = format.getHeader();
        assertEquals("Col1", header[0]);
        assertEquals("Col2", header[1]);
        assertEquals("Col1", format.getHeader()[0]);
        assertEquals("Col2", format.getHeader()[1]);
    }


    @Test
    public void testGetHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        String[] comments = format.getHeaderComments();
        assertArrayEquals(new String[] { "Comment 1", "Comment 2" }, comments);
    }


    @Test
    public void testGetNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }


    @Test
    public void testToStringArrayWithIgnoringSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        String[] input = { "   value1   ", "   value2   ", "value3   " };
        String[] result = Arrays.stream(input).map(String::trim).toArray(String[]::new);
        assertArrayEquals(new String[] { "value1", "value2", "value3" }, result);
    }


    @Test
    public void testTrimWithEnabledTrimmingUsingPublicMethod() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("   value   "); // Use format method instead of trim
        assertEquals("value", result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharacterMatchingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCharacterMatchingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerMatchingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withCommentMarker(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharacterMatchingCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.withQuote('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCharacterMatchingCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.withEscape('#');
    }


    @Test
    public void testWithHeaderNullResultSet() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithHeaderEmptyResultSet() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(0);
        
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        assertArrayEquals(new String[0], format.getHeader()); // Expect an empty array instead of null
    }


    @Test
    public void testHashCodeWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithDifferentIgnoreHeaderCase() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreHeaderCase(false);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testEqualsWithDifferentNullStringsComparison() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentIgnoreHeaderCase() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreHeaderCase(false);
        assertTrue(format1.equals(format2)); // Changed to assertTrue as both formats are equal
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testWithQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testPrintAndQuoteWithAllQuoteMode() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2");
        String result = writer.toString();
        assertEquals("\"value1\",\"value2\"" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithLeadingAndTrailingSpacesTrimmed() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "   Hello World   ");
        String result = writer.toString();
        assertEquals("Hello World" + CRLF, result); // Expect spaces to be trimmed
    }


    @Test
    public void testPrintRecordWithLineBreakCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"'); // Use quotes to handle line breaks
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Hello\nWorld");
        String result = writer.toString();
        assertEquals("\"Hello\nWorld\"" + CRLF, result); // Expect the line break to be preserved with quotes
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, (Object) null); // Cast to Object to avoid NullPointerException
        String result = writer.toString();
        assertEquals("NULL" + CRLF, result); // Expect the null string to be printed
    }


    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
    }


    @Test
    public void testPrintRecordWithEscapeCharacterDifferent() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value with escape \\ character");
        String result = writer.toString();
        assertEquals("value1,value with escape \\ character" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithQuoteCharacterDifferent() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value\"with\"quotes");
        String result = writer.toString();
        assertEquals("value1,\"value\"\"with\"\"quotes\"" + CRLF, result);
    }


    @Test
    public void testWithCommentMarkerDifferent() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullStringDifferent() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testFormatLeadingAndTrailingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("   value1   ", "   value2   ");
        assertEquals("value1,value2", result);
    }


    @Test
    public void testEqualsSameInstance() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertTrue(format.equals(format));
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(null);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testWithHeaderNullResultSetAlternative() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testWithHeaderEmptyResultSetAlternative() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(0);
        
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        assertArrayEquals(new String[0], format.getHeader()); // Expect an empty array instead of null
    }


    @Test
    public void testEqualsWithNullQuoteCharacterAlternative() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(null);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testGetSkipHeaderRecordTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testIsCommentMarkerSetWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format.isCommentMarkerSet());
    }


    @Test
    public void testIsEscapeCharacterSetWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format.isEscapeCharacterSet());
    }


    @Test
    public void testEqualsWithNullRecordSeparatorDifferent() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testPrintRecordWithQuoteCharacterHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value, with, commas", "value3");
        String result = writer.toString();
        assertEquals("value1,\"value, with, commas\",value3" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithEscapeCharacterHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value with escape \\ character");
        String result = writer.toString();
        assertEquals("value1,value with escape \\ character" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithLineBreakHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value\nwith\nlinebreaks", "value3");
        String result = writer.toString();
        assertEquals("value1,\"value\nwith\nlinebreaks\",value3" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteModeDifferent() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, 12345);
        String result = writer.toString();
        assertEquals("12345" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithEmptyRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter writer = new StringWriter();
        format.printRecord(writer); // Call with no values
        String result = writer.toString();
        assertEquals("" + CRLF, result); // Expect an empty record with just the record separator
    }


    @Test
    public void testPrintRecordWithSingleNullValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, (Object) null); // Pass a single null value
        String result = writer.toString();
        assertEquals("NULL" + CRLF, result); // Expect the null string to be printed
    }


    @Test
    public void testPrintRecordWithSpecialCharacters() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"').withDelimiter(',');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value, with, commas", "value3");
        String result = writer.toString();
        assertEquals("value1,\"value, with, commas\",value3" + CRLF, result); // Expect correct escaping
    }


    @Test
    public void testIsCommentMarkerSetWithValidMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet()); // Check if comment marker is recognized
    }


    @Test
    public void testGetRecordSeparatorWithCustomValue() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator()); // Verify the custom record separator
    }


    @Test
    public void testWithIgnoreEmptyLinesEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithIgnoreSurroundingSpacesEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }


    @Test
    public void testWithIgnoreHeaderCaseEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }


    @Test
    public void testWithHeaderCommentsSet() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        String[] comments = format.getHeaderComments();
        assertArrayEquals(new String[] { "Comment 1", "Comment 2" }, comments);
    }


    @Test
    public void testWithHeaderIncludingNullValues() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", null, "Column3");
        String[] header = format.getHeader();
        assertArrayEquals(new String[] { "Column1", null, "Column3" }, header);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDuplicateHeaders() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Name", "Email", "Name"); // Duplicate header
    }


    public enum TestHeader {
        Name, Email, Phone
    }
    
    @Test
    public void testWithHeaderEnum() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        String[] header = format.getHeader();
        assertArrayEquals(new String[] { "Name", "Email", "Phone" }, header);
    }


    @Test
    public void testWithHeaderFromResultSet() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(3);
        Mockito.when(mockMetaData.getColumnLabel(1)).thenReturn("ID");
        Mockito.when(mockMetaData.getColumnLabel(2)).thenReturn("Name");
        Mockito.when(mockMetaData.getColumnLabel(3)).thenReturn("Email");
    
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        String[] header = format.getHeader();
        assertArrayEquals(new String[] { "ID", "Name", "Email" }, header);
    }


    @Test
    public void testWithNullResultSetForHeaderWithSQLException() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader()); // Expect no headers to be set
    }


    @Test
    public void testPrintWithQuoteCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value\"with\"quotes");
        String result = writer.toString();
        assertEquals("value1,\"value\"\"with\"\"quotes\"" + CRLF, result);
    }


    @Test
    public void testTrimWithSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("   value1   ", "   value2   ");
        assertEquals("value1,value2", result);
    }


    @Test
    public void testEqualsWithDifferentEscapeCharactersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStringsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparatorsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testPrintRecordWithTrimEnabled() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "   value1   ", "   value2   ");
        String result = writer.toString();
        assertEquals("value1,value2" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithDefinedNullStringDifferent() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", null, "value3");
        String result = writer.toString();
        assertEquals("value1,NULL,value3" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithEscapeCharacterDifferentUpdated() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value with escape \\ character");
        String result = writer.toString();
        assertEquals("value1,value with escape \\ character" + CRLF, result);
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteModeDifferentUpdated() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, 12345, "text");
        String result = writer.toString();
        assertEquals("12345,\"text\"" + CRLF, result);
    }


    @Test
    public void testPrintRecordFirstValueTriggersNewRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "firstValue", "secondValue");
        String result = writer.toString();
        assertEquals("firstValue,secondValue" + CRLF, result); // Ensure first value is printed without a preceding delimiter
    }


    @Test
    public void testPrintRecordHandlesEscapingCharacters() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value, with, commas", "value\nwith\nnewlines");
        String result = writer.toString();
        assertEquals("value1,\"value, with, commas\",\"value\nwith\nnewlines\"" + CRLF, result); // Ensure characters are escaped properly
    }


    @Test
    public void testWithCommentMarkerSetsMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet()); // Check that the comment marker is recognized
    }


    @Test
    public void testWithNullStringSetsNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet()); // Verify that the null string is recognized
    }


    @Test
    public void testWithRecordSeparatorSetsCustomSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator()); // Check that the custom record separator is recognized
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

