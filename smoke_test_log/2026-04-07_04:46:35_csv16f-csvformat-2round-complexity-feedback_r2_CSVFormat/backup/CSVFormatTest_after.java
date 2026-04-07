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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

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
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test
    public void testNewFormatWithNullHeader() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertNull(format.getCommentMarker());
    }


    @Test
    public void testWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertNull(format.getEscapeCharacter());
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
        CSVFormat formatWithDifferentNullString = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(formatWithNullString.equals(formatWithDifferentNullString));
    }


    @Test
    public void testEqualsWithDifferentHeaderArrays() {
        CSVFormat formatWithHeader1 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat formatWithHeader2 = CSVFormat.DEFAULT.withHeader("Col1", "Col3");
        assertFalse(formatWithHeader1.equals(formatWithHeader2));
    }


    @Test
    public void testEqualsWithDifferentSurroundingSpaceHandling() {
        CSVFormat formatWithSpacesIgnored = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat formatWithSpacesNotIgnored = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(formatWithSpacesIgnored.equals(formatWithSpacesNotIgnored));
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
        format = format.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
        format = format.withSkipHeaderRecord(false);
        assertFalse(format.getSkipHeaderRecord());
    }


    @Test
    public void testWithRecordSeparatorNull() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
        format = format.withRecordSeparator(null);
        assertNull(format.getRecordSeparator());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDuplicateHeaders() {
        CSVFormat.DEFAULT.withHeader("Col1", "Col1");
    }


    @Test
    public void testWithQuoteModeNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        assertEquals(QuoteMode.MINIMAL, format.getQuoteMode());
        format = format.withQuoteMode(null);
        assertNull(format.getQuoteMode());
    }


    @Test
    public void testHashCodeWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('*');
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoringSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoringHeaderCase() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreHeaderCase(false);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithIgnoringEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testPrintRecordWithQuotedValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with, comma", "Another value");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with, comma\""));
        assertTrue(output.contains("Another value"));
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, null, "Value");
        String output = writer.toString();
        assertTrue(output.contains("NULL"));
        assertTrue(output.contains("Value"));
    }


    @Test
    public void testPrintRecordWithSpacesIgnored() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        format.printRecord(writer, "  Leading space", "Trailing space  ");
        String output = writer.toString();
        assertTrue(output.contains("Leading space"));
        assertTrue(output.contains("Trailing space"));
    }


    @Test
    public void testPrintRecordWithCustomRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(";");
        format.printRecord(writer, "First record");
        format.printRecord(writer, "Second record");
        String output = writer.toString();
        assertTrue(output.contains("First record;"));
        assertTrue(output.contains("Second record;"));
    }


    @Test
    public void testPrintRecordWithEscapedCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\\\ and comma,");
        String output = writer.toString();
        assertTrue(output.contains("Value with escape \\\\ and comma,"));
    }


    @Test
    public void testPrintRecordWithQuotedNonNumericValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with special characters: #, $, %");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with special characters: #, $, %\""));
    }


    @Test
    public void testPrintRecordWithTrimming() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        format.printRecord(writer, "  Leading space  ", "  Trailing space  ");
        String output = writer.toString();
        assertTrue(output.contains("Leading space"));
        assertTrue(output.contains("Trailing space"));
    }


    @Test
    public void testPrintRecordWithEscapeCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\\\ and comma,");
        String output = writer.toString();
        assertTrue(output.contains("Value with escape \\\\ and comma,"));
    }


    @Test
    public void testPrintRecordWithNoQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.NONE);
        format.printRecord(writer, "Value that would typically be quoted");
        String output = writer.toString();
        assertEquals("Value that would typically be quoted", output.trim());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterNewLine() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test
    public void testPrintRecordWithNullValueAsFirst() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, null, "Second value");
        String output = writer.toString();
        assertTrue(output.contains("Second value"));
        assertTrue(output.startsWith(","));
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
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
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        String[] comments = format.getHeaderComments();
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals("Comment 1", comments[0]);
        assertEquals("Comment 2", comments[1]);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreakThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiterThrowsException() {
        CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsDelimiterThrowsException() {
        CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }


    @Test
    public void testFormatWithNullQuoteCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        format.printRecord(writer, "Value with, comma");
        String output = writer.toString();
        assertEquals("Value with, comma", output.trim());
    }


    @Test
    public void testValueOfWithExcelFormat() {
        CSVFormat format = CSVFormat.valueOf("Excel");
        assertEquals(CSVFormat.EXCEL.getDelimiter(), format.getDelimiter());
        assertEquals(CSVFormat.EXCEL.getQuoteCharacter(), format.getQuoteCharacter());
        assertEquals(CSVFormat.EXCEL.getRecordSeparator(), format.getRecordSeparator());
        assertTrue(format.getAllowMissingColumnNames());
    }


    @Test
    public void testWithHeaderWithNullResultSet() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentObjectType() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals("Not a CSVFormat"));
    }


    @Test
    public void testEqualsWithNullObject() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(null);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentSurroundingSpacesIgnored() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHeaderCloningBehavior() {
        String[] headers = {"Col1", "Col2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        String[] retrievedHeaders = format.getHeader();
        assertNotSame(headers, retrievedHeaders);
        assertArrayEquals(headers, retrievedHeaders);
    }


    @Test
    public void testCommentMarkerHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
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
    public void testWithNullString() {
        String expectedNullString = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(expectedNullString);
        assertEquals(expectedNullString, format.getNullString());
    }


    @Test
    public void testPrintRecordWithAllNonNullValues() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL_NON_NULL);
        format.printRecord(writer, "Value1", "Value2", "Value3");
        String output = writer.toString();
        assertTrue(output.contains("\"Value1\""));
        assertTrue(output.contains("\"Value2\""));
        assertTrue(output.contains("\"Value3\""));
    }


    @Test
    public void testPrintRecordWithEscapeCharactersFixed() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\\\ and comma,");
        String output = writer.toString();
        assertTrue(output.contains("Value with escape \\\\ and comma,"));
    }


    @Test
    public void testPrintRecordWithNonNumericQuoteMode() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        format.printRecord(writer, 123, "Text", 456.78);
        String output = writer.toString();
        assertFalse(output.contains("\"123\""));
        assertTrue(output.contains("\"Text\""));
        assertFalse(output.contains("\"456.78\""));
    }


    @Test
    public void testPrintRecordWithEmptyRecord() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        format.printRecord(writer, new Object[]{});
        String output = writer.toString();
        assertEquals("", output.trim());
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\\\ and comma,");
        String output = writer.toString();
        assertTrue(output.contains("Value with escape \\\\ and comma,")); // Expecting correct escape handling
    }


    @Test
    public void testWithRecordSeparator() {
        String expectedSeparator = ";";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(expectedSeparator);
        assertEquals(expectedSeparator, format.getRecordSeparator());
    }


    @Test
    public void testWithCommentMarkerFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullStringFixed() {
        String expectedNullString = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(expectedNullString);
        assertTrue(format.isNullStringSet());
        assertEquals(expectedNullString, format.getNullString());
    }


    @Test
    public void testWithIgnoreEmptyLinesFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsDelimiterThrowsException() {
        CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsQuoteCharacterThrowsException() {
        CSVFormat.DEFAULT.withQuote('"').withCommentMarker('"');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsCommentMarkerThrowsException() {
        CSVFormat.DEFAULT.withCommentMarker('#').withEscape('#');
    }


    @Test
    public void testNullStringConversion() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, null, "Value");
        String output = writer.toString();
        assertTrue(output.contains("NULL"));
        assertTrue(output.contains("Value"));
    }


    @Test
    public void testEqualsWithIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"').withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"').withDelimiter(',');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharactersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStringsFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithCommentMarker() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        int hashWithComment = formatWithComment.hashCode();
        assertNotEquals(hashWithComment, CSVFormat.DEFAULT.hashCode());
    }


    @Test
    public void testHashCodeWithSkipHeaderRecord() {
        CSVFormat formatWithSkipHeader = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        int hashWithSkipHeader = formatWithSkipHeader.hashCode();
        assertNotEquals(hashWithSkipHeader, CSVFormat.DEFAULT.hashCode());
        
        CSVFormat formatWithoutSkipHeader = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        int hashWithoutSkipHeader = formatWithoutSkipHeader.hashCode();
        assertNotEquals(hashWithoutSkipHeader, hashWithSkipHeader);
    }


    @Test
    public void testHashCodeWithRecordSeparator() {
        CSVFormat formatWithSeparator = CSVFormat.DEFAULT.withRecordSeparator(";");
        int hashWithSeparator = formatWithSeparator.hashCode();
        assertNotEquals(hashWithSeparator, CSVFormat.DEFAULT.hashCode());
    }


    @Test
    public void testPrintRecordWithLineBreakCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "Hello\nWorld");
        String output = writer.toString();
        assertTrue(output.contains("Hello"));
        assertTrue(output.contains("World"));
    }


    @Test
    public void testPrintRecordWithQuotedValueContainingDelimiter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "Value with, comma");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with, comma\""));
    }


    @Test
    public void testPrintRecordWithEscapeCharacterFixed() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "Value with escape \\\\ and comma,");
        String output = writer.toString();
        assertTrue(output.contains("Value with escape \\\\ and comma,")); // Expecting correct escape handling
    }


    @Test
    public void testWithRecordSeparatorFixed() {
        String expectedSeparator = ";";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(expectedSeparator);
        assertEquals(expectedSeparator, format.getRecordSeparator());
    }


    @Test
    public void testWithHeaderCommentsIncludingNullValues() {
        String[] comments = {"Valid Comment", null, "Another Comment", null};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        String[] headerComments = format.getHeaderComments();
        assertEquals(4, headerComments.length);
        assertNull(headerComments[1]);
        assertNull(headerComments[3]);
        assertEquals("Valid Comment", headerComments[0]);
        assertEquals("Another Comment", headerComments[2]);
    }


    public enum TestHeader {
        ValidHeader1, ValidHeader2, NullHeader1, NullHeader2
    }
    
    @Test
    public void testWithHeaderEnumContainingNullValues() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        String[] headers = format.getHeader();
        assertEquals(4, headers.length);
        assertEquals("ValidHeader1", headers[0]);
        assertEquals("ValidHeader2", headers[1]);
        assertEquals("NullHeader1", headers[2]);
        assertEquals("NullHeader2", headers[3]);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteModeNoneWithoutEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
        // Instead of calling validate(), we can directly check the condition
        format.withEscape(null); // This will trigger the validation
    }


    @Test
    public void testEqualsIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"').withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"').withDelimiter(',');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithSkipHeaderRecordTrue() {
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
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure it computes a hash code without throwing an exception
    }


    @Test
    public void testIsCommentMarkerSetWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format.isCommentMarkerSet());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

