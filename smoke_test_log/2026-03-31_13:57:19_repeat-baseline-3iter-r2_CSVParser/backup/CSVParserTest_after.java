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


    @Test
    public void testInitializeHeaderWithEmptyHeader() throws IOException {
        String csvInput = "value1,value2\nvalue3,value4";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
        List<CSVRecord> records = parser.getRecords();
        
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("value1"));
        assertEquals(Integer.valueOf(1), headerMap.get("value2"));
    }


    @Test
    public void testGetRecordsWithTrailingDelimiter() throws IOException {
        String csvInput = "a,b,\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(true));
        List<CSVRecord> records = parser.getRecords();
        
        assertNotNull(records);
        assertEquals(1, records.size());
        CSVRecord record = records.get(0);
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals(2, record.size()); // Ensure the record size is 2
    }


    @Test
    public void testGetRecordsWithoutTrailingDelimiter() throws IOException {
        String csvInput = "a,b\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withTrailingDelimiter(false));
        List<CSVRecord> records = parser.getRecords();
        
        assertNotNull(records);
        assertEquals(1, records.size());
        CSVRecord record = records.get(0);
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals(2, record.size()); // Ensure the record size is 2
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseCSVWithDuplicateHeader() throws IOException {
        String csvInput = "id,name,id\nvalue1,value2,value3";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("id", "name", "id").withAllowMissingColumnNames(false);
        CSVParser.parse(new StringReader(csvInput), format);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}
