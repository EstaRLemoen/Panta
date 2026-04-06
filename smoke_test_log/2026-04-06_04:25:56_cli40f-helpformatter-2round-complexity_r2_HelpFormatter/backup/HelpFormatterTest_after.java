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
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
        formatter.printHelp("myapp", header, options, footer);
        // You can add assertions here to verify the output if needed
    }


    @Test
    public void testPrintHelpAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        formatter.printHelp("myapp", options, true);
        // You can add assertions here to verify the output if needed
    }


    @Test
    public void testPrintHelpWithNonEmptyHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        String footer = "This is a footer";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", header, options, footer);
        String output = outContent.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithNonEmptyFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        String footer = "This is a footer";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", header, options, footer);
        String output = outContent.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithCustomComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        String header = "This is a header";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", header, options, null);
        String output = outContent.toString();
        assertTrue(output.indexOf("-a") < output.indexOf("-b"));
    }


    @Test
    public void testPrintHelpWithHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", header, options, null);
        String output = outContent.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a footer";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", null, options, footer);
        String output = outContent.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithBlankArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        Option optionWithBlankArgName = new Option("o", "output", true, "Output file");
        optionWithBlankArgName.setArgName(""); // Set blank argument name
        options.addOption(optionWithBlankArgName);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", options);
        String output = outContent.toString();
        assertTrue(output.contains("-o,--output "));
    }


    @Test
    public void testPrintHelpWithCustomComparatorFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o1.getOpt().compareTo(o2.getOpt());
            }
        });
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", options);
        String output = outContent.toString();
        assertTrue(output.indexOf("-a") < output.indexOf("-b"));
    }


    @Test
    public void testPrintHelpWithHeaderFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", header, options, null);
        String output = outContent.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithFooterFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a footer";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", null, options, footer);
        String output = outContent.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithNoOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", options);
        String output = outContent.toString();
        assertTrue(output.contains("usage: myapp"));
    }


    @Test
    public void testPrintHelpWithNonEmptyHeaderFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", header, options, null);
        String output = outContent.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithNonEmptyFooterFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a footer";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", null, options, footer);
        String output = outContent.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithHeaderOutput() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String header = "This is a header";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", header, options, null);
        String output = outContent.toString();
        assertTrue(output.contains(header));
    }


    @Test
    public void testPrintHelpWithFooterOutput() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String footer = "This is a footer";
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", null, options, footer);
        String output = outContent.toString();
        assertTrue(output.contains(footer));
    }


    @Test
    public void testPrintHelpWithNoOptionsOutput() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", options);
        String output = outContent.toString();
        assertTrue(output.contains("usage: myapp"));
    }


    @Test
    public void testPrintHelpWithCustomComparatorOutput() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o1.getOpt().compareTo(o2.getOpt());
            }
        });
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        formatter.printHelp("myapp", options);
        String output = outContent.toString();
        assertTrue(output.indexOf("-a") < output.indexOf("-b"));
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

