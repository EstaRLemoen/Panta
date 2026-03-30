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
import static org.junit.Assert.assertTrue;
import java.nio.file.Path;
import java.nio.file.Files;

public class CSVParserTest {


    @Test
    public void testParseEmptyCSVString() throws IOException {
        String csvData = "";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullFormat() throws IOException {
        String csvData = "name,age\nAlice,30";
        CSVParser.parse(new StringReader(csvData), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullInputStream() throws IOException {
        CSVParser.parse((InputStream) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullPath() throws IOException {
        CSVParser.parse((Path) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullURL() throws IOException {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithNullFormat() throws IOException {
        String csvData = "name,age\nAlice,30";
        CSVParser parser = CSVParser.parse(csvData, null);
        parser.getRecords();
    }


    @Test
    public void testAddRecordValueWithTrimTrailingDelimiterAndNullString() throws IOException {
        // Create a CSVFormat with trim=true, trailingDelimiter=true, and nullString="NULL"
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true).withTrailingDelimiter().withNullString("NULL");
        String csvData = " NULL ,value2\nvalue3,NULL\n";
        CSVParser parser = new CSVParser(new StringReader(csvData), format);
    
        // Read first record
        CSVRecord record1 = parser.nextRecord();
        assertNotNull(record1);
        // First value should be null due to nullString match after trim
        assertNull(record1.get(0));
        assertEquals("value2", record1.get(1));
    
        // Read second record
        CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals("value3", record2.get(0));
        assertNull(record2.get(1));
    
        // There should be no more records
        assertNull(parser.nextRecord());
    
        parser.close();
    }


    @Test
    public void testCloseAndIsClosed() throws IOException {
        String csvData = "a,b\n1,2";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }


    @Test
    public void testGetHeaderMapNullAndCopy() throws IOException {
        // Format with no header defined
        CSVFormat formatNoHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        CSVParser parserNoHeader = new CSVParser(new StringReader("a,b\n1,2"), formatNoHeader);
        assertNull(parserNoHeader.getHeaderMap());
    
        // Format with header defined explicitly
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader("col1", "col2");
        CSVParser parserWithHeader = new CSVParser(new StringReader("x,y\n1,2"), formatWithHeader);
        Map<String, Integer> headerMap = parserWithHeader.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("col2"));
    
        // Modifying returned map should not affect internal headerMap
        headerMap.put("newCol", 99);
        Map<String, Integer> headerMap2 = parserWithHeader.getHeaderMap();
        assertFalse(headerMap2.containsKey("newCol"));
    }


    @Test
    public void testNextRecordHandlesComments() throws IOException {
        // Use a CSVFormat with comment marker
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        String csvData = "#comment1\n#comment2\nvalue1,value2\n";
        CSVParser parser = new CSVParser(new StringReader(csvData), format);
    
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        String comment = record.getComment();
        assertNotNull(comment);
        assertTrue(comment.contains("comment1"));
        assertTrue(comment.contains("comment2"));
        assertEquals("value1", record.get(0));
        assertEquals("value2", record.get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInitializeHeaderThrowsOnDuplicateHeaders() throws IOException {
        String[] headers = new String[] {"dup", "dup"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers).withAllowMissingColumnNames(false);
        new CSVParser(new StringReader("a,b\n1,2"), format);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFormat() throws IOException {
        File file = File.createTempFile("test", ".csv");
        try {
            CSVParser.parse(file, Charset.defaultCharset(), null);
        } finally {
            file.delete();
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamNullInputStream() throws IOException {
        CSVParser.parse((InputStream) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamNullFormat() throws IOException {
        InputStream is = new java.io.ByteArrayInputStream("a,b\n1,2".getBytes());
        try {
            CSVParser.parse(is, Charset.defaultCharset(), null);
        } finally {
            is.close();
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNullPath() throws IOException {
        CSVParser.parse((Path) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNullFormat() throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        try {
            CSVParser.parse(tempFile, Charset.defaultCharset(), null);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullURL() throws IOException {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullCharset() throws IOException {
        URL url = new URL("http://example.com");
        CSVParser.parse(url, null, CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseURLNullFormat() throws IOException {
        URL url = new URL("http://example.com");
        CSVParser.parse(url, Charset.defaultCharset(), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseReaderNullReader() throws IOException {
        CSVParser.parse((Reader) null, CSVFormat.DEFAULT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseReaderNullFormat() throws IOException {
        CSVParser.parse(new StringReader("a,b\n1,2"), null);
    }


    @Test
    public void testCloseParserReleasesResources() throws IOException {
        String csvData = "a,b\n1,2";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }


    @Test
    public void testTrailingDelimiterHandling() throws IOException {
        // Create a CSVFormat with trailingDelimiter=true
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        String csvData = "value1,value2,\nvalue3,value4,\n"; // Ends with a delimiter
    
        CSVParser parser = new CSVParser(new StringReader(csvData), format);
        List<CSVRecord> records = parser.getRecords();
    
        // There should be two records, and the last record should not be empty
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    
        // Ensure that the trailing delimiter does not create an empty record
        assertNull(parser.nextRecord());
        parser.close();
    }


    @Test
    public void testTrailingDelimiterEmptyRecords() throws IOException {
        // Create a CSVFormat with trailingDelimiter=true
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        String csvData = "value1,value2,\nvalue3,value4,\n"; // Ends with a delimiter
    
        CSVParser parser = new CSVParser(new StringReader(csvData), format);
        List<CSVRecord> records = parser.getRecords();
    
        // There should be two records, and the last record should not be empty
        assertEquals(2, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    
        // Ensure that the trailing delimiter does not create an empty record
        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

