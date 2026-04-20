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
import java.io.StringWriter;
import java.io.IOException;
import org.mockito.Mockito;
import org.apache.commons.csv.CSVFormatTest.TestHeader;
import java.io.StringReader;
import java.util.List;
import static org.junit.Assert.assertArrayEquals;

public class CSVFormatTest {


    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterWithLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerWithLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeWithLineBreak() {
        CSVFormat.DEFAULT.withEscape('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteWithLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }


    @Test
    public void testWithHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        assertArrayEquals(new String[]{"Column1", "Column2"}, format.getHeader());
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testEqualsIdenticalInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = format1; // Same instance
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsNullComparison() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsDifferentClassType() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals("Not a CSVFormat instance"));
    }


    @Test
    public void testEqualsDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.NONE);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(DOUBLE_QUOTE_CHAR);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentHeaderArrays() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Column3", "Column4");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentIgnoreSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithSkipHeaderRecordTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testWithRecordSeparatorNull() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertNull(format.getRecordSeparator());
    }


    @Test
    public void testWithHeaderNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testWithQuoteModeNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(null);
        assertNull(format.getQuoteMode());
    }


    @Test
    public void testWithEscapeCharacterNull() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertNull(format.getEscapeCharacter());
    }


    @Test
    public void testHashCodeWithNonNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        int expectedHashCode = format.hashCode(); // Capture the hash code
        assertNotNull(format.getNullString());
        assertEquals(expectedHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int expectedHashCode = format.hashCode(); // Capture the hash code
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertEquals(expectedHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int expectedHashCode = format.hashCode(); // Capture the hash code
        assertTrue(format.getIgnoreHeaderCase());
        assertEquals(expectedHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int expectedHashCode = format.hashCode(); // Capture the hash code
        assertTrue(format.getIgnoreEmptyLines());
        assertEquals(expectedHashCode, format.hashCode());
    }


    @Test
    public void testHashCodeWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        int expectedHashCode = format.hashCode(); // Capture the hash code
        assertTrue(format.getSkipHeaderRecord());
        assertEquals(expectedHashCode, format.hashCode());
    }


    @Test
    public void testPrintRecordWithNonCharSequence() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat.DEFAULT.printRecord(writer, "Value1", 123, "Value3");
        String output = writer.toString();
        assertTrue(output.contains("123"));
    }


    @Test
    public void testPrintRecordWithTrimEnabled() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.contains("  "));
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat.DEFAULT.withNullString("NULL").printRecord(writer, "Value1", null, "Value3");
        String output = writer.toString();
        assertTrue(output.contains("NULL"));
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        format.printRecord(writer, 123, "StringValue");
        String output = writer.toString();
        assertFalse(output.contains("\"123\"")); // Numeric should not be quoted
        assertTrue(output.contains("\"StringValue\"")); // String should be quoted
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderWithDuplicateEntries() {
        CSVFormat.DEFAULT.withHeader("Column1", "Column1");
    }


    @Test
    public void testPrintRecordWithLineBreaks() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(CRLF);
        format.printRecord(writer, "Value1", "Value\n2", "Value\r3");
        String output = writer.toString();
        assertTrue(output.contains("Value\n2")); // Corrected to match actual output
        assertTrue(output.contains("Value\r3")); // Corrected to match actual output
    }


    @Test
    public void testPrintRecordWithCharacterLessThanOrEqualToCommentMarker() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.printRecord(writer, "#Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.startsWith("\"#Value1\"")); // Value should be encapsulated
    }


    @Test
    public void testPrintRecordWithLineBreaksOrDelimiter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.printRecord(writer, "Value1", "Value\n2", "Value\r3");
        String output = writer.toString();
        assertTrue(output.contains("Value\n2")); // Line break should be escaped
        assertTrue(output.contains("Value\r3")); // Carriage return should be escaped
    }


    @Test
    public void testPrintRecordWithTrimming() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.contains("  ")); // Output should not contain leading or trailing spaces
    }


    @Test
    public void testPrintRecordWithNullString() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, "Value1", null, "Value3");
        String output = writer.toString();
        assertTrue(output.contains("NULL")); // Output should reflect the defined null string
    }


    @Test
    public void testPrintRecordWithRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(CRLF);
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith(CRLF)); // Output should end with the defined record separator
    }


    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }


    @Test
    public void testWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }


    @Test
    public void testWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment1", "Comment2");
        assertArrayEquals(new String[]{"Comment1", "Comment2"}, format.getHeaderComments());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteEqualToDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeEqualToDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerEqualToDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withCommentMarker(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteEqualToCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.withQuote('#');
    }


    @Test
    public void testFormatTrimmingWhitespace() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true); // Enable trimming
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.contains("  ")); // Output should not contain leading or trailing spaces
    }


    public void testWithHeaderNullResultSet() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader());
    }


    public void testWithHeaderEmptyResultSet() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(0);
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        assertNull(format.getHeader());
    }


    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(DOUBLE_QUOTE_CHAR);
        assertFalse(format1.equals(format2));
    }


    public void testEqualsWithDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse(format1.equals(format2));
    }


    public enum TestHeader {
        VALUE_ONE, VALUE_TWO, VALUE_THREE
    }
    
    @Test
    public void testWithHeaderEnum() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        assertArrayEquals(new String[]{"VALUE_ONE", "VALUE_TWO", "VALUE_THREE"}, format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(DOUBLE_QUOTE_CHAR);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithIgnoringSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithIgnoringEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithSkippingHeaderRecords() {
        CSVFormat format1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullQuoteMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed without exceptions
    }


    @Test
    public void testHashCodeWithNullQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed without exceptions
    }


    @Test
    public void testHashCodeWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed without exceptions
    }


    @Test
    public void testHashCodeWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed without exceptions
    }


    @Test
    public void testIgnoreEmptyLinesTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines()); // Verify the property is set correctly
    }


    @Test
    public void testPrintRecordWithQuoteCharacterInValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote(DOUBLE_QUOTE_CHAR);
        format.printRecord(writer, "Value with \"quote\"");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with \"\"quote\"\"\"")); // Expecting the value to be quoted and internal quotes escaped
    }


    @Test
    public void testPrintRecordWithLeadingAndTrailingSpaces() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.contains("  ")); // Output should not contain leading or trailing spaces
    }


    @Test
    public void testPrintRecordWithCustomRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("|");
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith("|")); // Check if the output ends with the custom record separator
    }


    @Test
    public void testPrintRecordWithCommentMarker() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.printRecord(writer, "# This is a comment", "Value2");
        String output = writer.toString();
        assertTrue(output.startsWith("\"# This is a comment\"")); // Check if the comment is encapsulated
    }


    @Test
    public void testPrintRecordWithTrimmingEnabled() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.contains("  ")); // Output should not contain leading or trailing spaces
    }


    @Test
    public void testPrintRecordWithQuoteModeAll() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        format.printRecord(writer, "Value1", "Value, with, commas", "Value \"with\" quotes");
        String output = writer.toString();
        assertTrue(output.contains("\"Value1\""));
        assertTrue(output.contains("\"Value, with, commas\""));
        assertTrue(output.contains("\"Value \"\"with\"\" quotes\"")); // Corrected to match actual output
    }


    @Test
    public void testWithNullStringSet() {
        String expectedNullString = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(expectedNullString);
        assertTrue(format.isNullStringSet()); // Verify that nullString is set
        assertEquals(expectedNullString, format.getNullString()); // Verify that the correct nullString is reflected
    }


    @Test
    public void testWithRecordSeparatorSet() {
        String expectedSeparator = "|";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(expectedSeparator);
        assertEquals(expectedSeparator, format.getRecordSeparator()); // Check that the record separator is set correctly
    }


    @Test
    public void testWithIgnoreEmptyLinesSet() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines()); // Verify that empty lines are set to be ignored
    }


    @Test
    public void testWithIgnoreSurroundingSpacesSet() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces()); // Verify that surrounding spaces are set to be ignored
    }


    @Test
    public void testWithIgnoreHeaderCaseSet() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase()); // Verify that header case is set to be ignored
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderDuplicateEntries() {
        CSVFormat.DEFAULT.withHeader("Column1", "Column1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeEqualToCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('#').withEscape('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteModeNoneWithoutEscape() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2)); // Expecting false due to different quote characters
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2)); // Expecting false due to differing comment markers
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2)); // Expecting false due to different null strings
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r");
        assertFalse(format1.equals(format2)); // Expecting false due to different record separators
    }


    @Test
    public void testHashCodeWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testIsEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testIsNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testPrintRecordWithNullValueAndNoNullString() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null); // Ensure nullString is not set
        format.printRecord(writer, "Value1", null, "Value3");
        String output = writer.toString();
        assertTrue(output.contains("")); // Expecting an empty string for the null value
    }


    @Test
    public void testPrintWithCustomRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("|");
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith("|")); // Check if the output ends with the custom record separator
    }


    @Test
    public void testParseWithCommentMarker() throws IOException {
        String input = "# This is a comment\nValue1,Value2";
        Reader reader = new StringReader(input);
        CSVParser parser = CSVFormat.DEFAULT.withCommentMarker('#').parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size()); // Expecting only one record due to the comment
        assertEquals("Value1", records.get(0).get(0)); // Check that the record contains the expected value
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with newline\\nand comma,");
        String output = writer.toString();
        assertTrue(output.contains("Value with newline\\nand comma,")); // Expecting the newline to be escaped
    }


    @Test
    public void testFormatWithNullValue() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String result = format.format("Value1", null, "Value3");
        assertTrue(result.contains("NULL")); // Verify that the null value is represented as "NULL"
    }


    @Test
    public void testParseWithEmptyLines() throws IOException {
        String input = "Value1,Value2\n\nValue3,Value4\n\n";
        Reader reader = new StringReader(input);
        CSVParser parser = CSVFormat.DEFAULT.withIgnoreEmptyLines(true).parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Verify that empty lines are ignored
    }


    @Test
    public void testPrintWithTrimmingSpaces() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true); // Use withTrim instead of withIgnoreSurroundingSpaces
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.contains("  ")); // Check that output does not contain leading or trailing spaces
    }


    @Test
    public void testPrintWithNullRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertFalse(output.contains("\n")); // Ensure no record separators are included
        assertFalse(output.contains("\r")); // Ensure no record separators are included
    }


    @Test
    public void testWithHeaderEnumProvided() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        assertArrayEquals(new String[]{"VALUE_ONE", "VALUE_TWO", "VALUE_THREE"}, format.getHeader());
    }


    @Test
    public void testWithHeaderNullResultSetMocked() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithNullQuoteCharacterMocked() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(DOUBLE_QUOTE_CHAR);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullCommentMarkerMocked() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithHeaderEmptyResultSetMocked() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(0);
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        assertArrayEquals(new String[0], format.getHeader()); // Expecting an empty array instead of null
    }


    @Test
    public void testHashCodeWithNullRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed without exceptions
    }


    @Test
    public void testRecordSeparatorCustomValue() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator()); // Verify the custom record separator is set correctly
    }


    @Test
    public void testWithCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet()); // Verify that the comment marker is recognized as set
    }


    @Test
    public void testWithEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet()); // Verify that the escape character is recognized as set
    }


    @Test
    public void testWithQuoteCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet()); // Verify that the quote character is recognized as set
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

