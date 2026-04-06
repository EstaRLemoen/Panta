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

import static org.apache.commons.csv.Token.Type.TOKEN;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.io.ByteArrayInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVParserTest {


    @Test(expected = IllegalArgumentException.class)
    public void testParseFormatNull() throws IOException {
        CSVParser.parse(new File("test.csv"), Charset.defaultCharset(), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamNull() throws IOException {
        CSVParser.parse((InputStream) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlCharsetNull() throws IOException {
        CSVParser.parse(new URL("http://example.com"), null, CSVFormat.DEFAULT);
    }


    @Test
    public void testGetRecordsEmpty() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader(""), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
    }


    @Test
    public void testGetRecordsWithTrailingDelimiter() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorClosedParser() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser immediately
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterEmptyLastRecord() throws IOException {
        String csvInput = "value1,value2,\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterEmptyRecord() throws IOException {
        String csvInput = "value1,value2,\n"; // CSV input with trailing delimiter and empty last record
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterFixed() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testGetHeaderMapCaseSensitivityFixed() throws IOException {
        String csvInput = "Header1,header1\nvalue1,value2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader("Header1", "header1"));
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertEquals(Integer.valueOf(0), headerMap.get("Header1"));
        assertEquals(Integer.valueOf(1), headerMap.get("header1"));
    }


    @Test
    public void testGetRecordsWithLeadingAndTrailingSpaces() throws IOException {
        String csvInput = "  value1 , value2  \n  value3 , value4  ";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withTrim(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testGetRecordsWithNullStringRepresentation() throws IOException {
        String csvInput = "value1,null,value3\nvalue4,null,value6";
        CSVFormat format = CSVFormat.DEFAULT.withNullString("null");
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertNull(records.get(0).get(1)); // Expect null instead of "null"
        assertEquals("value3", records.get(0).get(2));
        assertEquals("value4", records.get(1).get(0));
        assertNull(records.get(1).get(1)); // Expect null instead of "null"
        assertEquals("value6", records.get(1).get(2));
    }


    @Test
    public void testGetRecordsWithSkippedHeader() throws IOException {
        String csvInput = "Header1,Header2\nvalue1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithDuplicateHeaders() throws IOException {
        String csvInput = "Header1,Header1\nvalue1,value2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames(false);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        parser.getRecords(); // This should throw an IllegalArgumentException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseFileWithNullFormat() throws IOException {
        File testFile = new File("test.csv"); // Assuming this file exists for the test
        CSVParser.parse(testFile, Charset.defaultCharset(), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateHeaders() throws IOException {
        String csvInput = "Header1,Header1\nvalue1,value2"; // Duplicate headers
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames(false);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        parser.getRecords(); // This should throw an IllegalArgumentException
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorAfterClose() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamWithNullFormat() throws IOException {
        CSVParser.parse(new ByteArrayInputStream("value1,value2\n".getBytes()), Charset.defaultCharset(), null);
    }


    @Test
    public void testGetRecordsWithNoHeader() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withSkipHeaderRecord(true));
        List<CSVRecord> records = parser.getRecords();
        assertNull(parser.getHeaderMap());
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithDuplicateHeadersFixed() throws IOException {
        String csvInput = "Header1,Header1\nvalue1,value2"; // Duplicate headers
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames(false);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        parser.getRecords(); // This should throw an IllegalArgumentException
    }


    @Test
    public void testGetRecordsWithComments() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testNextAfterClose() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IOException.class)
    public void testCloseDuringParsing() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the lexer during parsing
        parser.getRecords(); // This should throw an IOException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseFileWithNullFormatFixed() throws IOException {
        File testFile = new File("test.csv"); // Assuming this file exists for the test
        CSVParser.parse(testFile, Charset.defaultCharset(), null);
    }


    @Test
    public void testGetRecordsWithSkipHeader() throws IOException {
        String csvInput = "Header1,Header2\nvalue1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testGetRecordsWithCommentLine() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testNextAfterCloseFixed() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullFormat() throws IOException {
        CSVParser.parse(new File("test.csv"), Charset.defaultCharset(), null);
    }


    @Test
    public void testGetRecordsWithCommentsFixed() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorAfterCloseFixed() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IOException.class)
    public void testGetRecordsThrowsIOExceptionWhenLexerClosed() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.getRecords(); // Start parsing
        parser.close(); // Close the lexer during parsing
        parser.getRecords(); // This should throw an IOException
    }


    @Test
    public void testGetRecordsWithCaseInsensitiveHeaders() throws IOException {
        String csvInput = "Header1,value1\nheader1,value2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withIgnoreHeaderCase(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertEquals(Integer.valueOf(0), headerMap.get("Header1")); // Should only have one entry
        assertNull(headerMap.get("header1")); // Should not have a second entry
    }


    @Test
    public void testGetRecordsWithCommentBeforeRecord() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testGetRecordsWithHeaderButNoRecords() throws IOException {
        String csvInput = "Header1,Header2\n"; // Header only, no records
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty()); // Expect no records to be returned
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

