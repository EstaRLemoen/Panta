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
import org.apache.commons.csv.CSVPrinter;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.util.List;

public class CSVFormatTest {


    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterLineBreak() {
        CSVFormat.newFormat('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharacterSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEscapeCharacterSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCommentMarkerSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharacterSameAsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('#').withCommentMarker('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEscapeCharacterSameAsCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNoEscapeCharacterWithQuoteModeNone() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testHeaderDuplicateEntries() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col1");
    }


    @Test
    public void testWithCommentMarkerNull() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertNull(format.getCommentMarker());
    }


    @Test
    public void testEqualsDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeWithNullQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testHeaderDuplicateEntriesFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2", "Col1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }


    @Test
    public void testEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithQuoteNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull(format.getQuoteCharacter());
    }


    @Test
    public void testWithCommentMarkerNullFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertNull(format.getCommentMarker());
    }


    // This test is already defined and needs to be removed to avoid compilation errors.


    @Test
    public void testEqualsWithNullObject() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClassObject() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(new Object()));
    }


    @Test
    public void testEqualsWithIdenticalInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format1.equals(format2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiterFixed() {
        CSVFormat.newFormat('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote('\n'); // Attempt to set quote character to line break
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker('\n'); // Attempt to set comment marker to line break
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withEscape('\n'); // Attempt to set escape character to line break
    }


    @Test
    public void testWithNullStringAsNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2").withNullString(null);
        assertNotNull(format);
        assertNull(format.getNullString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2").withDelimiter('\n');
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("This is a comment").withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
    }


    @Test
    public void testPrintWithNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withNullString("NULL"));
        printer.print(null);
        assertEquals("NULL", writer.toString());
    }


    @Test
    public void testPrintAndEscapeSpecialCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withQuote('"').withEscape(BACKSLASH));
        printer.print("Value, with, special, characters");
        assertEquals("\"Value, with, special, characters\"", writer.toString());
    }


    @Test
    public void testGetSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    // This test has been removed to avoid compilation errors.


    @Test
    public void testEqualsWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.NONE);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullAndDifferentObjectTypes() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(null));
        assertFalse(format.equals(new Object()));
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(null);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullEscapeCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(null);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullRecordSeparator() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertTrue(format1.equals(format2));
    }


    // This test has been removed to avoid compilation errors.
    // The original test was already defined.


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        assertNull(format.getNullString());
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testEqualsWithDifferentInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertTrue(format.isEscapeCharacterSet());
        assertEquals(Character.valueOf(BACKSLASH), format.getEscapeCharacter());
    }


    @Test
    public void testWithCommentMarkerFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
    }


    @Test
    public void testNewFormatWithCustomDelimiter() {
        char customDelimiter = ';';
        CSVFormat format = CSVFormat.newFormat(customDelimiter);
        assertEquals(customDelimiter, format.getDelimiter());
    }


    @Test
    public void testEqualsWithDifferentCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testGetNullString() {
        String nullStringValue = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(nullStringValue);
        assertEquals(nullStringValue, format.getNullString());
    }


    @Test
    public void testGetIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }


    // This test has been removed to avoid compilation errors.


    @Test
    public void testEqualsWithDifferentConfigurations() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';').withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testFormatWithQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        String formatted = format.format("Value, with, commas", "AnotherValue");
        assertEquals("\"Value, with, commas\",AnotherValue", formatted);
    }


    @Test
    public void testIgnoreEmptyLines() throws IOException {
        StringReader reader = new StringReader("Value1,Value2\n\nValue3,Value4\n");
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVParser parser = format.parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Should ignore the empty line
    }


    @Test
    public void testWithNullStringAsNullFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        // Verify behavior when formatting a null value
        String formatted = format.format("Value", null, "AnotherValue");
        assertEquals("Value,NULL,AnotherValue", formatted);
    }


    @Test
    public void testFormatWithNullValue() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String formatted = format.format("Value", null, "AnotherValue");
        assertEquals("Value,NULL,AnotherValue", formatted);
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


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderDuplicateEntries() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col1");
    }


    @Test
    public void testWithQuoteNullFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull(format.getQuoteCharacter());
    }


    @Test
    public void testQuoteCharacterSetToNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format.isQuoteCharacterSet());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterLineBreakHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\n');
    }


    @Test
    public void testEqualsWithDifferentCSVFormatsHandling() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testIgnoreEmptyLinesHandling() throws IOException {
        StringReader reader = new StringReader("Value1,Value2\n\nValue3,Value4\n");
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVParser parser = format.parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Should ignore the empty line
    }


    @Test
    public void testWithNullStringHandling() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        // Verify behavior when formatting a null value
        String formatted = format.format("Value", null, "AnotherValue");
        assertEquals("Value,NULL,AnotherValue", formatted);
    }


    @Test
    public void testFormatWithCustomNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        String formatted = format.format("Value", null, "AnotherValue");
        assertEquals("Value,N/A,AnotherValue", formatted);
    }


    @Test
    public void testParseWithEmptyLines() throws IOException {
        StringReader reader = new StringReader("Value1,Value2\n\nValue3,Value4\n");
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVParser parser = format.parse(reader);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Should ignore the empty line
    }


    @Test
    public void testIsCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithEmptyHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments();
        assertNotNull(format.getHeaderComments());
        assertEquals(0, format.getHeaderComments().length);
    }


    @Test
    public void testWithCustomRecordSeparator() {
        String customSeparator = "|";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(customSeparator);
        assertEquals(customSeparator, format.getRecordSeparator());
    }


    @Test
    public void testEqualsWithDifferentConfigurationsFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';').withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testFormatWithNullValueFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String formatted = format.format("Value", null, "AnotherValue");
        assertEquals("Value,NULL,AnotherValue", formatted);
    }


    @Test
    public void testEqualsWithDifferentQuoteCharactersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testIsCommentMarkerSetFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithCustomRecordSeparatorFixed() {
        String customSeparator = "|";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(customSeparator);
        assertEquals(customSeparator, format.getRecordSeparator());
    }


    @Test
    public void testHashCodeWithNullFields() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        int hashCode = format.hashCode();
        assertNotNull(hashCode);
    }


    @Test
    public void testEqualsWithNullAndDifferentObjectTypesFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(null));
        assertFalse(format.equals(new Object()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreakFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test
    public void testEqualsWithNullAndNonNullQuoteCharacters() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithNullQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithNullQuote)); // Check equality with different quote character states
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderDuplicateEntriesFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col1"); // Attempt to create a format with duplicate headers
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

