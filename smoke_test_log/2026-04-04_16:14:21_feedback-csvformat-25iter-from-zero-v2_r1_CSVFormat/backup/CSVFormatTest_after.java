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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

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
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }


    @Test
    public void testWithNullHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testWithCustomEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
    }


    @Test
    public void testWithFirstRecordAsHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testWithCustomCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
    }


    @Test
    public void testEqualsDifferentProperties() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t').withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testHashCodeConsistency() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        int hashCode1 = format.hashCode();
        int hashCode2 = format.hashCode();
        assertEquals(hashCode1, hashCode2);
    }


    @Test
    public void testPrintWithNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "value1", null, "value3");
        assertEquals("value1,NULL,value3" + format.getRecordSeparator(), writer.toString());
    }


    @Test
    public void testGetHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        String[] comments = format.getHeaderComments();
        assertArrayEquals(new String[]{"Comment 1", "Comment 2"}, comments);
    }


    @Test
    public void testEqualsWithIdenticalObjects() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = format1;
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullObject() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClassType() {
        CSVFormat format = CSVFormat.DEFAULT;
        String differentClassObject = "Not a CSVFormat";
        assertFalse(format.equals(differentClassObject));
    }


    @Test
    public void testEqualsWithDifferentDelimiters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter('\t');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
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
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(null);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentHeaderArrays() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Header1", "Header2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Header2", "Header3");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteModesUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentEscapeCharactersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentNullStringsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testQuoteCharacterSetWhenModeIsAll() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
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
    public void testTrimmingEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
    }


    @Test
    public void testEqualsWithNullCommentMarkerUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentHeaders() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Col1", "Col3");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentIgnoreEmptyLines() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentHeaders() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Header1", "Header2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Header1", "Header3");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentIgnoreSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentQuoteModesAll() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testCommentMarkerSetUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testEscapeCharacterSetUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(format.isEscapeCharacterSet());
    }


    @Test
    public void testNullStringSetUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharactersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStringsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithQuoteCharacterSetButNotUsed() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testEqualsWithEscapeCharacterSetButNotUsed() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testEqualsWithNullStringSetButNotUsed() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithRecordSeparatorSetButNotUsed() {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat formatWithoutRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertFalse(formatWithRecordSeparator.equals(formatWithoutRecordSeparator));
    }


    @Test
    public void testEqualsWithHeaderSetButNotUsed() {
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
    }


    @Test
    public void testEqualsWithSkipHeaderRecordSet() {
        CSVFormat formatWithSkipHeader = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat formatWithoutSkipHeader = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse(formatWithSkipHeader.equals(formatWithoutSkipHeader));
    }


    @Test
    public void testEqualsWithCommentMarkerSetButNotUsed() {
        CSVFormat formatWithCommentMarker = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutCommentMarker = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(formatWithCommentMarker.equals(formatWithoutCommentMarker));
    }


    @Test
    public void testEqualsWithQuoteModeSetButNotUsed() {
        CSVFormat formatWithQuoteMode = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        CSVFormat formatWithoutQuoteMode = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        assertFalse(formatWithQuoteMode.equals(formatWithoutQuoteMode));
    }


    @Test
    public void testEqualsWithHeaderSetButNotUsedUpdated() {
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Header1", "Header2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
    }


    @Test
    public void testEqualsWithEscapeCharacterSetButNotUsedUpdated() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testWithQuoteCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(format.isQuoteCharacterSet());
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
    public void testWithNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(format.isNullStringSet());
    }


    @Test
    public void testWithRecordSeparatorSet() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertNotNull(format.getRecordSeparator());
        assertEquals("\n", format.getRecordSeparator());
    }


    @Test
    public void testWithSystemRecordSeparator() {
        String systemSeparator = System.getProperty("line.separator");
        CSVFormat format = CSVFormat.DEFAULT.withSystemRecordSeparator();
        assertEquals(systemSeparator, format.getRecordSeparator());
    }


    @Test
    public void testWithTrimEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false).withTrim(true);
        assertTrue(format.getTrim());
    }


    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2").withAllowMissingColumnNames(true);
        assertTrue(format.getAllowMissingColumnNames());
    }


    @Test
    public void testWithQuoteModeSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"').withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        assertTrue(format.isQuoteCharacterSet());
    }


    @Test
    public void testWithHeaderCommentsSet() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1").withHeaderComments("Comment 1", "Comment 2");
        String[] comments = format.getHeaderComments();
        assertArrayEquals(new String[]{"Comment 1", "Comment 2"}, comments);
    }


    @Test
    public void testEqualsWithDifferentQuoteCharactersUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparatorsUpdated() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testNewFormatWithCustomDelimiter() {
        char customDelimiter = '|';
        CSVFormat format = CSVFormat.newFormat(customDelimiter);
        assertEquals(customDelimiter, format.getDelimiter());
    }


    @Test
    public void testWithCommentMarker() {
        char commentMarker = '#';
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(commentMarker);
        assertEquals(Character.valueOf(commentMarker), format.getCommentMarker());
    }


    @Test
    public void testWithEscapeCharacter() {
        char escapeCharacter = '\\';
        CSVFormat format = CSVFormat.DEFAULT.withEscape(escapeCharacter);
        assertEquals(Character.valueOf(escapeCharacter), format.getEscapeCharacter());
    }


    @Test
    public void testWithNullStringRepresentation() {
        String nullString = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(nullString);
        assertEquals(nullString, format.getNullString());
    }


    @Test
    public void testWithRecordSeparator() {
        String recordSeparator = "\n";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(recordSeparator);
        assertEquals(recordSeparator, format.getRecordSeparator());
    }


    @Test
    public void testWithNullStringSetToNull() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(format.isNullStringSet());
    }


    @Test
    public void testWithCommentMarkerUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testWithTrimEnabledUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
    }


    @Test
    public void testQuoteCharacterSetWhenNotUsed() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"').withNullString(null);
        assertTrue(format.isQuoteCharacterSet());
        assertFalse(format.equals(CSVFormat.DEFAULT.withQuote(null)));
    }


    @Test
    public void testEscapeCharacterSetWhenNotUsed() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withNullString(null);
        assertTrue(format.isEscapeCharacterSet());
        assertFalse(format.equals(CSVFormat.DEFAULT.withEscape(null)));
    }


    @Test
    public void testNullStringSetDefined() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertTrue(format.isNullStringSet());
        assertEquals("NULL", format.getNullString());
    }


    @Test
    public void testRecordSeparatorSetDefined() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertNotNull(format.getRecordSeparator());
        assertEquals("\n", format.getRecordSeparator());
    }


    @Test
    public void testWithHeaderDefinedButNotUsed() {
        String[] headers = {"Col1", "Col2", "Col3"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        assertArrayEquals(headers, format.getHeader());
    }


    @Test
    public void testWithHeaderCommentsDefinedButNotUsed() {
        String[] comments = {"Comment 1", "Comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals(comments, format.getHeaderComments());
    }


    @Test
    public void testWithIgnoreEmptyLinesSetToTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithTrimSetToTrue() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
    }


    @Test
    public void testWithTrailingDelimiterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        assertTrue(format.getTrailingDelimiter());
    }


    @Test
    public void testWithNullCommentMarker() {
        CSVFormat formatWithNullComment = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat formatWithNonNullComment = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(formatWithNullComment.equals(formatWithNonNullComment));
    }


    @Test
    public void testWithNullEscapeCharacter() {
        CSVFormat formatWithNullEscape = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat formatWithNonNullEscape = CSVFormat.DEFAULT.withEscape('\\');
        assertFalse(formatWithNullEscape.equals(formatWithNonNullEscape));
    }


    @Test
    public void testWithNullNullString() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat formatWithNonNullString = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(formatWithNullString.equals(formatWithNonNullString));
    }


    @Test
    public void testWithNullRecordSeparator() {
        CSVFormat formatWithNullSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVFormat formatWithNonNullSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertFalse(formatWithNullSeparator.equals(formatWithNonNullSeparator));
    }


    @Test
    public void testWithNullQuoteCharacter() {
        CSVFormat formatWithNullQuote = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat formatWithNonNullQuote = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(formatWithNullQuote.equals(formatWithNonNullQuote));
    }


    @Test
    public void testWithCustomQuoteCharacter() {
        char customQuote = '\'';
        CSVFormat format = CSVFormat.DEFAULT.withQuote(customQuote);
        assertEquals(Character.valueOf(customQuote), format.getQuoteCharacter());
    }


    @Test
    public void testWithCustomNullString() {
        String nullString = "NULL";
        CSVFormat format = CSVFormat.DEFAULT.withNullString(nullString);
        assertEquals(nullString, format.getNullString());
    }


    @Test
    public void testWithCustomRecordSeparator() {
        String recordSeparator = "\n";
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(recordSeparator);
        assertEquals(recordSeparator, format.getRecordSeparator());
    }


    @Test
    public void testWithCustomCommentMarkerUpdated() {
        char customCommentMarker = '#';
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(customCommentMarker);
        assertEquals(Character.valueOf(customCommentMarker), format.getCommentMarker());
    }


    @Test
    public void testWithCustomEscapeCharacterUpdated() {
        char customEscape = '\\';
        CSVFormat format = CSVFormat.DEFAULT.withEscape(customEscape);
        assertEquals(Character.valueOf(customEscape), format.getEscapeCharacter());
    }


    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithCustomRecordSeparatorUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withSystemRecordSeparator();
        assertEquals(System.getProperty("line.separator"), format.getRecordSeparator());
    }


    @Test
    public void testWithCustomNullStringUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertEquals("\\N", format.getNullString());
    }


    @Test
    public void testWithCustomQuoteCharacterUpdated() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

