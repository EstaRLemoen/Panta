package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import java.nio.charset.StandardCharsets;
// No new imports required

public class TestJsonStringEncoder {


    @Test
    public void testEncodeAsUTF8WithSpecialChars() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Special chars: \u2022 \u2013 \u2014";
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals("Special chars: \u2022 \u2013 \u2014".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testEncodeAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        char[] result = encoder.quoteAsString("");
        assertArrayEquals(new char[0], result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Line1\\nLine2\\tLine3".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithMultiByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Emoji: \uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithASCIIInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string with multiple characters: \uD83D\uDE00 and more text.";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithNullByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Simple input";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputBufferOverflow() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to encode: \uD83D\uDE00";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputBufferOverflow() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to escape: \n\t";
        char[] result = encoder.quoteAsString(input);
        String expected = "A long string that exceeds the initial buffer size and requires multiple segments to escape: \\n\\t";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Line1\\nLine2\\tLine3".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithStandardEscapeSequences() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = "Line1\\nLine2\\tLine3".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control char: \u0001"; // ASCII control character
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] { 'C', 'o', 'n', 't', 'r', 'o', 'l', ' ', 'c', 'h', 'a', 'r', ':', ' ', '\\', 'u', '0', '0', '0', '1' };
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        byte[] result = encoder.quoteAsUTF8("");
        assertArrayEquals(new byte[0], result); // Expecting empty byte array for empty input
    }


