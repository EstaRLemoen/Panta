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
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CSVParserTest {


    @Test(expected = IllegalArgumentException.class)
    public void testParseNullFile() throws IOException {
        CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseNullInputStream() throws IOException {
        CSVParser.parse((InputStream) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseNullPath() throws IOException {
        CSVParser.parse((Path) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseNullReader() throws IOException {
        CSVParser.parse((Reader) null, CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseNullURL() throws IOException {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = NoSuchElementException.class)
    public void testNextAfterClose() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserIteration() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiter() throws IOException {
        String csvInput = "value1,value2,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2", ""}, records.get(0).values());
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorAfterClose() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterHandling() throws IOException {
        String csvInput = "value1,value2,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2", ""}, records.get(0).values());
    }


    @Test
    public void testGetHeaderMapCaseSensitivity() throws IOException {
        String csvInput = "Name,Age,name,age\nJohn,30\nDoe,25";
        // Use a format with explicit header to get headerMap initialized
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Name", "Age", "name", "age").withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("Name"));
        assertEquals(Integer.valueOf(1), headerMap.get("Age"));
        assertEquals(Integer.valueOf(2), headerMap.get("name"));
        assertEquals(Integer.valueOf(3), headerMap.get("age"));
    }


    @Test
    public void testGetRecordsWithHeaderSkipped() throws IOException {
        String csvInput = "Header1,Header2\nvalue1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true).withHeader();
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Should not include header
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
        assertArrayEquals(new String[]{"value3", "value4"}, records.get(1).values());
    }


    @Test(expected = IOException.class)
    public void testGetRecordsWithInvalidTokens() throws IOException {
        // The CSVParser and Lexer do not support invalid tokens like '!!invalid!!' by default,
        // so to simulate invalid token, we create a custom format or input that triggers INVALID token.
        // Since the current CSVParser does not support invalid tokens in DEFAULT format,
        // we simulate by using malformed input that lexer would treat as invalid.
        // For demonstration, we use a malformed CSV with unclosed quotes.
        String csvInput = "\"value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.getRecords(); // This should throw an IOException due to invalid token (unclosed quote)
    }


    @Test
    public void testGetRecordsWithHeaderSkippedUnique() throws IOException {
        String csvInput = "Header1,Header2\nvalue1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true).withHeader();
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Should not include header
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
        assertArrayEquals(new String[]{"value3", "value4"}, records.get(1).values());
    }


    @Test
    public void testGetHeaderMapCaseSensitivityWithIgnoreCase() throws IOException {
        String csvInput = "Name,Age,Location\nJohn,30,USA\nDoe,25,Canada";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("name", "AGE", "location").withIgnoreHeaderCase(true).withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("name"));
        assertEquals(Integer.valueOf(1), headerMap.get("AGE"));
        assertEquals(Integer.valueOf(2), headerMap.get("location"));
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorThrowsExceptionWhenClosed() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithLeadingAndTrailingSpaces() throws IOException {
        String csvInput = "  value1 , value2  \n  value3 , value4  ";
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
        assertArrayEquals(new String[]{"value3", "value4"}, records.get(1).values());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithDuplicateHeaders() throws IOException {
        String csvInput = "Header,Header\nvalue1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(false);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        parser.getRecords(); // This should throw an IllegalArgumentException due to duplicate headers
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorThrowsExceptionWhenClosedFixed() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithEmptyHeader() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNull(headerMap); // Expecting headerMap to be null as there is no header
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterHandlingFixed() throws IOException {
        String csvInput = "value1,value2,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2", ""}, records.get(0).values());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithDuplicateHeadersFixed() throws IOException {
        String csvInput = "header1,header1\nvalue1,value2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(false);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        parser.getRecords(); // This should throw an IllegalArgumentException due to duplicate headers
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorThrowsExceptionWhenClosedDuringIteration() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterFixed() throws IOException {
        String csvInput = "value1,value2,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2", ""}, records.get(0).values());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

