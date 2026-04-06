/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Comparator;

public class HelpFormatterTest {


    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(null, new Options());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("", new Options());
    }


    @Test
    public void testPrintHelpWithHeaderAndFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "Header text";
        String footer = "Footer text";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", header, options, 1, 3, footer, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(header));
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpAutoUsageTrue() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, true);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp"));
    }


    @Test
    public void testPrintHelpWithNonNullHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "Header text";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", header, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithNonNullFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "Footer text";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, footer, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithNonEmptyHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a non-empty header";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", header, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithNonEmptyFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a non-empty footer";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, footer, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testAppendOptionWithArgument() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "The file to be processed");
        option.setArgName("FILE");
        options.addOption(option);
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printOptions(writer, HelpFormatter.DEFAULT_WIDTH, options, 1, 3);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains("-f,--file <FILE>   The file to be processed"));
    }


    @Test
    public void testPrintHelpWithHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", header, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a footer";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, footer, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithOptionArgument() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "The file to be processed");
        option.setArgName("FILE");
        options.addOption(option);
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains("-f,--file <FILE>   The file to be processed"));
    }


    @Test
    public void testPrintHelpWithCustomComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option optionA = new Option("a", "alpha", false, "Alpha option");
        Option optionB = new Option("b", "beta", false, "Beta option");
        options.addOption(optionA);
        options.addOption(optionB);
        
        // Set a custom comparator
        Comparator<Option> customComparator = new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o1.getLongOpt().compareTo(o2.getLongOpt());
            }
        };
        formatter.setOptionComparator(customComparator);
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.indexOf("--alpha") < output.indexOf("--beta"));
    }


    @Test
    public void testRenderOptionsWithNullComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(new Option("b", "beta", false, "Beta option"));
        options.addOption(new Option("a", "alpha", false, "Alpha option"));
        
        // Set the comparator to null
        formatter.setOptionComparator(null);
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printOptions(writer, HelpFormatter.DEFAULT_WIDTH, options, 1, 3);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.indexOf("-b") < output.indexOf("-a"));
    }


    @Test
    public void testPrintHelpWithNonEmptyHeaderFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a non-empty header";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", header, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithNonEmptyFooterFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a non-empty footer";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, footer, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithHeaderProvided() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", header, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithFooterProvided() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a footer";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, footer, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithNoOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertFalse(output.contains("-"));
    }


    @Test
    public void testPrintHelpWithHeaderFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", header, options, 1, 3, null, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithFooterFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a footer";
        
        // Capture the output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, footer, false);
        
        // Validate the output
        String output = stringWriter.toString();
        assertTrue(output.contains(footer));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

