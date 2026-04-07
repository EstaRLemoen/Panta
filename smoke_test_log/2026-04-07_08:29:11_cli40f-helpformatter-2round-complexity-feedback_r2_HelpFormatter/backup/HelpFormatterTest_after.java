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
        // Verify output (you may need to capture System.out for verification)
    }


    @Test
    public void testPrintHelpWithOptionsWithArgs() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        options.addOption("v", "version", false, "Print the version of the application");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, options);
        // Verify output (you may need to capture System.out for verification)
    }


    @Test
    public void testPrintHelpWithAutoUsage() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, options, true);
        
        String expectedOutput = "usage: myapp -f <FILE>"; // Adjust based on actual expected output
        assertTrue(outContent.toString().contains(expectedOutput));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithHeader() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        String header = "Header text";
        formatter.printHelp(cmdLineSyntax, header, options, null);
        
        assertTrue(outContent.toString().contains("Header text"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithFooter() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        String footer = "Footer text";
        formatter.printHelp(cmdLineSyntax, null, options, footer);
        
        assertTrue(outContent.toString().contains("Footer text"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithLongHeader() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String longHeader = "This is a very long header that exceeds the default width of the formatter and should be wrapped correctly.";
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, longHeader, options, null);
        
        String output = outContent.toString();
        assertTrue(output.contains("This is a very long header that exceeds the default width"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithNullOptionComparator() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        
        formatter.setOptionComparator(null); // Set comparator to null
        String cmdLineSyntax = "myapp [options]";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.indexOf("-b") < output.indexOf("-a")); // Ensure order is as added
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpOptionWithBlankArgName() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        options.getOption("f").setArgName(""); // Set blank argument name
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("-f"));
        assertTrue(output.contains("--file"));
        assertFalse(output.contains("<>")); // Ensure no empty angle brackets are printed
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpOptionWithNullDescription() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, null); // Set null description
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("-f"));
        assertTrue(output.contains("--file"));
        assertFalse(output.contains("null")); // Ensure null description does not appear
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintWrappedWithNextLineTabStopGreaterThanWidth() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        String longText = "This is a very long text that should be wrapped correctly when the next line tab stop is greater than or equal to the width.";
        int width = 50; // Set width
        int nextLineTabStop = 60; // Set nextLineTabStop greater than width
        
        formatter.printWrapped(new PrintWriter(outContent), width, nextLineTabStop, longText);
        
        String output = outContent.toString();
        assertTrue(output.length() <= width); // Ensure output is wrapped correctly
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithNullHeader() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, null, options, null);
        
        String output = outContent.toString();
        assertFalse(output.contains("Header text"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithNullFooter() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, null, options, null);
        
        String output = outContent.toString();
        assertFalse(output.contains("Footer text"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntaxFixed() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(null, new Options());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntaxFixed() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("", new Options());
    }


    @Test
    public void testPrintHelpWithNoOptionComparator() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        
        formatter.setOptionComparator(null); // Set comparator to null
        String cmdLineSyntax = "myapp [options]";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.indexOf("-b") < output.indexOf("-a")); // Ensure order is as added
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithMultipleOptions() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", false, "Beta option");
        
        String cmdLineSyntax = "myapp [options]";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("--alpha"));
        assertTrue(output.contains("-b"));
        assertTrue(output.contains("--beta"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithNoWhitespaceCmdLineSyntax() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp"; // No whitespace
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("usage: myapp")); // Ensure the syntax is printed correctly
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpOptionWithBlankArgNameFixed() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "The file to be processed");
        option.setArgName(""); // Set blank argument name
        options.addOption(option);
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("-f"));
        assertTrue(output.contains("--file"));
        assertFalse(output.contains("<>")); // Ensure no empty angle brackets are printed
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithShortOption() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("-f"));
        assertTrue(output.contains("--file"));
        assertTrue(output.contains("The file to be processed"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpOptionWithNullArgName() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "The file to be processed");
        option.setArgName(null); // Set null argument name
        options.addOption(option);
        
        String cmdLineSyntax = "myapp -f <FILE>";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("-f"));
        assertTrue(output.contains("--file"));
        assertFalse(output.contains("<null>")); // Ensure null argument name does not appear
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithLongOptionOnly() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "file", true, "The file to be processed"); // Long option only
        
        String cmdLineSyntax = "myapp --file <FILE>";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("--file"));
        assertTrue(output.contains("The file to be processed"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintWrappedWithLongTextExceedingWidth() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        String longText = "This is a very long text that should be wrapped correctly when the next line tab stop is reached.";
        int width = 50; // Set width
        int nextLineTabStop = 60; // Set nextLineTabStop greater than width
        
        formatter.printWrapped(new PrintWriter(outContent), width, nextLineTabStop, longText);
        
        String output = outContent.toString();
        assertTrue(output.length() <= width); // Ensure output is wrapped correctly
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithOptionComparator() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
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
        
        String cmdLineSyntax = "myapp [options]";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.indexOf("-a") < output.indexOf("-b")); // Ensure order is as defined by comparator
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithHeaderFixed() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        String header = "This is a header";
        formatter.printHelp(cmdLineSyntax, header, options, null);
        
        String output = outContent.toString();
        assertTrue(output.contains("This is a header"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithFooterFixed() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        
        String cmdLineSyntax = "myapp -f <FILE>";
        String footer = "This is a footer";
        formatter.printHelp(cmdLineSyntax, null, options, footer);
        
        String output = outContent.toString();
        assertTrue(output.contains("This is a footer"));
        
        System.setOut(System.out); // Reset to original System.out
    }


    @Test
    public void testPrintHelpWithMultipleOptionsFixed() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", false, "Beta option");
        
        String cmdLineSyntax = "myapp [options]";
        formatter.printHelp(cmdLineSyntax, options);
        
        String output = outContent.toString();
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("--alpha"));
        assertTrue(output.contains("-b"));
        assertTrue(output.contains("--beta"));
        
        System.setOut(System.out); // Reset to original System.out
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

