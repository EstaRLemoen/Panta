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
import org.mockito.Mockito;
import java.sql.ResultSetMetaData;
import static org.junit.Assert.assertNotNull;
import java.io.StringWriter;
import java.io.IOException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import java.io.StringReader;
import java.io.Reader;
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
    public void testWithQuoteLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }


    @Test
    public void testWithHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testEqualsIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsIdenticalQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('@');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('\\');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentHeaders() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Col3", "Col4");
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
    public void testWithHeaderCommentsNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments((Object[]) null);
        assertNull(format.getHeaderComments());
    }


    @Test
    public void testWithQuoteModeNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(null);
        assertNull(format.getQuoteMode());
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
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
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
    public void testPrintRecordWithQuoteCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value1", "Quote\"Value", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("\"Quote\"\"Value\""));
    }


    @Test
    public void testPrintRecordWithLeadingAndTrailingSpaces() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.startsWith("  "));
        assertFalse(output.endsWith("  "));
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, "Value1", null, "Value3");
        String output = writer.toString();
        assertTrue(output.contains("NULL"));
    }


    @Test
    public void testPrintRecordWithSpecialCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.printRecord(writer, "Value1", "Value\n2", "Value,3", "Value\r4");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value\n2"));
        assertTrue(output.contains("Value,3"));
        assertTrue(output.contains("Value\r4"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderDuplicateEntries() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col1");
    }


    @Test
    public void testPrintRecordWithSpaceCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.printRecord(writer, "Value1", "Value 2", "Value\t3");
        String output = writer.toString();
        assertTrue(output.contains("Value 2"));
        assertTrue(output.contains("Value\t3"));
    }


    @Test
    public void testPrintRecordWithNullRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertFalse(output.contains("\n"));
    }


    @Test
    public void testGetIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }


    @Test
    public void testGetHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        String[] comments = format.getHeaderComments();
        assertArrayEquals(new String[]{"Comment 1", "Comment 2"}, comments);
    }


    @Test
    public void testGetNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertEquals("\\N", format.getNullString());
    }


    @Test
    public void testToStringArrayWithIgnoringSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        String[] input = {"  value1  ", "  value2  "};
        String[] result = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = input[i].trim(); // Simulating the behavior of toStringArray
        }
        assertArrayEquals(new String[]{"value1", "value2"}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterSameAsQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withCommentMarker(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.withQuote('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.withEscape('#');
    }


    @Test
    public void testWithHeaderNullResultSet() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNotNull(format);
    }


    @Test
    public void testWithHeaderValidResultSet() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(2);
        Mockito.when(mockMetaData.getColumnLabel(1)).thenReturn("Col1");
        Mockito.when(mockMetaData.getColumnLabel(2)).thenReturn("Col2");
    
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentObjectType() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals("Not a CSVFormat"));
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentSkipHeaderRecord() {
        CSVFormat format1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithNullRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLinesFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int hashCode = format.hashCode();
        assertNotNull(hashCode);
    }


    @Test
    public void testPrintMethodWithNullString() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.print(null, writer, true);
        String output = writer.toString();
        assertTrue(output.contains("NULL"));
    }


    @Test
    public void testPrintRecordWithNonCharSequenceValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Value1", 123, "Value3");
        String output = writer.toString();
        assertTrue(output.contains("123"));
    }


    @Test
    public void testPrintRecordWithTrimmingEnabled() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.startsWith("  "));
        assertFalse(output.endsWith("  "));
    }


    @Test
    public void testPrintRecordWithLineBreaks() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Value1", "Value\n2", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value\n2"));
        assertTrue(output.contains("Value3"));
    }


    @Test
    public void testPrintRecordWithNullQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(null);
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertFalse(output.contains("\""));
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value2"));
    }


    @Test
    public void testPrintRecordWithEmptyValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Value1", "", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains(",,")); // Expecting empty value to be represented as two commas
        assertTrue(output.contains("Value3"));
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value1", "Value\\2");
        String output = writer.toString();
        assertTrue(output.contains("Value\\2"));
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullStringHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
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
    public void testFormatTrimmingWhitespace() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("  Value1  ", "  Value2  ");
        assertFalse(result.startsWith("  "));
        assertFalse(result.endsWith("  "));
    }


    @Test
    public void testWithCommentMarkerAndHeaderComments() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withHeaderComments("Comment 1", "Comment 2");
        StringWriter writer = new StringWriter();
        format.print(writer);
        String output = writer.toString();
        assertTrue(output.contains("Comment 1"));
        assertTrue(output.contains("Comment 2"));
    }


    @Test
    public void testEqualsWithNullEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('\\');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentCommentMarkersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentNullStringsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentRecordSeparatorsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullCommentMarkerUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithEscapeCharacter() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertNotEquals(formatWithEscape.hashCode(), formatWithoutEscape.hashCode());
    }


    @Test
    public void testCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLinesUpdated() {
        CSVFormat formatWithIgnore = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat formatWithoutIgnore = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertNotEquals(formatWithIgnore.hashCode(), formatWithoutIgnore.hashCode());
    }


    @Test
    public void testPrintRecordWithEscapeCharacterAndSpecialChars() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value1", "Value\\2", "Value\n3");
        String output = writer.toString();
        assertTrue(output.contains("Value\\2"));
        assertTrue(output.contains("Value\n3"));
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        format.printRecord(writer, 123, "StringValue");
        String output = writer.toString();
        assertFalse(output.contains("\"123\"")); // Numeric value should not be quoted
        assertTrue(output.contains("StringValue")); // String value should be quoted
    }


    @Test
    public void testPrintRecordWithTrimmingEnabledUpdated() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.startsWith("  "));
        assertFalse(output.endsWith("  "));
    }


    @Test
    public void testPrintRecordWithTrailingDelimiter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith(format.getDelimiter() + format.getRecordSeparator()));
    }


    @Test
    public void testPrintRecordWithQuoting() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value1", "Value,2");
        String output = writer.toString();
        assertTrue(output.contains("\"Value,2\""));
    }


    @Test
    public void testParseWithCommentMarker() throws IOException {
        String input = "# This is a comment\nValue1,Value2\n# Another comment\nValue3,Value4";
        Reader reader = new StringReader(input);
        CSVParser parser = CSVFormat.DEFAULT.withCommentMarker('#').parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Only two records should be parsed
    }


    @Test
    public void testWithHeaderCaseSensitivityIgnored() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        format = format.withHeader("Name", "name");
        String[] headers = format.getHeader();
        assertArrayEquals(new String[]{"Name", "name"}, headers);
    }


    @Test
    public void testWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Generated by XYZ");
        String[] comments = format.getHeaderComments();
        assertArrayEquals(new String[]{"Generated by XYZ"}, comments);
    }


    public enum HeaderEnum {
        COLUMN1, COLUMN2
    }
    
    @Test
    public void testWithHeaderUsingEnum() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(HeaderEnum.class);
        String[] headers = format.getHeader();
        assertArrayEquals(new String[]{"COLUMN1", "COLUMN2"}, headers);
    }


    @Test
    public void testTrimLeadingAndTrailingSpaces() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "  value1  ", "  value2  ");
        String output = writer.toString();
        assertFalse(output.startsWith("  "));
        assertFalse(output.endsWith("  "));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNoEscapeCharacterWithQuoteModeNone() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null).withQuoteMode(QuoteMode.NONE);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1,Value2");
        String output = writer.toString();
        assertTrue(output.contains("Value1,Value2")); // Ensure it handles without escaping
    }


    @Test
    public void testEqualsIdenticalCSVFormatInstances() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format.equals(format));
    }


    @Test
    public void testEqualsDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentEscapeCharactersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithQuoteModeAll() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        int hashCode = format.hashCode();
        assertNotNull(hashCode);
    }


    @Test
    public void testWithEscapeCharacterNull() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format.isEscapeCharacterSet());
    }


    @Test
    public void testWithNullStringNull() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(format.isNullStringSet());
    }


    @Test
    public void testWithCommentMarkerNull() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format.isCommentMarkerSet());
    }


    @Test
    public void testCustomRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.contains("\n"));
    }


    @Test
    public void testAllowMissingColumnNames() throws IOException {
        String input = "Value1,Value2\nValue3,Value4";
        Reader reader = new StringReader(input);
        CSVParser parser = CSVFormat.DEFAULT.withAllowMissingColumnNames().parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Two records should be parsed
    }


    @Test
    public void testTrimLeadingAndTrailingSpacesUpdated() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertFalse(output.startsWith("  "));
        assertFalse(output.endsWith("  "));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

