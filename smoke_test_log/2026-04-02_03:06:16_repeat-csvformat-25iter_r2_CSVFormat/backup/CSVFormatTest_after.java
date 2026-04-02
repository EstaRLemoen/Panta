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
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat.Predefined;

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
    public void testWithLineBreakCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakEscapeCharacter() {
        CSVFormat.DEFAULT.withEscape('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakQuoteCharacter() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test
    public void testNewFormatWithNullHeader() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testIsCommentMarkerSetWithUnusedMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithHeaderNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        format.withQuote(',');
    }


    @Test
    public void testHeaderCloningBehavior() {
        String[] originalHeader = {"Column1", "Column2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(originalHeader);
        String[] retrievedHeader = format.getHeader();
        retrievedHeader[0] = "ModifiedColumn1"; // Modify the cloned header
        assertNotEquals(originalHeader[0], retrievedHeader[0]); // Ensure original is unchanged
        assertEquals("Column1", originalHeader[0]); // Check original remains unchanged
    }


    @Test
    public void testEqualsDifferentDelimitersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2)); // Ensure formats are not equal
    }


    @Test
    public void testNullStringHandlingInPrintMethod() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.print(null); // Adjusted to match the method signature
        assertEquals("NULL", writer.toString().trim()); // Check output for null value
    }


    @Test
    public void testEqualsWithIdenticalFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',')
                                          .withQuote(DOUBLE_QUOTE_CHAR)
                                          .withIgnoreEmptyLines(true);
        assertTrue(format1.equals(format2)); // Check equality for identical formats
    }


    @Test
    public void testGetNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString()); // Verify the null string is correctly set
    }


    @Test
    public void testIsEscapeCharacterSetFalse() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format.isEscapeCharacterSet()); // Verify that escape character is not set
    }


    @Test
    public void testIsCommentMarkerSetTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet()); // Verify that comment marker is set
    }


    @Test
    public void testEqualsWithIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',');
        assertTrue(format1.equals(format2)); // Check equality for identical formats
    }


    @Test
    public void testPrintWithNullValue() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.print(null); // Print null value
        assertEquals("NULL", writer.toString().trim()); // Check output for null value
    }


    @Test
    public void testGetHeaderCommentsWithEmptyComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(); // No comments provided
        assertArrayEquals(new String[0], format.getHeaderComments()); // Check that it returns an empty array
    }


    @Test
    public void testEqualsWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(BACKSLASH).withQuoteMode(QuoteMode.NONE);
        assertFalse(format1.equals(format2)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2)); // Verify that the equals() method returns false
    }


    @Test
    public void testEqualsWithNullAndNonNullQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2)); // Check that the equals() method returns false
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2)); // Verify that the equals() method returns false
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2)); // Check that the equals() method returns false
    }


    @Test
    public void testHashCodeConsistency() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        assertEquals(format1.hashCode(), format2.hashCode()); // Check that hash codes are equal
    }


    @Test
    public void testPrintWithConfiguredNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.print(null); // Print null value
        assertEquals("NULL", writer.toString().trim()); // Check output for null value
    }


    @Test
    public void testEqualsWithDifferentQuoteCharactersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }


    @Test
    public void testEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2)); // Ensure formats are not equal
    }


    @Test
    public void testIsNullStringSetWhenNullStringIsNotSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(format.isNullStringSet()); // Check that isNullStringSet() returns false
    }


    @Test
    public void testIsQuoteCharacterSetWhenQuoteCharacterIsSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet()); // Verify that isQuoteCharacterSet() returns true
    }


    @Test
    public void testIsEscapeCharacterSetWhenEscapeCharacterIsSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet()); // Check that isEscapeCharacterSet() returns true
    }


    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        CSVFormat modifiedFormat = format.withSkipHeaderRecord(true);
        assertTrue(modifiedFormat.getSkipHeaderRecord()); // Check that the skip header record behavior is set
    }


    @Test
    public void testEqualsWithDifferentDelimitersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2)); // Ensure formats are not equal
    }


    @Test
    public void testHashCodeConsistencyFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        int hashCode1 = format.hashCode();
        int hashCode2 = format.hashCode();
        assertEquals(hashCode1, hashCode2); // Check that hash codes are equal across calls
    }


    @Test
    public void testPrintHandlesNullValueFixed() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.print(null); // Print null value
        assertEquals("NULL", writer.toString().trim()); // Check output for null value
    }


    @Test
    public void testWithTrimEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim()); // Verify that trimming is enabled
    }


    @Test
    public void testEqualsIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.Predefined.Default.getFormat();
        CSVFormat format2 = CSVFormat.Predefined.Default.getFormat();
        assertTrue(format1.equals(format2)); // Check equality for identical formats
    }


    @Test
    public void testEqualsDifferentCSVFormats() {
        CSVFormat format1 = CSVFormat.Predefined.Excel.getFormat();
        CSVFormat format2 = CSVFormat.Predefined.MySQL.getFormat();
        assertFalse(format1.equals(format2)); // Check that formats are not equal
    }


    @Test
    public void testPrintWithNullValueFixed() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.print(null); // Print null value
        assertEquals("NULL", writer.toString().trim()); // Check output for null value
    }


    @Test
    public void testEqualsWithNullObject() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals(null)); // Verify that equals() returns false when compared to null
    }


    @Test
    public void testEqualsWithDifferentClassTypes() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        assertFalse(format.equals("NotACSVFormat")); // Verify that equals() returns false when compared to a different class type
    }


    @Test
    public void testEqualsWithMatchingQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format1.equals(format2)); // Verify that equals() returns true when both instances have the same quote character
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2)); // Verify that equals() returns false when the null strings are different
    }


    @Test
    public void testEqualsWithQuoteCharacterSet() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithCommentMarkerSet() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(formatWithComment.equals(formatWithoutComment)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithEscapeCharacterSet() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithNullStringSet() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }


    @Test
    public void testEqualsWithDifferentEscapeCharactersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }


    @Test
    public void testEqualsWithDifferentNullStringsFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }


    @Test
    public void testEqualsWithQuoteCharacterSetFixed() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithCommentMarkerSetFixed() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(formatWithComment.equals(formatWithoutComment)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithEscapeCharacterSetFixed() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithNullStringSetFixed() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString)); // Check that formats are not equal
    }


    @Test
    public void testEqualsWithNullStringSetToNull() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString(null);
        assertTrue(format1.equals(format2)); // Verify equality when both nullStrings are null
    }


    @Test
    public void testEqualsWithIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format1.equals(format2)); // Verify equality when both instances have ignoreEmptyLines set to true
    }


    @Test
    public void testEqualsWithDifferentRecordSeparatorsFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse(format1.equals(format2)); // Verify that formats are not equal with different record separators
    }


    @Test
    public void testEqualsWithDifferentQuoteCharactersSet() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }


    @Test
    public void testEqualsWithDifferentEscapeCharactersSet() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }


    @Test
    public void testEqualsWithDifferentNullStringsSet() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }


    @Test
    public void testEqualsWithDifferentCommentMarkersSet() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('/');
        assertFalse(format1.equals(format2)); // Verify that the formats are not equal
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

