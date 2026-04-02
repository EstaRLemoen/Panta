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
import org.junit.Test;
import java.io.StringReader;
import java.util.List;
import org.junit.Test;
import java.io.StringReader;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CSVParserTest {


    @Test(expected = IllegalArgumentException.class)
    public void testParseFileFormatNull() throws IOException {
        CSVParser.parse(new File("dummy.csv"), Charset.defaultCharset(), null);
    }


    @Test(expected = IOException.class)
    public void testParseFileIOException() throws IOException {
        File file = new File("non_existent_file.csv");
        CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test
    public void testGetRecordsEmpty() throws IOException {
        StringReader reader = new StringReader("");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
    }


    @Test
    public void testGetRecordsSingleRecord() throws IOException {
        StringReader reader = new StringReader("value1,value2");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testGetRecordsWithComments() throws IOException {
        StringReader reader = new StringReader("# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorThrowsExceptionWhenClosed() throws IOException {
        StringReader reader = new StringReader("value1,value2");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
        parser.close();
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiter() throws IOException {
        StringReader reader = new StringReader("value1,value2,");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals(2, records.get(0).size()); // Ensure no empty value for trailing delimiter
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateHeaders() throws IOException {
        StringReader reader = new StringReader("header1,header1,value1");
        CSVParser.parse(reader, CSVFormat.DEFAULT.withHeader());
    }


    @Test
    public void testGetRecordsWithTrimming() throws IOException {
        StringReader reader = new StringReader("  value1 , value2  \n  value3 , value4  ");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withTrim(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateHeadersException() throws IOException {
        StringReader reader = new StringReader("header1,header1,value1");
        CSVParser.parse(reader, CSVFormat.DEFAULT.withHeader());
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorThrowsExceptionWhenClosedAgain() throws IOException {
        StringReader reader = new StringReader("value1,value2");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
        parser.close();
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithEmptyTrailingRecord() throws IOException {
        StringReader reader = new StringReader("value1,value2,");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals(2, records.get(0).size()); // Ensure no empty value for trailing delimiter
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorThrowsExceptionWhenClosedDuringIteration() throws IOException {
        StringReader reader = new StringReader("value1,value2");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close before fetching next
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterNoEmptyValue() throws IOException {
        StringReader reader = new StringReader("value1,value2,");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals(2, records.get(0).size()); // Ensure no empty value for trailing delimiter
    }


    @Test
    public void testGetRecordsSkipHeader() throws IOException {
        StringReader reader = new StringReader("header1,header2\nvalue1,value2");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterHandling() throws IOException {
        StringReader reader = new StringReader("value1,value2,");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals(2, records.get(0).size()); // Ensure no empty value for trailing delimiter
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterHandlingFixed() throws IOException {
        StringReader reader = new StringReader("value1,value2,");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals(2, records.get(0).size()); // Ensure no empty value for trailing delimiter
    }


    @Test
    public void testGetRecordsSkipHeaderFixed() throws IOException {
        StringReader reader = new StringReader("header1,header2\nvalue1,value2");
        CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateHeadersFixed() throws IOException {
        StringReader reader = new StringReader("Header,Header\nvalue1,value2");
        CSVParser.parse(reader, CSVFormat.DEFAULT.withHeader());
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

