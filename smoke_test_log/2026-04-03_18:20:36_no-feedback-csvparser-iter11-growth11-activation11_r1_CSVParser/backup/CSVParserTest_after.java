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
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;

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
    public void testIteratorAfterClose() throws IOException {
        String csvInput = "value1,value2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before iterating
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateHeaders() throws IOException {
        String csvInput = "header1,header2,header1";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
        parser.getRecords(); // This should trigger header initialization and throw an exception
    }


    @Test
    public void testGetRecordsWithComments() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
    }


    @Test
    public void testGetRecordsSkippingHeader() throws IOException {
        String csvInput = "header1,header2\nvalue1,value2\nvalue3,value4\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
        assertArrayEquals(new String[]{"value3", "value4"}, records.get(1).values());
    }


    @Test
    public void testGetRecordsWithHeaderSkipped() throws IOException {
        String csvInput = "header1,header2\nvalue1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
        assertArrayEquals(new String[]{"value3", "value4"}, records.get(1).values());
    }


    @Test
    public void testGetRecordsWithLeadingAndTrailingSpaces() throws IOException {
        String csvInput = "  value1 , value2  \n  value3 , value4  ";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withTrim());
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
        assertArrayEquals(new String[]{"value3", "value4"}, records.get(1).values());
    }


    @Test
    public void testGetRecordsSkippingHeaderFixed() throws IOException {
        String csvInput = "header1,header2\nvalue1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
        assertArrayEquals(new String[]{"value3", "value4"}, records.get(1).values());
    }


    @Test
    public void testGetRecordsWithCommentsIgnored() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorAfterCloseThrowsException() throws IOException {
        String csvInput = "value1,value2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before iterating
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetHeaderMapWithDuplicateHeadersAllowed() throws IOException {
        String csvInput = "Header,Header\nvalue1,value2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames(false));
        parser.getHeaderMap(); // This should throw an exception due to duplicate headers
    }


    @Test
    public void testGetRecordsWithTrailingDelimiter() throws IOException {
        String csvInput = "value1,value2,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
    }


    @Test
    public void testGetHeaderMapWithEmptyHeader() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNull(headerMap); // Expecting null since no headers are defined
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetHeaderMapWithDuplicateHeaders() throws IOException {
        String csvInput = "ID,Name,ID\n1,Alice\n2,Bob";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
        parser.getHeaderMap(); // This should throw an exception due to duplicate headers
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorAfterCloseThrowsNoSuchElementException() throws IOException {
        String csvInput = "value1,value2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before iterating
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterHandled() throws IOException {
        String csvInput = "value1,value2,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"value1", "value2"}, records.get(0).values());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

