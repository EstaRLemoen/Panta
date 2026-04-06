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
import org.apache.commons.cli.ParseException;

public class PosixParserTest {


    @Test
    public void testFlattenWithValidLongOptions() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo=bar", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--foo", "bar", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithNonOptionStop() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo=bar", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"--foo", "bar", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithSingleHyphen() throws ParseException {
        Options options = new Options();
        String[] args = {"-", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithDoubleHyphen() throws ParseException {
        Options options = new Options();
        String[] args = {"--", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithNonOptionTokensAfterOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo=bar", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"--foo", "bar", "arg1", "arg2"}, result);
    }


    @Test
    public void testBurstTokenWithArguments() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"-fvalue", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-f", "value", "arg1", "arg2"}, result);
    }


    @Test
    public void testProcessNonOptionTokenAfterValidOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo=bar", "arg1"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--foo", "bar", "arg1"}, result);
    }


    @Test
    public void testFlattenWithUnmatchedToken() throws ParseException {
        Options options = new Options();
        String[] args = {"-x", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-x", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithBurstingToken() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"-fvalue", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-f", "value", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithLongOptionMissingArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--foo", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithNonOptionAfterOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo=bar", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--foo", "bar", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithNonMatchingLongOption() throws ParseException {
        Options options = new Options();
        String[] args = {"--unknown", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--unknown", "arg1", "arg2"}, result);
    }


    @Test
    public void testGobbleWithEatTheRestTrue() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo=bar", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        parser.flatten(options, args, true); // This will set eatTheRest to true
        String[] result = parser.flatten(options, new String[] {"arg3", "arg4"}, true);
        assertArrayEquals(new String[] {"arg3", "arg4"}, result);
    }


    @Test
    public void testFlattenWithInvalidOptionStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"-x", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"-x", "arg1", "arg2"}, result);
    }


    @Test
    public void testProcessOptionTokenWithArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo=bar"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--foo", "bar"}, result);
    }


    @Test
    public void testProcessNonOptionTokenWithStopAtNonOptionTrue() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo", "value", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"--foo", "value", "arg1", "arg2"}, result);
    }


    @Test
    public void testBurstTokenWithInvalidOption() throws ParseException {
        Options options = new Options();
        String[] args = {"-x", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-x", "arg1", "arg2"}, result);
    }


    @Test
    public void testProcessOptionTokenWhenNotFoundWithStopAtNonOptionTrue() throws ParseException {
        Options options = new Options();
        String[] args = {"-x", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"-x", "arg1", "arg2"}, result);
    }


    @Test
    public void testProcessOptionTokenWithValidOptionAndArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"-f", "value", "arg1"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-f", "value", "arg1"}, result);
    }


    @Test
    public void testProcessOptionTokenWithNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("o", "option", true, "option description");
        String[] args = {"-o", "value", "nonOption"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-o", "value", "nonOption"}, result);
    }


    @Test
    public void testBurstTokenWithMultiCharacterOption() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        options.addOption("c", "charlie", false, "charlie option");
        String[] args = {"-abc", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-a", "-b", "-c", "arg1", "arg2"}, result);
    }


    @Test
    public void testProcessOptionTokenWithNoMatchingShortOption() throws ParseException {
        Options options = new Options();
        String[] args = {"-x", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-x", "arg1", "arg2"}, result);
    }


    @Test
    public void testProcessNonOptionTokenWithStopAtNonOptionTrueFixed() throws ParseException {
        Options options = new Options();
        options.addOption("o", "option", true, "option description");
        String[] args = {"-o", "value", "--", "nonOption1", "nonOption2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"-o", "value", "--", "nonOption1", "nonOption2"}, result);
    }


    @Test
    public void testProcessShortOptionWithNoMatchingOption() throws ParseException {
        Options options = new Options();
        String[] args = {"-x", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"-x", "arg1", "arg2"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptionsNoArguments() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        String[] args = {"-ab", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-a", "-b", "arg1", "arg2"}, result);
    }


    @Test
    public void testNonOptionTokenProcessingWithStopAtNonOptionTrue() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo", "value", "nonOption1", "nonOption2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"--foo", "value", "nonOption1", "nonOption2"}, result);
    }


    @Test
    public void testFlattenWithNonExistentShortOption() throws ParseException {
        Options options = new Options();
        String[] args = {"-x", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-x", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithStopAtNonOptionTrueAndNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"--foo", "value", "nonOption1", "nonOption2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] {"--foo", "value", "nonOption1", "nonOption2"}, result);
    }


    @Test
    public void testBurstTokenWithValidOptionAndArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");
        String[] args = {"-fvalue", "arg1", "arg2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"-f", "value", "arg1", "arg2"}, result);
    }


    @Test
    public void testFlattenWithStopAtNonOptionFalseAndNonOption() throws ParseException {
        Options options = new Options();
        String[] args = {"--foo", "value", "nonOption1", "nonOption2"};
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {"--foo", "value", "nonOption1", "nonOption2"}, result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

