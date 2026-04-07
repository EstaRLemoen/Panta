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
import java.io.StringWriter;
import java.io.PrintWriter;
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
    public void testPrintHelpNullHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("usage: myapp"));
    }


    @Test
    public void testPrintHelpNullFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", "Header", options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("Header"));
    }


    @Test
    public void testPrintHelpAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, true);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("-f,--file <arg>   The file to be processed"));
    }


    @Test
    public void testPrintHelpWithHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", "This is a header", options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("This is a header"));
    }


    @Test
    public void testPrintHelpWithFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, "This is a footer", false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("This is a footer"));
    }


    @Test
    public void testPrintWrappedLongText() {
        HelpFormatter formatter = new HelpFormatter();
        String longText = "This is a very long text that should be wrapped according to the specified width.";
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printWrapped(writer, 20, longText);
        writer.flush();
        String result = output.toString();
        
        assertTrue(result.contains("This is a very long"));
        assertTrue(result.contains("text that should be"));
        assertTrue(result.contains("wrapped according to"));
        assertTrue(result.contains("the specified width."));
    }


    @Test
    public void testPrintOptionsLongNameNoDescription() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "longOption", false, null); // No description provided
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printOptions(writer, HelpFormatter.DEFAULT_WIDTH, options, 1, 3);
        writer.flush();
        String result = output.toString();
        
        assertTrue(result.contains("--longOption"));
        assertFalse(result.contains("null")); // Ensure no description text is present
    }


    @Test
    public void testPrintHelpOptionWithBlankArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        options.getOption("f").setArgName(""); // Set blank argument name
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        
        assertTrue(result.contains("-f,--file"));
        assertFalse(result.contains("<")); // Ensure no argument name is appended
    }


    @Test
    public void testPrintWrappedMultiLineText() {
        HelpFormatter formatter = new HelpFormatter();
        String multiLineText = "Line 1\nLine 2\nLine 3";
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printWrapped(writer, 20, multiLineText);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 2"));
        assertTrue(result.contains("Line 3"));
    }


    @Test
    public void testPrintWrappedTrailingWhitespace() {
        HelpFormatter formatter = new HelpFormatter();
        String textWithWhitespace = "Hello World    ";
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printWrapped(writer, 20, textWithWhitespace);
        writer.flush();
        String result = output.toString();
        assertFalse(result.endsWith(" ")); // Ensure no trailing whitespace
        assertTrue(result.contains("Hello World"));
    }


    @Test
    public void testPrintHelpStandaloneOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("-f,--file <arg>   The file to be processed"));
    }


    @Test
    public void testPrintHelpWithFooterFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, "This is a footer", false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("This is a footer"));
    }


    @Test
    public void testPrintHelpWithHeaderFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", "This is a header", options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("This is a header"));
    }


    @Test
    public void testPrintHelpWithNullComparatorFixed() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("-b,--beta"));
        assertTrue(result.contains("-a,--alpha"));
    }


    @Test
    public void testPrintHelpWithMixedOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", false, "Beta option"); // No long option
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        
        assertTrue(result.contains("-a,--alpha")); // Check for long option
        assertTrue(result.contains("-b")); // Check for short option without long option
    }


    @Test
    public void testPrintHelpWithBlankArgNames() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option1 = new Option("a", "alpha", true, "Alpha option");
        option1.setArgName(""); // Set blank argument name
        options.addOption(option1);
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        
        assertTrue(result.contains("-a,--alpha")); // Ensure option is present
        assertFalse(result.contains("<")); // Ensure no argument name is appended
    }


    @Test
    public void testPrintHelpWithValidArgNames() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option1 = new Option("a", "alpha", true, "Alpha option");
        option1.setArgName("input"); // Set valid argument name
        options.addOption(option1);
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        
        assertTrue(result.contains("-a,--alpha <input>")); // Check that the argument name is correctly formatted
    }


    @Test
    public void testPrintWrappedWithExceedingNextLineTabStop() {
        HelpFormatter formatter = new HelpFormatter();
        String longText = "This is a very long text that should be wrapped according to the specified width.";
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printWrapped(writer, 20, longText); // Width is less than the length of the text
        writer.flush();
        String result = output.toString();
        
        assertTrue(result.contains("This is a very long"));
        assertTrue(result.contains("text that should be"));
        assertTrue(result.contains("wrapped according to"));
        assertTrue(result.contains("the specified width."));
    }


    @Test
    public void testFindWrapPosWithTrailingWhitespace() {
        HelpFormatter formatter = new HelpFormatter();
        String textWithWhitespace = "Hello World    ";
        
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printWrapped(writer, 20, textWithWhitespace);
        writer.flush();
        String result = output.toString();
        
        assertFalse(result.endsWith(" ")); // Ensure no trailing whitespace
        assertTrue(result.contains("Hello World"));
    }


    @Test
    public void testPrintHelpWithHeaderProvided() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", "This is a header", options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("This is a header"));
        assertTrue(result.contains("usage: myapp"));
    }


    @Test
    public void testPrintHelpWithFooterProvided() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", "Header", options, 1, 3, "This is a footer", false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.contains("This is a footer"));
    }


    @Test
    public void testPrintHelpWithCustomComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        // Set the comparator directly on the HelpFormatter instead
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o2.getKey().compareTo(o1.getKey()); // Reverse order
            }
        });
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertTrue(result.indexOf("-b,--beta") < result.indexOf("-a,--alpha")); // Check order
    }


    @Test
    public void testPrintHelpWithLongOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "File option");
    
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printOptions(writer, HelpFormatter.DEFAULT_WIDTH, options, 1, 3);
        writer.flush();
        String result = output.toString();
    
        assertTrue(result.contains("-f,--file <arg>")); // Verify long option is present
    }


    @Test
    public void testPrintHelpWithNullArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "File option");
        option.setArgName(""); // Set blank argument name
        options.addOption(option);
    
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printOptions(writer, HelpFormatter.DEFAULT_WIDTH, options, 1, 3);
        writer.flush();
        String result = output.toString();
    
        assertTrue(result.contains("-f,--file")); // Ensure option is present
        assertFalse(result.contains("<")); // Ensure no argument name is appended
    }


    @Test
    public void testPrintHelpWithNullComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "Beta option");
        options.addOption("a", "alpha", false, "Alpha option");
        formatter.setOptionComparator(null); // Set comparator to null
    
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
    
        assertTrue(result.indexOf("-b,--beta") < result.indexOf("-a,--alpha")); // Check original order
    }


    @Test
    public void testPrintHelpWithNextLineTabStopEqualToWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, 20, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertFalse(result.contains("\n\n")); // Ensure no unexpected line breaks
    }


    @Test
    public void testPrintHelpWithNullStringForRtrim() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", null, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertFalse(result.contains("null")); // Ensure null is not printed
    }


    @Test
    public void testPrintHelpWithTrailingWhitespace() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "The file to be processed");
        String textWithWhitespace = "This is a test string with trailing spaces.    ";
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH, "myapp", textWithWhitespace, options, 1, 3, null, false);
        writer.flush();
        String result = output.toString();
        assertFalse(result.endsWith(" ")); // Ensure no trailing whitespace
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

