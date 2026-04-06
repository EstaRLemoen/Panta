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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class CSVParserTest {


    @Test
    public void testParseEmptyCSVString() throws IOException {
        String csvData = "";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        
        assertTrue(records.isEmpty());
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserIteration() throws IOException {
        String csvData = "value1;value2";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        parser.close();
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = NoSuchElementException.class)
    public void testClosedParserDuringIteration() throws IOException {
        String csvData = "value1;value2";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser before iterating
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = NoSuchElementException.class)
    public void testNextRecordAfterClose() throws IOException {
        String csvData = "value1,value2";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        parser.close(); // Close the parser before calling next
        iterator.next(); // This should throw NoSuchElementException
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateHeaders() throws IOException {
        String csvData = "header1,header2,header1\nvalue1,value2,value3";
        CSVParser parser = CSVParser.parse(new StringReader(csvData), CSVFormat.DEFAULT.withHeader());
        parser.getRecords(); // This should trigger the header initialization and throw an exception
    }


    @Test
    public void testGetRecordsWithComments() throws IOException {
        String csvData = "# This is a comment\nvalue1,value2";
        CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT.withCommentMarker('#'));
        List<CSVRecord> records = parser.getRecords();
        
        assertEquals(1, records.size());
        assertEquals(2, records.get(0).size());
        assertEquals("value1", records.get(0).get(0));
        assertEquals("value2", records.get(0).get(1));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

