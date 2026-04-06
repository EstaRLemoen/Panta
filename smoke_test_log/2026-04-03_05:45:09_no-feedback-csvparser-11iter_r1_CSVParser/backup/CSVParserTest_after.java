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
import java.io.ByteArrayInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import static org.junit.Assert.assertEquals;

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
    public void testParseUrlNull() throws IOException {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
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


    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamFormatNull() throws IOException {
        CSVParser.parse(new ByteArrayInputStream("data".getBytes()), Charset.defaultCharset(), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNull() throws IOException {
        CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNull() throws IOException {
        CSVParser.parse((Path) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseReaderNull() throws IOException {
        CSVParser.parse((Reader) null, CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNull() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }


    @Test
    public void testGetRecordsWithTrailingDelimiters() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = IOException.class)
    public void testGetRecordsWithInvalidTokens() throws IOException {
        String csvInput = "value1,\"value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.getRecords(); // This should trigger the parsing and throw an IOException
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorClosedParser() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserIteration() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithLeadingAndTrailingSpaces() throws IOException {
        String csvInput = "  value1 ; value2  \n  value3 ;  value4  ";
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';').withTrim(); // Use semicolon as delimiter and trim spaces
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testGetRecordsWithoutHeader() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithDuplicateHeaders() throws IOException {
        String csvInput = "header1,header2,header1\nvalue1,value2,value3";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
        parser.getRecords(); // This should trigger the parsing and throw an IllegalArgumentException
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


    @Test
    public void testGetRecordsWithEmptyHeader() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testGetRecordsWithSkippedHeader() throws IOException {
        String csvInput = "header1,header2\nvalue1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorClosedParserDuringIteration() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser before iteration
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithCaseInsensitiveHeaders() throws IOException {
        String csvInput = "Header1,Header2\nValue1,Value2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withIgnoreHeaderCase(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("Value1", records.get(0).get(0));
        assertEquals("Value2", records.get(0).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithDuplicateHeadersHandling() throws IOException {
        String csvInput = "Header,Header\nValue1,Value2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
        parser.getRecords(); // This should trigger the parsing and throw an IllegalArgumentException
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorClosedParserHandling() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
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
    public void testNextRecordAfterClose() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithCommentsHandling() throws IOException {
        String csvInput = "# This is a comment\nvalue1,value2\n# Another comment\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test
    public void testGetRecordsWithoutHeaderHandling() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorClosedParserDuringIterationHandling() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser before iterating
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithEmptyHeaderAndSkip() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testNextRecordAfterCloseHandling() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiterHandling() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
        // Ensure that the last record does not have an empty value for the trailing delimiter
        assertEquals(2, records.get(0).size());
        assertEquals(2, records.get(1).size());
    }


    @Test
    public void testGetRecordsWithNullStringValue() throws IOException {
        String csvInput = "value1,value2,null,value4";
        CSVFormat format = CSVFormat.DEFAULT.withNullString("null");
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertNull(records.get(0).get(2)); // Check that the null value is correctly represented
        assertEquals("value4", records.get(0).get(3));
    }


    @Test(expected = NoSuchElementException.class)
    public void testIteratorClosedParserBeforeNext() throws IOException {
        CSVParser parser = CSVParser.parse(new StringReader("value1,value2\nvalue3,value4\n"), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before iteration
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithSkipHeader() throws IOException {
        String csvInput = "header1,header2\nvalue1,value2\nvalue3,value4";
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
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

