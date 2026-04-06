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
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.AmbiguousOptionException;

public class PosixParserTest {


    @Test
    public void testFlattenWithValidOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-f", "bar"}, result);
    }


    @Test
    public void testFlattenWithInvalidOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = {"--invalid", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "--invalid", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithLongOptionWithArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = {"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
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
    public void testBurstTokenWithArguments() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = {"-fbar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-f", "bar"}, result);
    }


    @Test
    public void testFlattenWithInvalidOptionAndStopAtNonOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"--invalid", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "--invalid", "arg1", "arg2"}, result);
    }


    @Test
    public void testInvalidOptionWithStopAtNonOption() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"--invalid", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "--invalid", "arg1", "arg2"}, result);
    }


    @Test
    public void testValidOptionFollowedByArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-f", "bar"}, result);
    }


    @Test
    public void testProcessNonOptionTokenWithStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-f", "arg1", "arg2"}, result);
    }


    @Test
    public void testNonMatchingTokenWhenStopAtNonOptionFalse() throws ParseException {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"--unknown"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--unknown"}, result);
    }


    @Test(expected = AmbiguousOptionException.class)
    public void testAmbiguousLongOptions() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");
        options.addOption("fo", "foobar", false, "foobar option");
        PosixParser parser = new PosixParser();
        String[] args = {"--fo"};
        parser.flatten(options, args, false);
    }


    @Test
    public void testNonOptionTokenWithStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        PosixParser parser = new PosixParser();
        String[] args = {"-f", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-f", "arg1", "arg2"}, result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

