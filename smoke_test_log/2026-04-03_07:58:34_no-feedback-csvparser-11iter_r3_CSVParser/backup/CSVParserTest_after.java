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
    public void testClosedParserIteration() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before iteration
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test
    public void testTrimmingBehaviorForInputValues() throws IOException {
        String csvInput = "  value1  ,  value2  ";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrim(true));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testTrimmingBehaviorForInputValuesWithSpaces() throws IOException {
        String csvInput = "  value1; value2;  ";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrim(true).withDelimiter(';'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testHandlingOfTrailingDelimiters() throws IOException {
        String csvInput = "value1;value2;";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withDelimiter(';'));
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }


    @Test
    public void testParseWithoutHeaderAndSkipHeaderRecord() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Ensure records are parsed correctly
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserIterationBehavior() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before iteration
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaders() throws IOException {
        String csvInput = "header1,header1\nvalue1,value2";
        CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
    }


    @Test
    public void testHandlingOfEmptyTrailingRecords() throws IOException {
        String csvInput = "value1,value2,\nvalue3,value4,\n\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size()); // Ensure only two records are parsed
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
        assertEquals("value3", records.get(1).get(0));
        assertEquals("value4", records.get(1).get(1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeadersFixed() throws IOException {
        String csvInput = "Name,Name,Location\nAlice,30,New York";
        CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
    }


    @Test
    public void testNoHeaderDefinedFirstRecord() throws IOException {
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
    public void testHandlingOfDuplicateHeaders() throws IOException {
        String csvInput = "Name,Name,Location\nAlice,30,New York";
        CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserNextRecord() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before calling next
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCaseSensitivityInHeaderMapping() throws IOException {
        String csvInput = "Name,Age,name\nAlice,30,Bob";
        CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withIgnoreHeaderCase().withHeader());
    }


    @Test
    public void testEmptyHeaderRecord() throws IOException {
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
    public void testDuplicateHeadersInCSV() throws IOException {
        String csvInput = "header1,header1\nvalue1,value2";
        CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaderNames() throws IOException {
        String csvInput = "header1,header1\nvalue1,value2";
        CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserDuringIteration() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser before calling next
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

