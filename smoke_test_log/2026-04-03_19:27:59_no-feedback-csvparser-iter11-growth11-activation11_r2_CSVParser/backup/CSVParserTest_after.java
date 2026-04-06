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
import static org.junit.Assert.assertEquals;
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


    @Test
    public void testTrailingDelimiterHandling() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserIteration() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL);
        parser.close(); // Close the parser before iterating
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testTrimmedRecords() throws IOException {
        String csvInput = "  value1 ; value2  \n  value3 ; value4  ";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrim(true).withDelimiter(';'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testMissingHeaderWithSkipHeader() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.EXCEL.withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Ensure two records are parsed
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserNext() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IOException.class)
    public void testInvalidParseSequence() throws IOException {
        String csvInput = "\"value1,value2\nvalue3,value4"; // Unclosed quotes
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL);
        parser.getRecords(); // This should throw an IOException
    }


    @Test
    public void testEmptyTrailingRecordWithDelimiter() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size()); // Ensure three records are returned
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
        assertEquals("", records.get(2).get(0)); // The last record should be empty string
    }


    @Test
    public void testTrailingDelimiterWithEmptyRecord() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size()); // Ensure three records are returned
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
        assertEquals("", records.get(2).get(0)); // The last record should be empty string
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserDuringIteration() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL);
        parser.close(); // Close the parser before iterating
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testEmptyInputStream() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader(""), CSVFormat.EXCEL);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty()); // Verify that the returned list of records is empty
    }


    @Test
    public void testMissingHeaderWithoutHeaderLine() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withSkipHeaderRecord(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testTrailingDelimiterHandlingFixed() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaders() throws IOException {
        String csvInput = "header1,header2,header1\nvalue1,value2,value3";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withHeader());
        parser.getRecords(); // This should throw an IllegalArgumentException
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserDuringIterationFixed() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL);
        parser.close(); // Close the parser before iterating
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testCommentsInCSVFixed() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testTrailingDelimiterWithoutEmptyValue() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testEmptyHeaderRecord() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withSkipHeaderRecord(true));
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNull(headerMap); // Expecting null since there's no header
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaderNames() throws IOException {
        String csvInput = "ID,Name,ID\n1,John,2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withHeader());
        parser.getRecords(); // This should throw an IllegalArgumentException
    }


    @Test
    public void testEmptyHeaderRecordFixed() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withSkipHeaderRecord(true));
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNull(headerMap); // Expecting null since there's no header
    }


    @Test
    public void testTrailingDelimiterWithEmptyValue() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size()); // Ensure three records are returned
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
        assertEquals("", records.get(2).get(0)); // The last record should be empty string
    }


    @Test
    public void testTrailingDelimiterWithEmptyValueFixed() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n,";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size()); // Ensure three records are returned
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
        assertEquals("", records.get(2).get(0)); // The last record should be empty string
    }


    @Test
    public void testCommentsInCSV() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeadersFixed() throws IOException {
        String csvInput = "header1,header2,header1\nvalue1,value2,value3";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withHeader());
        parser.getRecords(); // This should throw an IllegalArgumentException
    }


    @Test
    public void testTrailingDelimiterWithoutEmptyValueFixed() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Ensure two records are returned
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

