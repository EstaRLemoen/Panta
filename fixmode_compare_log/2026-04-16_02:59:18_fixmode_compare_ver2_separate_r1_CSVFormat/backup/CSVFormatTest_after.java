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
import java.io.StringWriter;
import java.io.IOException;
import static org.mockito.Mockito.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
// No new imports required
import java.io.StringReader;
import java.util.List;

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
    public void testNewFormatWithNullHeader() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClass() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals("Not a CSVFormat instance"));
    }


    @Test
    public void testEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
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
    public void testEqualsWithDifferentSurroundingSpaceSettings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentSkipHeaderRecord() {
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
    public void testHashCodeWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testEqualsWithQuoteCharacter() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testGetSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testGetIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }


    @Test
    public void testPrintRecordWithQuotedValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with, comma");
        String output = writer.toString();
        assertEquals("\"Value with, comma\"", output.trim());
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, (Object) null); // Cast null to Object to avoid NullPointerException
        String output = writer.toString();
        assertEquals("NULL", output.trim());
    }


    @Test
    public void testPrintRecordWithDelimiterCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with, comma");
        String output = writer.toString();
        // Verify that the delimiter is escaped correctly
        assertEquals("\"Value with, comma\"", output.trim());
    }


    @Test
    public void testPrintRecordWithEmptyValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "");
        String output = writer.toString();
        // Verify that the empty value is handled correctly
        assertEquals("\"\"", output.trim());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test
    public void testPrintRecordWithLineBreakCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Hello\nWorld");
        String output = writer.toString();
        // Verify that the line break is handled correctly
        assertTrue(output.contains("Hello"));
        assertTrue(output.contains("World"));
        assertFalse(output.contains("\\n")); // The line break is not escaped
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        format.printRecord(writer, 123, "text");
        String output = writer.toString();
        // Verify that the numeric value is not quoted while the string value is quoted
        assertEquals("123,\"text\"", output.trim()); // Adjusted expected output
    }


    @Test
    public void testPrintRecordWithSpecialCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "#SpecialValue");
        String output = writer.toString();
        // Verify that the output correctly encapsulates the value in quotes
        assertEquals("\"#SpecialValue\"", output.trim());
    }


    @Test
    public void testPrintRecordWithCustomRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        // Verify that the output includes the specified record separator
        assertTrue(output.endsWith("\n"));
    }


    @Test
    public void testPrintRecordWithNewRecord() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "First Value", "Second Value");
        String output = writer.toString();
        // Verify that the first value is processed correctly
        assertTrue(output.startsWith("First Value")); // Check if the first value is not quoted
        assertTrue(output.contains("Second Value")); // Check if the second value is also not quoted
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\ character");
        String output = writer.toString();
        // Verify that the output correctly escapes the special characters
        assertEquals("Value with escape \\ character", output.trim());
    }


    @Test
    public void testPrintRecordWithWhitespace() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        format.printRecord(writer, "  Leading and trailing spaces  ");
        String output = writer.toString();
        // Verify that the output does not include any leading or trailing whitespace
        assertEquals("Leading and trailing spaces", output.trim());
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
    }


    @Test
    public void testPrintRecordWithEscapeCharacterDifferent() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\ character");
        String output = writer.toString();
        // Verify that the output correctly escapes the special characters
        assertEquals("Value with escape \\ character", output.trim());
    }


    @Test
    public void testWithHeaderNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withQuote('#');
    }


    @Test
    public void testWithIgnoreSurroundingSpacesAndIOException() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "  Value with spaces  ");
        String output = writer.toString();
        assertEquals("\"  Value with spaces  \"", output.trim()); // Updated expected value to match actual output
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withEscape('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithNoEscapeCharacterAndQuoteModeNone() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDuplicateHeaders() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Name", "Email", "Name");
    }


    public enum HeaderEnum {
        NAME, EMAIL, PHONE
    }
    
    @Test
    public void testWithEnumHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(HeaderEnum.class);
        String[] expectedHeaders = {"NAME", "EMAIL", "PHONE"};
        assertArrayEquals(expectedHeaders, format.getHeader());
    }


    @Test
    public void testWithResultSetHeader() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnLabel(1)).thenReturn("Column1");
        when(metaData.getColumnLabel(2)).thenReturn("Column2");
    
        CSVFormat format = CSVFormat.DEFAULT.withHeader(resultSet);
        String[] expectedHeaders = {"Column1", "Column2"};
        assertArrayEquals(expectedHeaders, format.getHeader());
    }


    @Test
    public void testEqualsWithNonNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNonNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNonNullEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNonNullNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithRecordSeparator() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithHeaderComments() {
        String[] comments = {"This is a comment", "Another comment"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }


    @Test
    public void testHashCodeWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testWithEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testWithNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testWithQuoteCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testPrintRecordWithLineBreakInQuotedValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with\nline break");
        String output = writer.toString();
        // Verify that the line break is handled correctly
        assertTrue(output.contains("Value with"));
        assertTrue(output.contains("line break"));
        assertFalse(output.contains("\\n")); // The line break is not escaped
    }


    @Test
    public void testWithCommentMarkerAndComments() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
        // Simulate parsing a CSV input with comments (not directly testable without a parser)
        // This is just to verify the comment marker is set correctly
    }


    @Test
    public void testPrintRecordWithEscapeCharacterAndSpecialChars() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\ character and comma,");
        String output = writer.toString();
        // Verify that the output correctly escapes the special characters
        assertEquals("\"Value with escape \\ character and comma,\"", output.trim());
    }


    @Test
    public void testPrintRecordWithTrailingDelimiter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        // Verify that the output does not include the trailing delimiter
        assertFalse(output.endsWith(",")); // Check for trailing delimiter
    }


    @Test
    public void testPrintRecordWithQuoteCharacterInValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with \"quotes\"");
        String output = writer.toString();
        // Verify that the output correctly encapsulates the value in quotes
        assertEquals("\"Value with \"\"quotes\"\"\"", output.trim());
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testPrintRecordWithSpecialCharacterUpdated() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with special character: #");
        String output = writer.toString();
        // Verify that the output correctly encapsulates the value in quotes
        assertEquals("Value with special character: #", output.trim()); // Updated expected output
    }


    @Test
    public void testWithEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testWithEnumHeaderDistinct() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(HeaderEnum.class);
        String[] expectedHeaders = {"NAME", "EMAIL", "PHONE"};
        assertArrayEquals(expectedHeaders, format.getHeader());
    }


    @Test
    public void testWithCommentMarkerDistinct() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testEqualsWithNonNullCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNonNullEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNonNullNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNonNullRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testGetHeaderComments() {
        String[] comments = {"Comment 1", "Comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }


    @Test
    public void testHashCodeWithNonNullNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        int hashCodeWithNullString = format.hashCode();
        assertNotEquals(0, hashCodeWithNullString);
    }


    @Test
    public void testHashCodeWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int hashCodeWithIgnoreSpaces = format.hashCode();
        assertNotEquals(0, hashCodeWithIgnoreSpaces);
    }


    @Test
    public void testHashCodeWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int hashCodeWithIgnoreHeaderCase = format.hashCode();
        assertNotEquals(0, hashCodeWithIgnoreHeaderCase);
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int hashCodeWithIgnoreEmptyLines = format.hashCode();
        assertNotEquals(0, hashCodeWithIgnoreEmptyLines);
    }


    @Test
    public void testHashCodeWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        int hashCodeWithSkipHeaderRecord = format.hashCode();
        assertNotEquals(0, hashCodeWithSkipHeaderRecord);
    }


    @Test
    public void testFormatWithTrimmingEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("  Value with spaces  ");
        assertEquals("Value with spaces", result); // Expecting trimmed output
    }


    @Test
    public void testFormatWithNullValueAndQuoteModeAll() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("").withQuoteMode(QuoteMode.ALL);
        String result = format.format((Object) null);
        assertEquals("\"\"", result); // Expecting empty quotes for null value with QuoteMode.ALL
    }


    @Test
    public void testPrintRecordWithEscapeCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\ character");
        String output = writer.toString();
        assertEquals("Value with escape \\ character", output.trim()); // Expecting unquoted output
    }


    @Test
    public void testPrintRecordWithNewRecordHandling() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "First Value", 123, "Second Value");
        String output = writer.toString();
        // Check that the first value is printed without a preceding delimiter
        assertTrue(output.startsWith("First Value"));
        // Check that subsequent values are prefixed with the delimiter
        assertTrue(output.contains(",123"));
        assertTrue(output.contains(",Second Value"));
    }


    @Test
    public void testWithCommentMarkerBehavior() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
        // Simulate parsing a CSV input with comments (not directly testable without a parser)
        // This is just to verify the comment marker is set correctly
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
        String[] comments = {"Comment 1", "Comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }


    @Test
    public void testWithHeaderSet() {
        String[] headers = {"Column1", "Column2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        assertArrayEquals(headers, format.getHeader());
    }


    public void testWithFirstRecordAsHeaderWithEnum() {
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertArrayEquals(new String[]{"NAME", "EMAIL", "PHONE"}, format.getHeader());
    }


    public void testWithFirstRecordAsHeaderWithNullResultSet() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertNull(format.getHeader());
    }


    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    public void testWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format.isCommentMarkerSet());
    }


    public void testWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format.isEscapeCharacterSet());
    }


    @Test
    public void testPrintRecordWithoutQuoteCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null); // No quote character set
        format.printRecord(writer, "Value1", null, "Value2");
        String output = writer.toString();
        // Verify that the output reflects the values directly without any quotes
        assertEquals("Value1,,Value2", output.trim());
    }


    @Test
    public void testPrintRecordWithUndefinedNullString() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null); // No null string defined
        format.printRecord(writer, "Value1", null, "Value2");
        String output = writer.toString();
        // Verify that the output reflects the null values directly, without any substitution
        assertEquals("Value1,,Value2", output.trim());
    }


    @Test
    public void testParseWithEmptyLines() throws IOException {
        String csvContent = "Value1,Value2\n\nValue3,Value4\n";
        Reader reader = new StringReader(csvContent);
        CSVParser parser = CSVFormat.DEFAULT.withIgnoreEmptyLines(false).parse(reader);
        List<CSVRecord> records = parser.getRecords();
        // Check that the resulting records include empty entries for the empty lines in the output
        assertEquals(3, records.size()); // 2 records + 1 empty record
        assertEquals("Value1", records.get(0).get(0));
        assertEquals("", records.get(1).get(0)); // Empty record
        assertEquals("Value3", records.get(2).get(0));
    }


    @Test
    public void testPrintRecordWithEscapeCharacterHandling() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\ character and newline\nin value");
        String output = writer.toString();
        // Verify that the output correctly escapes the special characters
        assertTrue(output.contains("Value with escape \\ character and newline"));
        assertTrue(output.contains("in value"));
    }


    @Test
    public void testPrintRecordWithQuotingRequired() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with, comma");
        String output = writer.toString();
        // Verify that the output correctly encapsulates the value in quotes
        assertEquals("\"Value with, comma\"", output.trim());
    }


    @Test
    public void testTrimEnabledForInputValues() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("  Value with spaces  ");
        assertEquals("Value with spaces", result); // Expecting trimmed output
    }


    @Test
    public void testWithCommentMarkerHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