    @Test
    public void testQuoteAsUTF8WithGrinningFaceEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = NullPointerException.class)
    public void testQuoteAsUTF8WithNullInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        encoder.quoteAsUTF8(null);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersEscaping() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control characters: \n\t\b\f\r";
        char[] result = encoder.quoteAsString(input);
        String expected = "Control characters: \\n\\t\\b\\f\\r";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInputBufferOverflowDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a long string that exceeds the initial buffer size and will require multiple segments to escape: \n\t";
        char[] result = encoder.quoteAsString(input);
        String expected = "This is a long string that exceeds the initial buffer size and will require multiple segments to escape: \\n\\t";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithMultiByteCharactersDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Multi-byte characters: \uD83D\uDE00 \uD83C\uDF1F"; // Grinning Face Emoji and Shooting Star
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDifferentEmojiAndShootingStar() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83C\uDF1F"; // Grinning Face Emoji and Shooting Star
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairDifferentEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputBufferOverflow() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to encode: " +
                       "This is a test string that will fill the buffer.";
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithTwoByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Two-byte: \u00E9"; // 'é' character
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithEscaping() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control characters: \n\t\b\f\r";
        char[] result = encoder.quoteAsString(input);
        String expected = "Control characters: \\n\\t\\b\\f\\r";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInputBufferOverflowDifferentBehavior() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to escape: " +
                       "This is a test string that will fill the buffer.";
        char[] result = encoder.quoteAsString(input);
        String expected = input.replace("\n", "\\n").replace("\t", "\\t");
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputBufferOverflowDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to encode: " +
                       "This is a test string that will fill the buffer.";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = NullPointerException.class)
    public void testQuoteAsStringWithNullInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        encoder.quoteAsString(null); // Expecting NullPointerException for null input
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDifferentBehavior() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control characters: \n\t";
        char[] result = encoder.quoteAsString(input);
        String expected = "Control characters: \\n\\t";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a long string with special characters: \n\t and more text.";
        char[] result = encoder.quoteAsString(input);
        String expected = "This is a long string with special characters: \\n\\t and more text.";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithControlCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Text with control character: \n";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = "Text with control character: \\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithGrinningFaceAndShootingStar() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83C\uDF1F"; // Grinning Face and Shooting Star
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputBufferOverflowDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A different long string that exceeds the initial buffer size and requires multiple segments to encode: " +
                       "This is a test string with special characters: \uD83D\uDE00 and more text.";
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputBufferOverflowDifferentBehaviorForSpecialChars() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A different long string that exceeds the initial buffer size and requires multiple segments to escape: " +
                       "This is a test string with special characters: \n\t and more text.";
        char[] result = encoder.quoteAsString(input);
        String expected = input.replace("\n", "\\n").replace("\t", "\\t");
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Emoji: \uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to encode: " +
                       "This is a test string with special characters: \uD83D\uDE00 and more text.";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDifferentEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = NullPointerException.class)
    public void testQuoteAsStringWithNullInputHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        encoder.quoteAsString(null); // Expecting NullPointerException for null input
    }


    @Test
    public void testQuoteAsStringWithControlCharactersEscapingHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Line1\\nLine2\\tLine3".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputBufferOverflowHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to escape: \n\t";
        char[] result = encoder.quoteAsString(input);
        String expected = "A long string that exceeds the initial buffer size and requires multiple segments to escape: \\n\\t";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairHandlingDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersNeedingNumericEscaping() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control characters: \u0000\u0001"; // Characters needing numeric escape
        char[] result = encoder.quoteAsString(input);
        String expected = "Control characters: \\u0000\\u0001"; // Expected output with numeric escapes
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithNullByteArrayBuilderInitialization() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Simple input"; // Simple input to trigger initialization
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithBufferResizing() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and includes special characters: \uD83D\uDE00";
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83C\uDF1F"; // Grinning Face and Shooting Star
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithHighSurrogate() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83C\uDF1F"; // Grinning Face and Shooting Star
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder output = new StringBuilder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to escape: " +
                       "This is a test string that will fill the buffer.";
        encoder.quoteAsString(input, output);
        String expected = input.replace("\n", "\\n").replace("\t", "\\t");
        assertEquals(expected, output.toString());
    }


    @Test
    public void testQuoteAsUTF8WithLongInputBufferExpansionWithSpecialChars() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size and requires multiple segments to encode: " +
                       "This is a test string that will fill the buffer with special characters: \uD83D\uDE00 and more text.";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairHandlingDifferentEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDifferentBehaviorV2() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control characters: \n\t\b\f\r";
        char[] result = encoder.quoteAsString(input);
        String expected = "Control characters: \\n\\t\\b\\f\\r";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePairDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83C\uDF1F"; // Grinning Face and Shooting Star
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIllegalSurrogatePairDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputBufferOverflowDifferentBehavior() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a long string that exceeds the initial buffer size and requires multiple segments to encode: \n\t" +
                       "Additional text to ensure overflow occurs.";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.replace("\n", "\\n").replace("\t", "\\t").getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputBufferOverflowDifferentBehaviorV2() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a long string that exceeds the initial buffer size and requires multiple segments to escape: \n\t" +
                       "Additional text to ensure overflow occurs.";
        char[] result = encoder.quoteAsString(input);
        String expected = "This is a long string that exceeds the initial buffer size and requires multiple segments to escape: \\n\\t" +
                          "Additional text to ensure overflow occurs.";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersEscapingDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control characters: \n\t\r"; // Different input
        char[] result = encoder.quoteAsString(input);
        String expected = "Control characters: \\n\\t\\r"; // Expected output with escape sequences
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testEncodeAsUTF8WithTwoByteCharactersDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Two-byte character: \u00E9"; // 'é' character with different context
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDifferentContext() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji with different context
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDifferentContext() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Invalid surrogate pair with different context
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a long string that exceeds the initial buffer size and requires multiple segments to encode: " +
                       "This is a test string that will fill the buffer with special characters: \uD83D\uDE00 and more text.";
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairsDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83C\uDF1F"; // Grinning Face and Shooting Star
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = input.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithBufferFullDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a long string that exceeds the initial buffer size and requires multiple segments to escape: " +
                       "This is a test string that will fill the buffer.";
        char[] result = encoder.quoteAsString(input);
        String expected = input.replace("\n", "\\n").replace("\t", "\\t");
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairHandlingDifferentContext() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

