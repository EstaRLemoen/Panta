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
import org.apache.commons.csv.QuoteMode;

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
    public void testWithQuoteCharacterNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull(format.getQuoteCharacter());
    }


    @Test
    public void testWithHeaderNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test
    public void testEqualsWithDifferentHeaders() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Col3", "Col4");
        assertFalse(format1.equals(format2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }


    @Test
    public void testNewFormatWithNullHeaderFixed() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull(format.getHeader());
    }


    @Test
    public void testWithQuoteCharacterNullFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull(format.getQuoteCharacter());
    }


    @Test
    public void testEqualsIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsDifferentCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testPrintNullValue() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat.DEFAULT.printRecord(writer, "Value1", null, "Value3");
        assertEquals("Value1,,Value3", writer.toString().trim());
    }


    @Test
    public void testPrintSpecialCharacters() throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat.DEFAULT.printRecord(writer, "Value1", "Value,2", "Value\n3");
        assertEquals("Value1,\"Value,2\",\"Value\n3\"", writer.toString().trim());
    }


    @Test
    public void testIsCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue(format.isCommentMarkerSet());
    }


    @Test
    public void testEqualsIdenticalCSVFormatsWithSameSettings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withIgnoreEmptyLines(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withIgnoreEmptyLines(true);
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testFormatWithNullValue() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        String result = format.format("Value1", null, "Value3");
        assertEquals("Value1,\\N,Value3", result);
    }


    @Test
    public void testGetSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }


    @Test
    public void testEqualsDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.ALL);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullArgument() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClassType() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals("NotACSVFormat"));
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape('\\');
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
    public void testEqualsWithIdenticalInstances() {
        CSVFormat format1 = CSVFormat.DEFAULT;
        CSVFormat format2 = format1;
        assertTrue(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
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
    public void testEqualsWithDifferentIgnoreSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithQuoteCharacterSetButNotUsed() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testEqualsWithCommentMarkerSetButNotUsed() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(formatWithComment.equals(formatWithoutComment));
    }


    @Test
    public void testEqualsWithEscapeCharacterSetButNotUsed() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testEqualsWithNullStringSetButNotUsed() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithIgnoreEmptyLinesFlagSet() {
        CSVFormat formatWithIgnoreEmptyLines = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat formatWithoutIgnoreEmptyLines = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(formatWithIgnoreEmptyLines.equals(formatWithoutIgnoreEmptyLines));
    }


    @Test
    public void testIsQuoteCharacterSetWhenNotUsed() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertTrue(formatWithQuote.isQuoteCharacterSet());
        assertFalse(formatWithoutQuote.isQuoteCharacterSet());
    }


    @Test
    public void testIsCommentMarkerSetWhenNotUsed() {
        CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat formatWithoutComment = CSVFormat.DEFAULT.withCommentMarker(null);
        assertTrue(formatWithComment.isCommentMarkerSet());
        assertFalse(formatWithoutComment.isCommentMarkerSet());
    }


    @Test
    public void testIsEscapeCharacterSetWhenNotUsed() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertTrue(formatWithEscape.isEscapeCharacterSet());
        assertFalse(formatWithoutEscape.isEscapeCharacterSet());
    }


    @Test
    public void testIsNullStringSetWhenNotUsed() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertTrue(formatWithNullString.isNullStringSet());
        assertFalse(formatWithoutNullString.isNullStringSet());
    }


    @Test
    public void testGetRecordSeparatorWhenNotUsed() {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat formatWithoutRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertNotNull(formatWithRecordSeparator.getRecordSeparator());
        assertNull(formatWithoutRecordSeparator.getRecordSeparator());
    }


    @Test
    public void testIsQuoteCharacterSetWithQuoteCharacterSet() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        assertTrue(formatWithQuote.isQuoteCharacterSet());
    }


    @Test
    public void testIsQuoteCharacterSetWithoutQuoteCharacterSet() {
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithoutQuote.isQuoteCharacterSet());
    }


    @Test
    public void testIsEscapeCharacterSetWithEscapeCharacterSet() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        assertTrue(formatWithEscape.isEscapeCharacterSet());
    }


    @Test
    public void testIsEscapeCharacterSetWithoutEscapeCharacterSet() {
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithoutEscape.isEscapeCharacterSet());
    }


    @Test
    public void testIsNullStringSetWithNullStringDefined() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue(formatWithNullString.isNullStringSet());
    }


    @Test
    public void testIsNullStringSetWithoutNullStringDefined() {
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithoutNullString.isNullStringSet());
    }


    @Test
    public void testGetHeaderCommentsWithCommentsDefined() {
        CSVFormat formatWithComments = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        String[] comments = formatWithComments.getHeaderComments();
        assertNotNull(comments);
        assertEquals(2, comments.length);
        assertEquals("Comment 1", comments[0]);
        assertEquals("Comment 2", comments[1]);
    }


    @Test
    public void testGetRecordSeparatorWithRecordSeparatorDefined() {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", formatWithRecordSeparator.getRecordSeparator());
    }


    @Test
    public void testGetRecordSeparatorWithoutRecordSeparatorDefined() {
        CSVFormat formatWithoutRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertNull(formatWithoutRecordSeparator.getRecordSeparator());
    }


    @Test
    public void testEqualsWithNullStringSetToNull() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("\\N");
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithSkipHeaderRecordSetToTrue() {
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
    public void testEqualsWithNullQuoteCharacterFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullEscapeCharacterFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('\\');
        assertFalse(format1.equals(format2));
    }


    @Test
    public void testFormatWithQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        String result = format.format("Value1", "Value,2", "Value\"3");
        assertEquals("Value1,\"Value,2\",\"Value\"\"3\"", result);
    }


    @Test
    public void testPrintWithNullString() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        StringWriter writer = new StringWriter();
        format.printRecord(writer, "Value1", null, "Value3");
        assertEquals("Value1,NULL,Value3", writer.toString().trim());
    }


    @Test
    public void testEqualsWithQuoteCharacterSet() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testEqualsWithEscapeCharacterSet() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testEqualsWithNullStringSet() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithRecordSeparatorSet() {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat formatWithoutRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertFalse(formatWithRecordSeparator.equals(formatWithoutRecordSeparator));
    }


    @Test
    public void testEqualsWithHeaderSet() {
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacter() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacter() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testEqualsWithDifferentNullString() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparator() {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat formatWithoutRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertFalse(formatWithRecordSeparator.equals(formatWithoutRecordSeparator));
    }


    @Test
    public void testEqualsWithDifferentHeader() {
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
    }


    @Test
    public void testEqualsWithQuoteCharacterSetFixed() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testEqualsWithEscapeCharacterSetFixed() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testEqualsWithNullStringSetFixed() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithRecordSeparatorSetFixed() {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat formatWithoutRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertFalse(formatWithRecordSeparator.equals(formatWithoutRecordSeparator));
    }


    @Test
    public void testEqualsWithHeaderSetFixed() {
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
    }


    @Test
    public void testEqualsWithQuoteCharacterSetButNull() {
        CSVFormat formatWithQuote = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(formatWithQuote.equals(formatWithoutQuote));
    }


    @Test
    public void testEqualsWithEscapeCharacterSetButNull() {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat formatWithoutEscape = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(formatWithEscape.equals(formatWithoutEscape));
    }


    @Test
    public void testEqualsWithNullStringSetButNull() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat formatWithoutNullString = CSVFormat.DEFAULT.withNullString(null);
        assertFalse(formatWithNullString.equals(formatWithoutNullString));
    }


    @Test
    public void testEqualsWithRecordSeparatorSetButNull() {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat formatWithoutRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertFalse(formatWithRecordSeparator.equals(formatWithoutRecordSeparator));
    }


    @Test
    public void testEqualsWithHeaderSetButNull() {
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertFalse(formatWithHeader.equals(formatWithoutHeader));
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
    public void testWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse(format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertFalse(format.isEscapeCharacterSet());
    }


    @Test
    public void testWithNullQuoteCharacter() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(format.isQuoteCharacterSet());
    }


    @Test
    public void testWithSpecificNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        assertEquals("N/A", format.getNullString());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

