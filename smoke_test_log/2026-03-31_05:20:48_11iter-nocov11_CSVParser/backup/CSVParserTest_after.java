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
import static org.junit.Assert.assertArrayEquals;

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
        String csvInput = "a,b,c\n1,2,3\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        parser.close(); // Close the parser
        parser.iterator().next(); // This should throw NoSuchElementException
    }


    @Test
    public void testGetRecordsWithTrailingDelimiter() throws IOException {
        String csvInput = "a,b,c,\n";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertArrayEquals(new String[]{"a", "b", "c", ""}, records.get(0).values());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateHeaders() throws IOException {
        String csvInput = "header1,header1\nvalue1,value2";
        CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
    }


    @Test
    public void testGetHeaderMapReturnsNullForDuplicateHeaders() throws IOException {
        String csvInput = "name,name,age\nAlice,25\nBob,30";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.EXCEL.withAllowMissingColumnNames());
        assertNull(parser.getHeaderMap());
    }


    @Test
    public void testGetRecordsWithEmptyTrailingDelimiterAndTrimEnabled() throws IOException {
        String csvInput = "   \n"; // Input with spaces
        CSVFormat format = CSVFormat.DEFAULT.withTrim().withTrailingDelimiter();
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), format);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty()); // Expecting an empty list
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordsWithDuplicateHeaders() throws IOException {
        String csvInput = "header1,header1\nvalue1,value2";
        CSVParser parser = CSVParser.parse(new StringReader(csvInput), CSVFormat.DEFAULT.withHeader());
        parser.getRecords(); // This should throw IllegalArgumentException due to duplicate headers
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}
