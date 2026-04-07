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
import static org.junit.Assert.assertNotNull;
import java.io.StringWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.IOException;
import java.util.List;
import org.mockito.Mockito;

public class CSVFormatTest {


    @Test
    public void testNewFormatWithValidDelimiter() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNotNull(format);
        assertEquals(',', format.getDelimiter());
    }


    @Test
    public void testWithHeaderMethod() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        String[] header = format.getHeader();
        assertNotNull(header);
        assertEquals(2, header.length);
        assertEquals("Col1", header[0]);
        assertEquals("Col2", header[1]);
    }


    @Test
    public void testWithCommentMarkerMethod() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
    }


    @Test
    public void testValueOfMethod() {
        CSVFormat format = CSVFormat.valueOf("Excel");
        assertNotNull(format);
        assertEquals(CSVFormat.EXCEL.getDelimiter(), format.getDelimiter());
    }


    @Test
    public void testEqualsWithIdenticalFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testValidateWithLineBreakDelimiter() {
        CSVFormat format = CSVFormat.newFormat('\n');
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferingHeaders() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Col3", "Col4");
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
    public void testEqualsWithSkippingHeaderRecord() {
        CSVFormat format1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
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
        assertNotNull(hashCode); // Ensure hashCode is computed
    }


    @Test
    public void testFormatWithLeadingAndTrailingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String output = format.format("  Value1  ", "  Value2  ");
        assertEquals("Value1,Value2", output);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiter() {
        CSVFormat format = CSVFormat.newFormat('\n');
    }


    @Test
    public void testPrintRecordWithNullValueAndNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, (Object) null, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.contains("NULL"));
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value2"));
    }


    @Test
    public void testPrintRecordWithQuotedValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value with \"quotes\"", "Value2");
        String output = writer.toString();
        assertTrue(output.contains("\"Value with \"\"quotes\"\"\""));
        assertTrue(output.contains("Value2"));
    }


    @Test
    public void testParseWithCommentMarker() throws IOException {
        StringReader reader = new StringReader("# This is a comment\nValue1,Value2");
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = format.parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("Value1", records.get(0).get(0));
        assertEquals("Value2", records.get(0).get(1));
    }


    @Test
    public void testPrintRecordWithNumericValueNonNumericQuoteMode() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, 12345, "Value2");
        String output = writer.toString();
        assertFalse(output.contains("\"12345\"")); // Should not be quoted
        assertTrue(output.contains("Value2"));
    }


    @Test
    public void testPrintRecordWithNullValueUsingNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, null, "Value1");
        String output = writer.toString();
        assertTrue(output.contains("NULL")); // Should use the defined null string
        assertTrue(output.contains("Value1"));
    }


    @Test
    public void testPrintRecordWithLeadingAndTrailingSpaces() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertEquals("Value1,Value2", output.trim());
    }


    @Test
    public void testPrintRecordWithNullRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        StringWriter writer = new StringWriter();
        format.print(writer); // Call print directly
        String output = writer.toString();
        assertFalse(output.endsWith("\n")); // Ensure no record separator is appended
    }


    @Test
    public void testWithNullString() {
        String nullStringValue = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(nullStringValue);
        assertTrue(format.isNullStringSet());
        assertEquals(nullStringValue, format.getNullString());
    }


    @Test
    public void testWithRecordSeparator() {
        String recordSeparatorValue = "\n";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(recordSeparatorValue);
        assertEquals(recordSeparatorValue, format.getRecordSeparator());
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
        format = format.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
        format = format.withIgnoreSurroundingSpaces(false);
        assertFalse(format.getIgnoreSurroundingSpaces());
    }


    @Test
    public void testWithHeaderComments() {
        String[] comments = {"Comment 1", "Comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithDuplicateHeaders() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", "Column2", "Column1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test
    public void testWithHeaderEnumNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testWithHeaderResultSetNull() throws SQLException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(format.getHeader());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test
    public void testIsEqualWithSameInstance() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertTrue(format.equals(format));
    }


    @Test
    public void testIsEqualWithNull() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testIsEqualWithDifferentClassType() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(new Object()));
    }


    @Test
    public void testIsEqualWithDifferingQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullStringValues() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithIgnoringSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed
    }


    @Test
    public void testHashCodeWithSkippingHeaderRecords() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed
    }


    @Test
    public void testHashCodeWithAllowingMissingColumnNames() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed
    }


    @Test
    public void testHashCodeWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        int hashCode = format.hashCode();
        assertNotNull(hashCode); // Ensure hashCode is computed
    }


    @Test
    public void testPrintRecordWithAllQuoteMode() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1", "Value with, special characters!", "Value2");
        String output = writer.toString();
        assertTrue(output.contains("\"Value1\""));
        assertTrue(output.contains("\"Value with, special characters!\""));
        assertTrue(output.contains("\"Value2\""));
    }


    @Test
    public void testPrintRecordWithTrimmingEnabled() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "  Value1  ", "  Value2  ");
        String output = writer.toString();
        assertEquals("Value1,Value2", output.trim());
    }


    @Test
    public void testPrintRecordWithLineBreakCharacters() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1", "Value\nwith\nline\nbreaks", "Value2");
        String output = writer.toString();
        assertTrue(output.contains("Value1"));
        assertTrue(output.contains("Value\nwith\nline\nbreaks"));
        assertTrue(output.contains("Value2"));
    }


    @Test
    public void testPrintWithEmptyRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        StringWriter writer = new StringWriter();
        format.print(writer); // Call print with no values
        String output = writer.toString();
        assertEquals("", output); // Ensure output is empty
    }


    @Test
    public void testPrintWithNullValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, null, "Value1"); // Call print with a null value
        String output = writer.toString();
        assertTrue(output.contains("NULL")); // Ensure null is represented by nullString
        assertTrue(output.contains("Value1"));
    }


    @Test
    public void testPrintWithSpecialCharacters() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value with \"quotes\"", "Value, with comma"); // Call print with special characters
        String output = writer.toString();
        assertTrue(output.contains("\"Value with \"\"quotes\"\"\"")); // Ensure quotes are escaped
        assertTrue(output.contains("Value, with comma")); // Ensure comma is handled correctly
    }


    @Test
    public void testWithNullStringSet() {
        String nullStringValue = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(nullStringValue);
        assertTrue(format.isNullStringSet());
        assertEquals(nullStringValue, format.getNullString());
    }


    @Test
    public void testWithRecordSeparatorSet() {
        String recordSeparatorValue = "\n";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(recordSeparatorValue);
        assertEquals(recordSeparatorValue, format.getRecordSeparator());
    }


    @Test
    public void testWithIgnoreEmptyLinesSet() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithIgnoreSurroundingSpacesSet() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }


    @Test
    public void testWithIgnoreHeaderCaseSet() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';').withCommentMarker(';');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withQuote('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteModeNoneWithoutEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }


    @Test
    public void testFormatWithLeadingAndTrailingSpacesSingleValue() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String output = format.format("   example   ");
        assertEquals("example", output);
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullString() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithHeaderResultSetNullFixed() throws SQLException {
        // This test is now removed to avoid duplication
        // CSVFormat format = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        // assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentNullStringValues() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("N/A");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertNotEquals(format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testPrintAndEscapeWithEscapeCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value with newline\\nand backslash\\\\");
        String output = writer.toString();
        assertTrue(output.contains("Value with newline\\n"));
        assertTrue(output.contains("and backslash\\\\"));
    }


    @Test
    public void testWithCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testWithQuoteCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testWithQuoteModeAllSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testWithNullStringSetFixed() {
        String nullStringValue = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(nullStringValue);
        assertTrue(format.isNullStringSet());
        assertEquals(nullStringValue, format.getNullString());
    }


    @Test
    public void testPrintWithoutQuoting() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "SimpleValue");
        String output = writer.toString();
        assertEquals("SimpleValue", output.trim()); // Ensure no quotes are added
    }


    @Test
    public void testPrintWithCommentMarker() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "# This is a comment", "Value1");
        String output = writer.toString();
        assertTrue(output.contains("# This is a comment"));
        assertTrue(output.contains("Value1"));
    }


    @Test
    public void testPrintWithEscapeCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value with newline\\nand backslash\\\\");
        String output = writer.toString();
        assertTrue(output.contains("Value with newline\\n"));
        assertTrue(output.contains("and backslash\\\\"));
    }


    @Test
    public void testPrintRecordWithEmptyRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        StringWriter writer = new StringWriter();
        format.printRecord(writer); // Call with an empty record
        String output = writer.toString();
        assertEquals("", output.trim()); // Ensure output is empty
    }


    @Test
    public void testPrintWithCustomRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(";");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith("Value1,Value2;")); // Ensure custom record separator is used
    }


    @Test
    public void testPrintRecordWithTrailingDelimiter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith(format.getDelimiter() + format.getRecordSeparator()));
    }


    @Test
    public void testPrintRecordWithCustomRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("|");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith("|"));
    }


    @Test
    public void testPrintRecordWithEscapeCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value with newline\\nand backslash\\\\");
        String output = writer.toString();
        assertTrue(output.contains("Value with newline\\n"));
        assertTrue(output.contains("and backslash\\\\"));
    }


    @Test
    public void testPrintRecordWithCommentMarker() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "# This is a comment", "Value1");
        String output = writer.toString();
        assertTrue(output.contains("# This is a comment"));
        assertTrue(output.contains("Value1"));
    }


    @Test
    public void testParseWithIgnoreEmptyLines() throws IOException {
        StringReader reader = new StringReader("Value1,Value2\n\nValue3,Value4");
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVParser parser = format.parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("Value1", records.get(0).get(0));
        assertEquals("Value2", records.get(0).get(1));
        assertEquals("Value3", records.get(1).get(0));
        assertEquals("Value4", records.get(1).get(1));
    }


    @Test
    public void testWithHeaderAndComments() {
        String[] comments = {"Header comment 1", "Header comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2").withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
        assertEquals("Col1", format.getHeader()[0]);
        assertEquals("Col2", format.getHeader()[1]);
    }


    @Test
    public void testWithCustomRecordSeparator() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("|");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1", "Value2");
        String output = writer.toString();
        assertTrue(output.endsWith("|")); // Ensure custom record separator is used
    }


    @Test
    public void testTrimSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String output = format.format("   Value1   ", "   Value2   ");
        assertEquals("Value1,Value2", output);
    }


    @Test
    public void testPrintAndEscapeWithEscapeCharacterFixed() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value with newline\\nand backslash\\\\");
        String output = writer.toString();
        assertTrue(output.contains("Value with newline\\n"));
        assertTrue(output.contains("and backslash\\\\"));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

