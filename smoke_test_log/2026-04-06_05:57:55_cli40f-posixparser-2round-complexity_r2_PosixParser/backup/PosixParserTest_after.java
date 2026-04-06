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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public class PosixParserTest {


    @Test
    public void testFlattenWithValidOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-f", "bar"}, result);
    }


    @Test
    public void testFlattenWithNonOptionArgument() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz"}, result);
    }


    @Test
    public void testFlattenWithSingleAndDoubleHyphenTokens() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-", "--"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-", "--"}, result);
    }


    @Test
    public void testFlattenWithNonOptionTokenWhenStopAtNonOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz"}, result);
    }


    @Test
    public void testFlattenWithMultiCharacterOptionToken() throws ParseException {
        Options options = new Options();
        Option optionA = new Option("a", "alpha", false, "alpha option");
        Option optionB = new Option("b", "beta", true, "beta option");
        options.addOption(optionA);
        options.addOption(optionB);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-ab", "value"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }


    @Test
    public void testFlattenWithNonMatchingLongOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"--unknown"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"--unknown"}, result);
    }


    @Test
    public void testFlattenWithNonOptionTokenAndStopAtNonOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz", "qux"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz", "qux"}, result);
    }


    @Test
    public void testFlattenWithInvalidOptionToken() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-x", "value1", "value2"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-x", "value1", "value2"}, result);
    }


    @Test
    public void testFlattenWithLongOptionHavingArgument() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }


    @Test
    public void testProcessNonOptionTokenAfterOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz"}, result);
    }


    @Test
    public void testBurstTokenWithInvalidOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-x", "value1", "value2"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-x", "value1", "value2"}, result);
    }


    @Test
    public void testBurstTokenWithOptionHavingArgument() throws ParseException {
        Options options = new Options();
        Option option = new Option("o", "option", true, "option with argument");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-o", "value"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-o", "value"}, result);
    }


    @Test
    public void testFlattenWithTwoCharacterShortOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("a", "alpha", false, "alpha option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-a"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-a"}, result);
    }


    @Test
    public void testProcessNonOptionTokenWhenStopAtNonOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"input.txt"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"--", "input.txt"}, result);
    }


    @Test
    public void testFlattenWithLongOptionHavingArgumentFix() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }


    @Test
    public void testLongOptionWithArgument() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }


    @Test
    public void testNonOptionTokenProcessingWhenStopAtNonOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz", "qux"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz", "qux"}, result);
    }


    @Test
    public void testBurstingWithInvalidOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-x"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-x"}, result);
    }


    @Test
    public void testProcessNonOptionTokenAfterValidOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptionsAndArguments() throws ParseException {
        Options options = new Options();
        Option optionA = new Option("a", "alpha", false, "alpha option");
        Option optionB = new Option("b", "beta", true, "beta option");
        options.addOption(optionA);
        options.addOption(optionB);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-abvalue"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }


    @Test
    public void testNonOptionTokenWhenStopAtNonOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz"}, result);
    }


    @Test
    public void testNonOptionTokensWhenStopAtNonOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz", "qux"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz", "qux"}, result);
    }


    @Test
    public void testInvalidOptionWhenStopAtNonOptionFalse() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-x", "value1", "value2"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-x", "value1", "value2"}, result);
    }


    @Test
    public void testStopAtNonOptionWithNonOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz"}, result);
    }


    @Test
    public void testLongOptionWithArgumentFix() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }


    @Test
    public void testBurstTokenWithInvalidOptionFix() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-z"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-z"}, result);
    }


    @Test
    public void testInvalidOptionWhenStopAtNonOptionFalseFix() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-x", "value1", "value2"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-x", "value1", "value2"}, result);
    }


    @Test
    public void testNonOptionTokensAfterValidOption() throws ParseException {
        Options options = new Options();
        Option option = new Option("f", "foo", true, "foo option");
        options.addOption(option);
        
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar", "baz"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-f", "bar", "baz"}, result);
    }


    @Test
    public void testBurstingWithValidOptions() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option");
        
        PosixParser parser = new PosixParser();
        String[] args = {"-abvalue"};
        String[] result = parser.flatten(options, args, false);
        
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }


    @Test
    public void testNoMatchingOptionsWithStopAtNonOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"--unknown"};
        String[] result = parser.flatten(options, args, true);
        
        assertArrayEquals(new String[]{"--", "--unknown"}, result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

