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
import org.junit.Test;
import org.apache.commons.cli.ParseException;

public class PosixParserTest {


    @Test
    public void testFlattenSingleOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f"}, false);
        assertArrayEquals(new String[]{"-f"}, result);
    }


    @Test
    public void testFlattenMultipleOptions() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        options.addOption("b", "bar", false, "bar option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "-b"}, false);
        assertArrayEquals(new String[]{"-f", "-b"}, result);
    }


    @Test
    public void testFlattenLongOptionWithArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"--foo=bar"}, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }


    @Test
    public void testFlattenSingleAndDoubleHyphenTokens() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-", "--"}, false);
        assertArrayEquals(new String[]{"-", "--"}, result);
    }


    @Test
    public void testFlattenUnknownLongOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"--unknown"}, false);
        assertArrayEquals(new String[]{"--unknown"}, result);
    }


    @Test
    public void testFlattenOptionFollowedByNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption"}, false);
        assertArrayEquals(new String[]{"-f", "nonOption"}, result);
    }


    @Test
    public void testBurstTokenMultiCharacterOption() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ab"}, false);
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }


    @Test
    public void testFlattenStopAtNonOptionTrueStopsProcessing() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = new String[]{"-f", "nonOption", "-f"};
        String[] result = parser.flatten(options, args, true);
        // After nonOption, processing stops and remaining tokens are added as-is
        assertArrayEquals(new String[]{"-f", "--", "nonOption", "-f"}, result);
    }


    @Test
    public void testBurstTokenWithMultipleValidOptions() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        options.addOption("c", "charlie", false, "charlie option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }


    @Test
    public void testBurstTokenOptionWithArgumentAndTrailingChars() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option with argument");
        options.addOption("b", "beta", false, "beta option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-abfoo"}, false);
        // -a has argument, so 'bfoo' is treated as argument to -a, bursting stops after adding argument
        assertArrayEquals(new String[]{"-a", "bfoo"}, result);
    }


    @Test
    public void testFlattenInvalidOptionWithStopAtNonOptionFalse() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = new String[]{"-x", "value"};
        String[] result = parser.flatten(options, args, false);
        // -x is invalid but stopAtNonOption is false, so tokens include -x and value
        assertArrayEquals(new String[]{"-x", "value"}, result);
    }


    @Test
    public void testProcessNonOptionTokenWithStopAtNonOptionTrue() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption"}, true);
        assertArrayEquals(new String[]{"-f", "--", "nonOption"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptionsAndArguments() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-abvalue"}, false);
        assertArrayEquals(new String[]{"-a", "bvalue"}, result);
    }


    @Test
    public void testBurstTokenOptionWithArgument() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-avalue"}, false);
        assertArrayEquals(new String[]{"-a", "value"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptionsAndArgument() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-avalue"}, false);
        assertArrayEquals(new String[]{"-a", "value"}, result);
    }


    @Test
    public void testNonOptionTokenProcessingWithStopAtNonOptionFalse() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption1", "nonOption2"}, false);
        assertArrayEquals(new String[]{"-f", "nonOption1", "nonOption2"}, result);
    }


    @Test
    public void testInvalidOptionWithStopAtNonOptionTrue() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x", "value"}, true);
        assertArrayEquals(new String[]{"-x", "value"}, result);
    }


    @Test
    public void testProcessNonOptionTokenWithStopAtNonOptionTrueFixed() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption"}, true);
        assertArrayEquals(new String[]{"-f", "--", "nonOption"}, result);
    }


    @Test
    public void testNonOptionTokenAfterOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption"}, false);
        assertArrayEquals(new String[]{"-f", "nonOption"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptions() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ab"}, false);
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }


    @Test
    public void testValidOptionWithArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "value"}, false);
        assertArrayEquals(new String[]{"-f", "value"}, result);
    }


    @Test
    public void testNonOptionTokenProcessingWithStopAtNonOptionTrue() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption"}, true);
        assertArrayEquals(new String[]{"-f", "--", "nonOption"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptionAndArgument() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-avalue"}, false);
        assertArrayEquals(new String[]{"-a", "value"}, result);
    }


    @Test
    public void testUnmatchedTokenWithStopAtNonOptionFalse() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x"}, false);
        assertArrayEquals(new String[]{"-x"}, result);
    }


    @Test
    public void testValidOptionWithoutArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f"}, false);
        assertArrayEquals(new String[]{"-f"}, result);
    }


    @Test
    public void testStopAtNonOptionWithNoArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption"}, true);
        assertArrayEquals(new String[]{"-f", "--", "nonOption"}, result);
    }


    @Test
    public void testNonOptionTokenAfterOptionFixed() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonOption"}, false);
        assertArrayEquals(new String[]{"-f", "nonOption"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptionsFixed() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        options.addOption("c", "charlie", false, "charlie option");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

