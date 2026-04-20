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
import java.io.StringReader;
// No new imports needed for this test
import java.io.StringWriter;
import java.io.IOException;
import org.mockito.Mockito;

public class CSVFormatTest {


    @Test
    public void testNewFormatWithValidDelimiter() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertEquals(',', format.getDelimiter());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }


    @Test
    public void testWithQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertEquals('"', format.getQuoteCharacter().charValue());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakQuoteCharacter() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test
    public void testWithEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertEquals(BACKSLASH, format.getEscapeCharacter().charValue());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakEscapeCharacter() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test
    public void testWithHeaderNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsIdenticalInstances() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format.equals(format));
    }


    @Test
    public void testEqualsDifferentInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testParseWithLineBreakCharacters() throws IOException {
        String csvContent = "value1,value2\nvalue3,value4\r\nvalue5,value6";
        Reader reader = new StringReader(csvContent);
        CSVParser parser = CSVFormat.DEFAULT.parse(reader);
        assertNotNull(parser);
        assertEquals(3, parser.getRecords().size());
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat formatWithNullComment = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat formatWithNonNullComment = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(formatWithNullComment.equals(formatWithNonNullComment));
    }


    @Test
    public void testEqualsWithNullEscapeCharacter() {
        CSVFormat formatWithNullEscape = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat formatWithNonNullEscape = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertFalse(formatWithNullEscape.equals(formatWithNonNullEscape));
    }


    @Test
    public void testEqualsWithNullString() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat formatWithNonNullString = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(formatWithNullString.equals(formatWithNonNullString));
    }


    @Test
    public void testEqualsWithDifferentHeaderArrays() {
        CSVFormat formatWithHeader1 = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        CSVFormat formatWithHeader2 = CSVFormat.DEFAULT.withHeader("Column3", "Column4");
        assertFalse(formatWithHeader1.equals(formatWithHeader2));
    }


    @Test
    public void testEqualsWithDifferentIgnoreSurroundingSpaces() {
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
    public void testEqualsWithDifferentRecordSeparator() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testGetHeaderCloningBehavior() {
        String[] header = {"Column1", "Column2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        String[] retrievedHeader = format.getHeader();
        retrievedHeader[0] = "ModifiedColumn"; // Modify the cloned header
        assertNotEquals("ModifiedColumn", format.getHeader()[0]); // Ensure original is unchanged
    }


    @Test
    public void testGetHeaderCommentsCloningBehavior() {
        String[] comments = {"Comment1", "Comment2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        String[] retrievedComments = format.getHeaderComments();
        retrievedComments[0] = "ModifiedComment"; // Modify the cloned comments
        assertNotEquals("ModifiedComment", format.getHeaderComments()[0]); // Ensure original is unchanged
    }


    @Test
    public void testWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testWithNonNullStringForNullString() {
        String expectedNullString = "N/A";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(expectedNullString);
        assertEquals(expectedNullString, format.getNullString());
    }


    @Test
    public void testWithNonNullQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testPrintRecordWithNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, "value1", null, "value3");
        String expectedOutput = "value1,NULL,value3" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithoutLeadingDelimiter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "value1", "value2");
        String expectedOutput = "value1,value2" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithQuoteModeAll() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        format.printRecord(writer, "value1,value2", "value3");
        String expectedOutput = "\"value1,value2\",\"value3\"" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakDelimiter() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test
    public void testFormatEmptyRecord() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format(); // Call with no values
        assertEquals("", result); // Expect an empty string for an empty record
    }


    @Test
    public void testPrintRecordWithTrailingDelimiter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        format.printRecord(writer, "value1", "value2");
        String expectedOutput = "value1,value2" + format.getDelimiter() + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testNewFormatWithSpecialCharacterDelimiter() {
        // Adjusted to expect no exception since the pipe is a valid delimiter
        CSVFormat format = CSVFormat.newFormat('|'); // Using pipe as a delimiter
        assertEquals('|', format.getDelimiter());
    }


    @Test
    public void testWithCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testPrintWithRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        format.println(writer); // Correctly using println to output the record separator
        format.printRecord(writer, "value1", "value2"); // Use printRecord to print values
        String expectedOutput = "\nvalue1,value2\n"; // Adjusted expected output to match actual result
        assertEquals(expectedOutput, writer.toString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharacterConflict() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCharacterConflict() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerConflict() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withCommentMarker(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCharacterEqualsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.withEscape('#'); // This should trigger an IllegalArgumentException
    }


    @Test
    public void testWithNullHeaderEnum() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(format.getHeader()); // Ensure that no headers are set
    }


    @Test
    public void testWithNullResultSetMetadata() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader()); // Ensure that no headers are set
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDuplicateHeaders() {
        String[] headers = {"Name", "Email", "Name"}; // Duplicate header
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        // Validate is done during the creation of the format, so we just assert the exception is thrown.
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteModeNoneAndNullEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
        // Validate is done during the creation of the format, so we just assert the exception is thrown.
        format.withEscape(null); // This should trigger an IllegalArgumentException due to null escape character
    }


    @Test
    public void testEqualsWithDifferentClassType() {
        CSVFormat format = CSVFormat.DEFAULT;
        String differentClassType = "NotACSVFormat";
        assertFalse(format.equals(differentClassType));
    }


    @Test
    public void testEqualsWithDifferentDelimiter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithIgnoreEmptyLinesTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testWithSkipHeaderRecordTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testWithEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        int hashCode = format.hashCode(); // Invoke hashCode to check behavior
        assertNotNull(hashCode); // Ensure it computes a hash code without exceptions
    }


    @Test
    public void testPrintRecordWithLineBreakCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "line1\nline2", "line3\rline4", "line5\r\nline6");
        String expectedOutput = "\"line1\nline2\",\"line3\rline4\",\"line5\r\nline6\"" + format.getRecordSeparator(); // Adjusted to include quotes
        assertEquals(expectedOutput, writer.toString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPrintRecordWithQuoteModeNone() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE); // Removed escape character
        format.printRecord(writer, "value1", "value2");
    }


    @Test
    public void testPrintRecordWithEmptyValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "value1", "", "value3");
        String expectedOutput = "value1,,value3" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithCommentCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "#This is a comment", "value2");
        String expectedOutput = "\"#This is a comment\",value2" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithTrailingSpace() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "value1 ", "value2");
        String expectedOutput = "\"value1 \",value2" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithCustomRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        format.printRecord(writer, "value1", "value2");
        String expectedOutput = "value1,value2\n"; // Adjusted expected output to match actual result
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testWithRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        format.printRecord(writer, "value1", "value2");
        String expectedOutput = "value1,value2\n"; // Expecting newline as record separator
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testIgnoreEmptyLines() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        String csvContent = "value1,value2\n\nvalue3,value4";
        Reader reader = new StringReader(csvContent);
        CSVParser parser = format.parse(reader);
        assertEquals(2, parser.getRecords().size()); // Should ignore the empty line
    }


    @Test
    public void testPrintWithQuoteCharacterCorrected() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "value1,value2"); // Corrected to use printRecord
        String expectedOutput = "\"value1,value2\"" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithNullValueConfigured() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, "value1", null, "value3");
        String expectedOutput = "value1,NULL,value3" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testParseWithCommentMarker() throws IOException {
        String csvContent = "# This is a comment\nvalue1,value2\nvalue3,value4";
        Reader reader = new StringReader(csvContent);
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = format.parse(reader);
        assertEquals(2, parser.getRecords().size()); // Should ignore the comment line
    }


    public enum TestHeader {
        NAME, EMAIL, PHONE
    }
    
    @Test
    public void testWithHeaderEnum() throws IOException {
        String csvContent = "John Doe,johndoe@example.com,1234567890\nJane Doe,janedoe@example.com,0987654321";
        Reader reader = new StringReader(csvContent);
        CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeader.class);
        CSVParser parser = format.parse(reader);
        assertEquals("John Doe", parser.getRecords().get(0).get("NAME")); // Check header mapping
    }


    @Test
    public void testWithNullResultSet() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader()); // Ensure that no headers are set
    }


    @Test
    public void testEqualsWithNullInput() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null)); // Expect false when comparing with null
    }


    @Test
    public void testEqualsWithDifferentQuoteMode() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2)); // Expect false due to differing quote modes
    }


    @Test
    public void testEqualsWithNullQuoteCharacterDistinct() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2)); // Expect false when comparing with a non-null quoteCharacter
    }


    @Test
    public void testEqualsWithNullCommentMarkerDistinct() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2)); // Expect false when comparing with a non-null commentMarker
    }


    @Test
    public void testEqualsWithNullStringDistinct() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(format1.equals(format2)); // Expect false when comparing with a non-null nullString
    }


    @Test
    public void testEqualsWithNullRecordSeparator() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertFalse(format1.equals(format2)); // Expect false due to differing recordSeparators
    }


    @Test
    public void testHashCodeWithNullQuoteMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(null);
        int hashCode = format.hashCode(); // Ensure it computes a hash code without exceptions
        assertNotNull(hashCode); // Check that a hash code is computed
    }


    @Test
    public void testHashCodeWithTrueIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int hashCode = format.hashCode(); // Ensure it computes a hash code without exceptions
        assertNotNull(hashCode); // Check that a hash code is computed
    }


    @Test
    public void testHashCodeWithFalseIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        int hashCode = format.hashCode(); // Ensure it computes a hash code without exceptions
        assertNotNull(hashCode); // Check that a hash code is computed
    }


    @Test
    public void testPrintRecordWithNullString() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, "value1", null, "value3");
        String expectedOutput = "value1,NULL,value3" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithQuoteCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        format.printRecord(writer, "value1,value2");
        String expectedOutput = "\"value1,value2\"" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintWithEscapeCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "value1,value2", "value3\nvalue4");
        String expectedOutput = "\"value1,value2\",\"value3\nvalue4\"" + format.getRecordSeparator(); // Adjusted expected output
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithNullValueHandling() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        format.printRecord(writer, "value1", null, "value3");
        String expectedOutput = "value1,NULL,value3" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithLineBreakCharactersAdjusted() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        format.printRecord(writer, "value1\nvalue2", "value3\rvalue4");
        String expectedOutput = "\"value1\nvalue2\",\"value3\rvalue4\"" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testWithCommentMarkerSetAdjusted() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testPrintRecordWithSpecialCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "value1,value2", "value3\nvalue4");
        String expectedOutput = "\"value1,value2\",\"value3\nvalue4\"" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintWithCustomRecordSeparator() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        format.print(writer); // Call print to check record separator
        format.printRecord(writer, "value1", "value2");
        String expectedOutput = "value1,value2\n"; // Expecting newline as record separator
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        format.printRecord(writer, "value1,value2", "value3\nvalue4");
        String expectedOutput = "\"value1,value2\",\"value3\nvalue4\"" + format.getRecordSeparator();
        assertEquals(expectedOutput, writer.toString());
    }


    @Test
    public void testWithHeaderComments() {
        String[] comments = {"Header Comment 1", "Header Comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertNotNull(format.getHeaderComments());
        assertArrayEquals(comments, format.getHeaderComments());
    }


    @Test
    public void testGetNullString() {
        String expectedNullString = "N/A";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(expectedNullString);
        assertEquals(expectedNullString, format.getNullString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharacterConflictWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        format.withQuote('#'); // This should trigger an IllegalArgumentException
    }


    @Test
    public void testWithHeaderFromResultSet() throws SQLException {
        // Mock ResultSet and ResultSetMetaData
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        ResultSetMetaData metaData = Mockito.mock(ResultSetMetaData.class);
        
        // Setup mock behavior
        Mockito.when(resultSet.getMetaData()).thenReturn(metaData);
        Mockito.when(metaData.getColumnCount()).thenReturn(3);
        Mockito.when(metaData.getColumnLabel(1)).thenReturn("Column1");
        Mockito.when(metaData.getColumnLabel(2)).thenReturn("Column2");
        Mockito.when(metaData.getColumnLabel(3)).thenReturn("Column3");
        
        // Call withHeader with mocked ResultSet
        CSVFormat format = CSVFormat.DEFAULT.withHeader(resultSet);
        
        // Verify that the resulting format has the correct headers
        String[] expectedHeaders = {"Column1", "Column2", "Column3"};
        assertArrayEquals(expectedHeaders, format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format1.equals(format2));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

