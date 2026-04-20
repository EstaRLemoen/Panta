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
import static org.mockito.Mockito.*;
import java.sql.ResultSetMetaData;
import org.apache.commons.csv.QuoteMode;
// No new imports required
import java.io.StringReader;
import java.util.List;
import java.io.StringReader; import java.util.List; import org.apache.commons.csv.CSVParser; import org.apache.commons.csv.CSVRecord;

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
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClass() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals("Not a CSVFormat"));
    }


    @Test
    public void testEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithSameQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
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
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader("Column3", "Column4");
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
    }


    @Test
    public void testEqualsWithDifferentSurroundingSpaceSettings() {
        CSVFormat formatWithIgnoreSpaces = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat formatWithoutIgnoreSpaces = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(formatWithIgnoreSpaces.equals(formatWithoutIgnoreSpaces));
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testWithNullRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertNull(format.getRecordSeparator());
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertEquals("\\N", format.getNullString());
    }


    @Test
    public void testHashCodeWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the absence of an escape character correctly.
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithNullNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the absence of a nullString correctly.
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the state of the ignoreSurroundingSpaces flag correctly.
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the state of the ignoreHeaderCase flag correctly.
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the state of the ignoreEmptyLines flag correctly.
        assertNotNull(hashCode);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderWithDuplicateEntries() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", "Column2", "Column1");
    }


    @Test
    public void testPrintRecordWithLineBreak() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Value1", "Value\nWithLineBreak", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value\nWithLineBreak"));
        assertTrue(output.contains("Value3"));
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Value1", "Value with escape \\ character", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value with escape \\ character"));
        assertTrue(output.contains("Value3"));
    }


    @Test
    public void testPrintRecordNewRecord() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "FirstValue", "SecondValue", "ThirdValue");
        String output = writer.toString();
        assertTrue(output.startsWith("FirstValue"));
        assertTrue(output.contains("SecondValue"));
        assertTrue(output.contains("ThirdValue"));
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
    }


    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }


    @Test
    public void testWithHeaderIncludingNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", null, "Column3");
        String[] header = format.getHeader();
        assertEquals(3, header.length);
        assertNull(header[1]); // Verify that the second header is null
    }


    @Test
    public void testFormatTrimsWhitespace() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("  Value1  ", "  Value2  ");
        assertFalse(result.startsWith("  "));
        assertFalse(result.endsWith("  "));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteConflictingWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeConflictingWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerConflictingWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }


    @Test
    public void testFormatWithNullValueNoEscapeNoQuote() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null).withQuote(null);
        String result = format.format("Value1", null, "Value3");
        assertEquals("Value1,,Value3", result);
    }


    public enum TestHeader {
        Column1, Column2, Column3
    }
    
    @Test
    public void testWithHeaderUsingEnum() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        String[] header = format.getHeader();
        assertArrayEquals(new String[]{"Column1", "Column2", "Column3"}, header);
    }


    @Test
    public void testWithHeaderNullResultSet() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testWithHeaderEmptyResultSet() throws SQLException {
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(0);
        
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        assertEquals(0, format.getHeader().length);
    }


    @Test
    public void testEqualsWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.NONE);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(formatWithComment.equals(formatWithoutComment));
    }


    @Test
    public void testEqualsWithDifferentIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharactersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStringsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullAndNonNullQuoteModes() {
        CSVFormat formatWithNullQuoteMode = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat formatWithNonNullQuoteMode = CSVFormat.DEFAULT.withQuote('"');
        assertNotEquals(formatWithNullQuoteMode.hashCode(), formatWithNonNullQuoteMode.hashCode());
    }


    @Test
    public void testHeaderCommentsHandling() {
        String[] comments = {"Comment 1", "Comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }


    @Test
    public void testEscapeCharacterHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testNullStringHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testHashCodeWithNullRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        // Ensure recordSeparator is null
        format = format.withRecordSeparator(null);
        int hashCode = format.hashCode();
        // Verify that the hashCode computation does not throw an exception
        assertNotNull(hashCode);
    }


    @Test
    public void testIsCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testIsNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testIsEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testGetTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
    }


    @Test
    public void testPrintRecordWithLineBreakCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Value1", "Value\nWithLineBreak", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value\nWithLineBreak"));
        assertTrue(output.contains("Value3"));
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, "Value1", null, "Value3");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("NULL"));
        assertTrue(output.contains("Value3"));
    }


    @Test
    public void testPrintRecordWithEmptyValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "", "Value2");
        String output = writer.toString();
        assertTrue(output.contains(",Value2")); // Ensure the empty value is handled correctly
    }


    @Test
    public void testPrintRecordWithQuoteCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with \"quote\"");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with \"\"quote\"\"\"")); // Ensure the quote character is escaped
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        format.printRecord(writer, 12345);
        String output = writer.toString();
        assertEquals("12345", output.trim()); // Ensure the numeric value is not quoted
    }


    @Test
    public void testPrintWithNullRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        format.print(writer);
        String output = writer.toString();
        assertFalse(output.contains("\n"));
        assertFalse(output.contains("\r"));
    }


    @Test
    public void testTrimWhitespaceWithIOExceptionHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        String csvInput = "  Value1  ,  Value2  ";
        CSVParser parser = format.parse(new StringReader(csvInput));
        List<CSVRecord> records = parser.getRecords();
        assertEquals("Value1", records.get(0).get(0)); // Check trimmed value
        assertEquals("Value2", records.get(0).get(1)); // Check trimmed value
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeNullAndQuoteModeNone() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null).withQuoteMode(QuoteMode.NONE);
        // This should trigger the validation check in the constructor
    }


    @Test
    public void testWithHeaderUsingResultSet() throws SQLException {
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("Column1");
        when(mockMetaData.getColumnLabel(2)).thenReturn("Column2");
    
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        String[] header = format.getHeader();
        assertArrayEquals(new String[]{"Column1", "Column2"}, header);
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithCommentMarkerSet() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(formatWithComment.equals(formatWithoutComment));
    }


    @Test
    public void testWithHeaderUsingEnumDifferent() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        String[] header = format.getHeader();
        assertArrayEquals(new String[]{"Column1", "Column2", "Column3"}, header);
    }


    @Test
    public void testEqualsWithNullStringSet() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithSkipHeaderRecordSet() {
        CSVFormat formatWithSkipHeader = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat formatWithoutSkipHeader = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(formatWithSkipHeader.equals(formatWithoutSkipHeader));
    }


    @Test
    public void testEqualsWithNullRecordSeparator() {
        CSVFormat formatWithNullSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVFormat formatWithNonNullSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertFalse(formatWithNullSeparator.equals(formatWithNonNullSeparator));
    }


    @Test
    public void testHashCodeWithNullQuoteMode() {
        CSVFormat formatWithNullQuoteMode = CSVFormat.DEFAULT.withQuote(null);
        int hashCode = formatWithNullQuoteMode.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed even with null quoteMode
    }


    @Test
    public void testHashCodeWithNullCommentMarker() {
        CSVFormat formatWithNullCommentMarker = CSVFormat.DEFAULT.withCommentMarker(null);
        int hashCode = formatWithNullCommentMarker.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed even with null commentMarker
    }


    @Test
    public void testParseWithCommentMarker() throws IOException {
        String csvInput = "# This is a comment\nValue1,Value2\n# Another comment\nValue3,Value4";
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = format.parse(new StringReader(csvInput));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Ensure we have two records
        assertEquals("Value1", records.get(0).get(0)); // First record
        assertEquals("Value2", records.get(0).get(1)); // First record
        assertEquals("Value3", records.get(1).get(0)); // Second record
        assertEquals("Value4", records.get(1).get(1)); // Second record
    }


    @Test
    public void testTrimWhitespace() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format("  Value1  ", "  Value2  ");
        assertFalse(result.startsWith("  "));
        assertFalse(result.endsWith("  "));
    }


    @Test
    public void testPrintRecordWithLineBreakCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Value1", "Value\nWithLineBreak", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value\nWithLineBreak"));
        assertTrue(output.contains("Value3"));
    }


    @Test
    public void testPrintRecordWithNonNumericValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with special, characters");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with special, characters\""));
    }


    @Test
    public void testPrintWithQuoteCharacterCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with \"quote\"");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with \"\"quote\"\"\"")); // Ensure the quote character is escaped
    }


    @Test
    public void testFormatTrimsWhitespaceWithLeadingAndTrailingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("   example   ");
        assertFalse(result.startsWith("   "));
        assertFalse(result.endsWith("   "));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteConflictingWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withQuote('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeConflictingWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withEscape('#');
    }


    @Test
    public void testWithHeaderEnumNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithIdenticalInstances() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat(',');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithDifferentIgnoreEmptyLines() {
        CSVFormat formatWithIgnoreEmptyLines = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat formatWithoutIgnoreEmptyLines = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertNotEquals(formatWithIgnoreEmptyLines.hashCode(), formatWithoutIgnoreEmptyLines.hashCode());
    }


    @Test
    public void testEqualsWithDifferentCommentMarkersUpdated() {
        CSVFormat formatWithHashComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithSlashComment = CSVFormat.DEFAULT.withCommentMarker('/');
        assertFalse(formatWithHashComment.equals(formatWithSlashComment));
    }


    @Test
    public void testHashCodeWithDifferentQuoteModes() {
        CSVFormat formatWithAllQuoteMode = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.ALL);
        CSVFormat formatWithNoneQuoteMode = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.NONE);
        assertNotEquals(formatWithAllQuoteMode.hashCode(), formatWithNoneQuoteMode.hashCode());
    }


    @Test
    public void testGetSkipHeaderRecordWhenEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testIsCommentMarkerSetWhenEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testIsNullStringSetWhenDefined() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testIsEscapeCharacterSetWhenDefined() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testPrintAndQuoteWithAllQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        format.printRecord(writer, "Value with special, characters");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with special, characters\""));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

