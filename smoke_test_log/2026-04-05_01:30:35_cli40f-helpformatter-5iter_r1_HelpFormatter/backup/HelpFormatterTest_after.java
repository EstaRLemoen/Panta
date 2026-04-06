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


    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpWithNullCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(null, new Options());
    }


    @Test
    public void testPrintOptionsWithBlankArgumentName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("o", "Option with blank arg", true, "Description");
        option.setArgName(""); // Set blank argument name
        options.addOption(option);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printOptions(printWriter, 74, options, 1, 3);
        String output = stringWriter.toString();
        assertTrue(output.contains("-o"));
        assertFalse(output.contains("<>")); // Ensure no argument name is shown
    }


    @Test
    public void testPrintHelpWithAutoUsageTrue() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", null, options, 1, 3, null, true);
        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp"));
    }


    @Test
    public void testPrintHelpWithFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", "This is a header", options, 1, 3, "This is a footer");
        String output = stringWriter.toString();
        assertTrue(output.contains("This is a footer"));
    }


    @Test
    public void testPrintHelpWithUnsortedOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option1 = new Option("b", "optionB", false, "Option B");
        Option option2 = new Option("a", "optionA", false, "Option A");
        options.addOption(option1);
        options.addOption(option2);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", null, options, 1, 3, null);
        String output = stringWriter.toString();
        assertTrue(output.indexOf("-a") < output.indexOf("-b")); // Ensure options are sorted
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpWithNullCmdLineSyntaxFixed() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(null, new Options());
    }


    @Test
    public void testPrintHelpWithHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", "This is a header", options, 1, 3, null);
        String output = stringWriter.toString();
        assertTrue(output.contains("This is a header"));
    }


    @Test
    public void testPrintHelpWithFooterNonEmpty() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", "This is a header", options, 1, 3, "This is a footer");
        String output = stringWriter.toString();
        assertTrue(output.contains("This is a footer"));
    }


    @Test
    public void testAppendOptionWithBlankArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("o", "optionWithBlankArg", true, "Description");
        option.setArgName(""); // Set blank argument name
        options.addOption(option);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", null, options, 1, 3, null);
        String output = stringWriter.toString();
        assertTrue(output.contains("-o"));
        assertFalse(output.contains("<>")); // Ensure no argument name is shown
    }


    @Test
    public void testPrintHelpWithNonEmptyHeader() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", "This is a header", options, 1, 3, null);
        String output = stringWriter.toString();
        assertTrue(output.contains("This is a header"));
    }


    @Test
    public void testPrintHelpWithNonEmptyFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", null, options, 1, 3, "This is a footer");
        String output = stringWriter.toString();
        assertTrue(output.contains("This is a footer"));
    }


    @Test
    public void testPrintHelpWithCustomComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option1 = new Option("b", "optionB", false, "Option B");
        Option option2 = new Option("a", "optionA", false, "Option A");
        options.addOption(option1);
        options.addOption(option2);
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o1.getOpt().compareTo(o2.getOpt());
            }
        });
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", null, options, 1, 3, null);
        String output = stringWriter.toString();
        assertTrue(output.indexOf("-a") < output.indexOf("-b")); // Ensure options are sorted
    }


    @Test
    public void testPrintHelpWithOptionHavingBlankArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("o", "optionWithBlankArg", true, "Description");
        option.setArgName(""); // Set blank argument name
        options.addOption(option);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        formatter.printHelp(printWriter, 74, "myapp", null, options, 1, 3, null);
        String output = stringWriter.toString();
        assertTrue(output.contains("-o"));
        assertFalse(output.contains("<>")); // Ensure no argument name is shown
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

