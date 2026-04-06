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
import java.io.IOException;
import java.util.List;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import java.io.StringReader;

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
    public void testWithLineBreakQuoteCharacter() {
        CSVFormat.DEFAULT.withQuote('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithLineBreakEscapeCharacter() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test
    public void testNewFormatHeaderIsNull() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull("Header should be null", format.getHeader());
    }


    @Test
    public void testEqualsWithDifferentDelimiter() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat(';');
        assertFalse("Formats with different delimiters should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse("Formats with different quote characters should not be equal", format1.equals(format2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteSameAsDelimiterThrows() {
        CSVFormat.DEFAULT.withDelimiter(',').withQuote(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeSameAsDelimiterThrows() {
        CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsDelimiterThrows() {
        CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }


    @Test
    public void testEqualsWithSameParameters() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat(',');
        assertTrue("Formats with the same parameters should be equal", format1.equals(format2));
    }


    @Test
    public void testNewFormatWithNullQuoteMode() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull("Quote mode should be null", format.getQuoteMode());
    }


    @Test
    public void testNewFormatWithNullEscapeCharacter() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull("Escape character should be null", format.getEscapeCharacter());
    }


    @Test
    public void testNewFormatWithNullCommentMarker() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull("Comment marker should be null", format.getCommentMarker());
    }


    @Test
    public void testNewFormatHeaderIsNullFixed() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull("Header should be null", format.getHeader());
    }


    @Test
    public void testIsCommentMarkerSet() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue("Comment marker should be set", format.isCommentMarkerSet());
    }


    @Test
    public void testEqualsWithSameParametersFixed() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat(',');
        assertTrue("Formats with the same parameters should be equal", format1.equals(format2));
    }


    @Test
    public void testWithHeaderNonNull() {
        String[] headers = {"Column1", "Column2", "Column3"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        assertNotNull("Header should not be null", format.getHeader());
        assertArrayEquals("Header should match the provided values", headers, format.getHeader());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteModeNone() {
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCharacterLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerSameAsDelimiter() {
        CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }


    @Test
    public void testWithNullStringEmpty() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("");
        assertEquals("Null string should be set to empty", "", format.getNullString());
    }


    @Test
    public void testWithQuoteNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull("Quote character should be null", format.getQuoteCharacter());
    }


    @Test
    public void testWithEscapeNull() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertNull("Escape character should be null", format.getEscapeCharacter());
    }


    @Test
    public void testEqualsWithIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withEscape(BACKSLASH);
        assertTrue("Identical formats should be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(';').withQuote('"');
        assertFalse("Formats with different delimiters should not be equal", format1.equals(format2));
    }


    @Test
    public void testIsCommentMarkerSetWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse("Comment marker should not be set", format.isCommentMarkerSet());
    }


    @Test
    public void testIsEscapeCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertTrue("Escape character should be set", format.isEscapeCharacterSet());
    }


    @Test
    public void testIsNullStringSetWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        assertFalse("Null string should not be set", format.isNullStringSet());
    }


    @Test
    public void testEqualsIdenticalCSVFormats() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withEscape(BACKSLASH);
        assertTrue("Identical formats should be equal", format1.equals(format2));
    }


    @Test
    public void testGetNullStringReturnsNull() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        assertNull("Null string should be null", format.getNullString());
    }


    @Test
    public void testGetHeaderComments() {
        String[] comments = {"Comment 1", "Comment 2"};
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        assertArrayEquals("Header comments should match the provided values", comments, format.getHeaderComments());
    }


    @Test
    public void testIsEscapeCharacterSetFixed() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertTrue("Escape character should be set", format.isEscapeCharacterSet());
    }


    @Test
    public void testEqualsWithDifferentParameters() {
        CSVFormat format1 = CSVFormat.newFormat(',').withQuote('"');
        CSVFormat format2 = CSVFormat.newFormat(';').withQuote('\'');
        assertFalse("Formats with different delimiters and quote characters should not be equal", format1.equals(format2));
    }


    @Test
    public void testHashCodeConsistency() {
        CSVFormat format = CSVFormat.newFormat(',').withQuote('"').withEscape(BACKSLASH);
        int hashCode1 = format.hashCode();
        int hashCode2 = format.hashCode();
        assertEquals("Hash code should be consistent across multiple calls", hashCode1, hashCode2);
    }


    @Test
    public void testWithNullCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertFalse("Comment marker should not be set", format.isCommentMarkerSet());
    }


    @Test
    public void testEqualsWithNullObject() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        assertFalse("Format should not be equal to null", format.equals(null));
    }


    @Test
    public void testEqualsWithDifferentClassType() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        assertFalse("Format should not be equal to a different class type", format.equals("Not a CSVFormat"));
    }


    @Test
    public void testEqualsWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse("Formats with different quote modes should not be equal", format1.equals(format2));
    }


    @Test
    public void testFormatWithNullValue() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String result = format.format("Value1", null, "Value3");
        assertEquals("Formatted output should represent null correctly", "Value1,NULL,Value3", result);
    }


    @Test
    public void testEqualsWithIdenticalObjects() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertTrue("Format should be equal to itself", format.equals(format));
    }


    @Test
    public void testEqualsWithNullQuoteCharacter() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote(null);
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('"');
        assertFalse("Format with null quote character should not be equal to one with a quote", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse("Formats with different comment markers should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStrings() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse("Formats with different null strings should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentQuoteModesFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse("Formats with different quote modes should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('/');
        assertFalse("Formats with different escape characters should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentHeaderArrays() {
        CSVFormat format1 = CSVFormat.DEFAULT.withHeader("Column1", "Column2");
        CSVFormat format2 = CSVFormat.DEFAULT.withHeader("Column3", "Column4");
        assertFalse("Formats with different header arrays should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentCommentMarkersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertFalse("Formats with different comment markers should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStringsFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("NULL");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse("Formats with different null strings should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentEscapeCharactersFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        CSVFormat format2 = CSVFormat.DEFAULT.withEscape('"');
        assertFalse("Formats with different escape characters should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentNullStringRepresentations() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("");
        assertFalse("Formats with different null string representations should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullCommentMarker() {
        CSVFormat formatWithNullComment = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat formatWithValidComment = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse("Formats with different comment markers should not be equal", formatWithNullComment.equals(formatWithValidComment));
    }


    @Test
    public void testEqualsWithNullEscapeCharacter() {
        CSVFormat formatWithNullEscape = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat formatWithValidEscape = CSVFormat.DEFAULT.withEscape(BACKSLASH);
        assertFalse("Formats with different escape characters should not be equal", formatWithNullEscape.equals(formatWithValidEscape));
    }


    @Test
    public void testEqualsWithNullNullString() {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat formatWithValidNullString = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse("Formats with different null string representations should not be equal", formatWithNullString.equals(formatWithValidNullString));
    }


    @Test
    public void testEqualsWithIgnoreSurroundingSpaces() {
        CSVFormat formatWithIgnoringSpaces = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat formatWithoutIgnoringSpaces = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse("Formats with different ignore surrounding spaces settings should not be equal", formatWithIgnoringSpaces.equals(formatWithoutIgnoringSpaces));
    }


    @Test
    public void testEqualsWithIgnoreEmptyLines() {
        CSVFormat formatWithIgnoringEmptyLines = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVFormat formatWithoutIgnoringEmptyLines = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse("Formats with different ignore empty lines settings should not be equal", formatWithIgnoringEmptyLines.equals(formatWithoutIgnoringEmptyLines));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparators() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse("Formats with different record separators should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithNullCommentMarkerFixed() {
        CSVFormat formatWithNullComment = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat formatWithValidComment = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse("Formats with different comment markers should not be equal", formatWithNullComment.equals(formatWithValidComment));
    }


    @Test
    public void testEqualsWithDifferentNullStringRepresentationsFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withNullString("\\N");
        CSVFormat format2 = CSVFormat.DEFAULT.withNullString("N/A");
        assertFalse("Formats with different null string representations should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentSkipHeaderRecords() {
        CSVFormat format1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withSkipHeaderRecord(false);
        assertFalse("Formats with different skipHeaderRecord settings should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentRecordSeparatorsFixed() {
        CSVFormat format1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat format2 = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertFalse("Formats with different record separators should not be equal", format1.equals(format2));
    }


    @Test
    public void testEqualsWithDifferentQuoteCharacters() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse("Formats with different quote characters should not be equal", format1.equals(format2));
    }


    @Test
    public void testHashCodeWithDifferentQuoteModes() {
        CSVFormat format1 = CSVFormat.DEFAULT.withQuote('"');
        CSVFormat format2 = CSVFormat.DEFAULT.withQuote('\'');
        assertNotEquals("Hash codes should be different for formats with different quote modes", format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testHashCodeWithDifferentCommentMarkers() {
        CSVFormat format1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat format2 = CSVFormat.DEFAULT.withCommentMarker(';');
        assertNotEquals("Hash codes should be different for formats with different comment markers", format1.hashCode(), format2.hashCode());
    }


    @Test
    public void testIsNullStringSet() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        assertTrue("Null string should be set", format.isNullStringSet());
    }


    @Test
    public void testIsQuoteCharacterSet() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        assertTrue("Quote character should be set", format.isQuoteCharacterSet());
    }


    @Test
    public void testGetRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("Record separator should be set correctly", "\n", format.getRecordSeparator());
    }


    @Test
    public void testGetIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue("Should ignore empty lines", format.getIgnoreEmptyLines());
    }


    @Test
    public void testWithQuoteModeAll() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertTrue("Quote character should be recognized as set", format.isQuoteCharacterSet());
    }


    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertTrue("Comment marker should be recognized", format.isCommentMarkerSet());
    }


    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        assertTrue("Null string should be recognized as set", format.isNullStringSet());
    }


    @Test
    public void testWithTrimEnabled() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue("Trimming should be enabled", format.getTrim());
    }


    @Test
    public void testWithCustomRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("Record separator should be set to the custom value", "\n", format.getRecordSeparator());
    }


    @Test
    public void testWithIgnoreEmptyLines() throws IOException {
        String csvData = "Column1,Column2\n\nValue1,Value2\n\nValue3,Value4";
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVParser parser = format.parse(new java.io.StringReader(csvData));
        java.util.List<CSVRecord> records = parser.getRecords();
        assertEquals("There should be 3 records", 3, records.size());
        assertEquals("Value1 should be in the first record", "Value1", records.get(1).get(0));
        assertEquals("Value2 should be in the first record", "Value2", records.get(1).get(1));
    }


    @Test
    public void testWithCustomEscapeCharacter() throws IOException {
        String csvData = "Column1,Column2\nValue1,\"Value2 with a quote: \\\"\"\nValue3,Value4";
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        CSVParser parser = format.parse(new java.io.StringReader(csvData));
        java.util.List<CSVRecord> records = parser.getRecords();
        assertEquals("There should be 3 records", 3, records.size());
        assertEquals("Value2 should be correctly parsed", "Value2 with a quote: \"", records.get(1).get(1));
    }


    @Test
    public void testEqualsWithDifferentIgnoreSurroundingSpaces() {
        CSVFormat format1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat format2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false);
        assertFalse("Formats with different ignore surrounding spaces settings should not be equal", format1.equals(format2));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

