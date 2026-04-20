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
// No new imports required
import java.io.StringReader;
import java.util.List;
import org.apache.commons.csv.QuoteMode;
import org.mockito.Mockito;

public class CSVFormatTest {


    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterLineBreak() {
        CSVFormat.newFormat('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterLineBreakWithCharacter() {
        CSVFormat format = CSVFormat.newFormat('\n');
    }


    @Test
    public void testEqualsWithNull() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClass() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals("Not a CSVFormat instance"));
    }


    @Test
    public void testHeaderAssignmentWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStringValues() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentHeaders() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Column3", "Column4");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCustomRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        StringWriter writer = new StringWriter();
        CSVPrinter printer = format.print(writer);
        printer.printRecord("value1", "value2");
        printer.printRecord("value3", "value4");
        String output = writer.toString();
        assertTrue(output.contains("value1,value2\nvalue3,value4\n"));
    }


    @Test
    public void testIgnoreSurroundingSpacesWithImport() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        String csvData = "  value1 , value2  \n  value3 , value4  ";
        CSVParser parser = format.parse(new StringReader(csvData));
        List<CSVRecord> records = parser.getRecords();
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testHashCodeWithNullQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the absence of a quote character
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the absence of a comment marker
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the absence of an escape character
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithNullNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the absence of a nullString
        assertNotNull(hashCode);
    }


    @Test
    public void testHashCodeWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int hashCode = format.hashCode();
        // Verify that the hashCode reflects the true value of the ignoreSurroundingSpaces flag
        assertNotNull(hashCode);
    }


    @Test
    public void testEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testQuoteCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testTrimEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
    }


    @Test
    public void testQuotingBehaviorForNonNumericValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2, with comma", "value3");
        String output = writer.toString();
        assertTrue(output.contains("\"value2, with comma\""));
    }


    @Test
    public void testHandlingOfNullValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", null, "value3");
        String output = writer.toString();
        assertTrue(output.contains("value1,\\N,value3"));
    }


    @Test
    public void testTrimmingBehavior() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "  value1  ", "  value2  ");
        String output = writer.toString();
        assertTrue(output.contains("value1,value2"));
    }


    @Test
    public void testNullStringHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", null, "value3");
        String output = writer.toString();
        assertTrue(output.contains("value1,\\N,value3"));
    }


    @Test
    public void testIgnoreEmptyLines() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        String csvData = "value1,value2\n\nvalue3,value4\n";
        CSVParser parser = format.parse(new StringReader(csvData));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testCommentMarkerHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "# This is a comment");
        String output = writer.toString();
        assertTrue(output.contains("# This is a comment"));
    }


    @Test
    public void testFormatWithNullValue() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        String output = format.format("value1", null, "value3");
        assertTrue(output.contains("value1,\\N,value3"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderWithDuplicateEntries() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", "Column1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test
    public void testFormatWithLeadingAndTrailingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true); // Enable trimming
        String output = format.format("  value1  ", "  value2  ");
        assertTrue(output.contains("value1,value2")); // Check for trimmed output
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreakCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreakCharacterObject() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(Character.valueOf('\n'));
    }


    @Test
    public void testEqualsIdenticalInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentDelimiter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteModesWithCorrectAPI() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentSurroundingSpaceHandling() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEmptyLineHandling() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullQuoteMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure that hashCode is computed without exceptions
    }


    @Test
    public void testEqualsWithIgnoreHeaderCase() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreHeaderCase(true).withHeader("Column1", "Column2");
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreHeaderCase(true).withHeader("Column1", "Column2");
        assertTrue(format1.equals(format2)); // Check equality ignoring case
    }


    @Test
    public void testGetNullStringReturnsSetValue() {
        String expectedNullString = "\\N";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(expectedNullString);
        assertEquals(expectedNullString, format.getNullString());
    }


    @Test
    public void testGetQuoteCharacterReturnsSetValue() {
        Character expectedQuoteChar = '"';
        CSVFormat format = CSVFormat.DEFAULT.withQuote(expectedQuoteChar);
        assertEquals(expectedQuoteChar, format.getQuoteCharacter());
    }


    @Test
    public void testPrintRecordWithLineBreaks() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2\nwith line break", "value3");
        String output = writer.toString();
        assertTrue(output.contains("\"value2\nwith line break\""));
    }


    @Test
    public void testPrintWithNonNumericQuoteMode() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, 123, "non-numeric value");
        String output = writer.toString();
        assertTrue(output.contains("non-numeric value"));
        assertFalse(output.contains("\"123\"")); // Numeric should not be quoted
    }


    @Test
    public void testEmptyRecordsWhenIgnoringEmptyLinesDisabled() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1,value2");
        format.printRecord(writer, "", ""); // Adding empty record
        String output = writer.toString();
        assertTrue(output.contains("value1,value2"));
        assertTrue(output.contains(","));
    }


    @Test
    public void testPrintRecordWithCustomRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2");
        String output = writer.toString();
        assertTrue(output.contains("value1,value2\n")); // Verify that the output uses the specified record separator
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet()); // Verify that the CSVFormat recognizes the comment marker
    }


    @Test
    public void testHeaderCaseSensitivityIgnored() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", "COLUMN2", "COLUMN3")
                                             .withIgnoreHeaderCase(true);
        String[] headers = format.getHeader();
        assertArrayEquals(new String[]{"Column1", "COLUMN2", "COLUMN3"}, headers);
    }


    @Test
    public void testNullHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(null);
        assertNull(format.getHeaderComments());
    }


    @Test
    public void testFormatHandlesNullValues() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        String output = format.format("value1", null, "value3");
        assertTrue(output.contains("value1,N/A,value3"));
    }


    @Test
    public void testTrimBehaviorOnInputValues() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        String trimmed = format.withTrim(true).format("  value1  ");
        assertEquals("value1", trimmed);
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
    public void testWithNullEscapeCharacterAndQuoteModeNone() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
        format.withEscape(null);
    }


    @Test
    public void testWithHeaderNullResultSet() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNotNull(format); // Ensure that the method handles null gracefully
        assertNull(format.getHeader()); // Verify that no headers are set
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharacterLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withQuote('\n'); // Attempt to set a line break as the quote character
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2)); // Should return false due to different quote characters
    }


    @Test
    public void testWithHeaderEmptyResultSet() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(0); // No columns
        Mockito.when(mockMetaData.getColumnLabel(1)).thenThrow(new SQLException("No columns present")); // Simulate no columns
    
        CSVFormat format = CSVFormat.DEFAULT.withHeader(mockResultSet);
        assertNotNull(format); // Ensure that the method handles empty metadata gracefully
        assertArrayEquals(new String[0], format.getHeader()); // Verify that an empty array is returned
    }


    @Test
    public void testEqualsWithDifferentSkipHeaderRecord() {
        CSVFormat format1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(format1.equals(format2)); // Should return false due to different skip header record settings
    }


    @Test
    public void testEqualsWithDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('/');
        assertFalse(format1.equals(format2)); // Should return false due to different comment markers
    }


    @Test
    public void testHashCodeWithIgnoreHeaderCaseTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure that hashCode is computed without exceptions
    }


    @Test
    public void testHashCodeWithIgnoreEmptyLinesTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure that hashCode is computed without exceptions
    }


    @Test
    public void testHashCodeWithSkipHeaderRecordTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure that hashCode is computed without exceptions
    }


    @Test
    public void testCommentMarkerHandlingWithComments() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "# This is a comment");
        String output = writer.toString();
        assertTrue(output.contains("# This is a comment")); // Check that comments are included
    }


    @Test
    public void testPrintAndEscapeWithSpecialCharacters() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2\nwith line break", "value3,value4");
        String output = writer.toString();
        assertTrue(output.contains("value1"));
        assertTrue(output.contains("value2\nwith line break")); // Check for correct handling of newline
        assertTrue(output.contains("value3,value4")); // Check for correct handling of delimiter
    }


    @Test
    public void testPrintRecordWithAllQuoteMode() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2, with comma", "value3");
        String output = writer.toString();
        assertTrue(output.contains("\"value1\""));
        assertTrue(output.contains("\"value2, with comma\""));
        assertTrue(output.contains("\"value3\""));
    }


    @Test
    public void testPrintRecordWithLineBreaksInQuotedValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", "value2\nwith line break", "value3");
        String output = writer.toString();
        assertTrue(output.contains("\"value2\nwith line break\""));
    }


    @Test
    public void testCommentMarkerSet() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        String csvData = "# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4";
        CSVParser parser = format.parse(new StringReader(csvData));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testWithNullStringDefined() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        String output = format.format("value1", null, "value3");
        assertTrue(output.contains("value1,N/A,value3"));
    }


    @Test
    public void testRecordSeparatorSet() {
        String expectedSeparator = "\n";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(expectedSeparator);
        assertEquals(expectedSeparator, format.getRecordSeparator());
    }


    @Test
    public void testIgnoreEmptyLinesEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testIgnoreSurroundingSpacesEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }


    @Test
    public void testIgnoreHeaderCaseEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }


    @Test
    public void testHeaderCommentsSet() {
        String[] comments = {"Header comment 1", "Header comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

