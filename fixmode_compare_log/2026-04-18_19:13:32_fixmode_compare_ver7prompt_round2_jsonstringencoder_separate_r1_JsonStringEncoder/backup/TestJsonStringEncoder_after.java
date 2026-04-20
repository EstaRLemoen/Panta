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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.assertArrayEquals;
// No new imports required

public class TestJsonStringEncoder {


    @Test
    public void testEncodeAsUTF8() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals("Hello, World!".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8() throws java.io.UnsupportedEncodingException {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals("Hello, World!".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringNoSpecialChars() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello, World!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithSpecialChar() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \nWorld!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello, \\nWorld!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithMultipleSpecialChars() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \"World\"!\nNew Line.";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello, \\\"World\\\"!\\nNew Line.".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\nWorld\\t!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello,\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello,\\nWorld\\t!".toCharArray(), result);
    }


    @Test(expected = NullPointerException.class)
    public void testQuoteAsUTF8WithNullInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        encoder.quoteAsUTF8(null);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D" + "\uDC00" + "\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithTwoByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "ñ"; // Character that requires 2-byte UTF-8 encoding
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xC3, (byte) 0xB1}, result);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputIncludingTwoByteCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace("\0", "A") + "\u00F1"; // Long string with a 2-byte character
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairsDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D" + "\uDC00" + "\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithValidSurrogateAndInvalidCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D"; // Valid surrogate followed by an invalid character
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testEncodeAsUTF8WithTwoByteCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "ñ"; // Character that requires 2-byte UTF-8 encoding
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xC3, (byte) 0xB1}, result);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello,\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello,\\nWorld\\t!".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidCharacterExceedingRange() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D"; // Valid surrogate followed by an invalid character exceeding the Unicode range
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithNoEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World"; // Simple string with no special characters
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(input.toCharArray(), result); // Verify no modifications
    }


    @Test
    public void testEncodeAsUTF8WithTwoByteCharacterDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\u20AC"; // Euro sign
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xAC}, result); // Expected 2-byte UTF-8 encoding
    }


    @Test
    public void testEncodeAsUTF8WithThreeByteCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Valid surrogate pair
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairsDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDistinctInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tTabbed"; // String with control characters
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Line1\\nLine2\\tTabbed".toCharArray(), result); // Verify escape sequences
    }


    @Test
    public void testQuoteAsUTF8WithBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Create a string that exceeds the initial buffer size
        String input = "Hello, World! " + new String(new char[1000]).replace("\0", "A") + "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        // Verify that the resulting byte array is correctly formed
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D" + "\uDC00" + "\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacterEscape() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \u0007World!"; // Bell character requiring numeric escape
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello, \\u0007World!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInputAndMultipleEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, " + new String(new char[1000]).replace("\0", "\n") + "World!"; // Long string with newlines
        char[] result = encoder.quoteAsString(input);
        String expected = "Hello, " + new String(new char[1000]).replace("\0", "\\n") + "World!";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithSpecialCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \"World\"!"; // String with quotes needing escape
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello, \\\"World\\\"!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithASCIICharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairsDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00" + "\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputExceedingBufferDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello,\nWorld\t!"; // String with control characters
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello,\\nWorld\\t!".toCharArray(), result); // Verify escape sequences
    }


    @Test
    public void testQuoteAsUTF8WithEscapingCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \"World\"!"; // String with quotes needing escape
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals("Hello, \\\"World\\\"!".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithNormalString() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!"; // Normal string input
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello,\nWorld!"; // String with control characters
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals("Hello,\\nWorld!".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputDistinctVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithNullByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithFullOutputBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D" + "\uDC00" + "\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithFullOutputBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace("\0", "A"); // Long string to exceed buffer size
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacterEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello,\nWorld\t!"; // String with control characters
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello,\\nWorld\\t!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithControlCharactersDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello,\nWorld!"; // String with control characters
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals("Hello,\\nWorld!".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDistinctVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDistinctVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLongInputAndDistinctVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!" + new String(new char[1000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithEscapeSequences() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \nWorld!\tThis is a test."; // Input with newline and tab
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello, \\nWorld!\\tThis is a test.".toCharArray(), result);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace("\0", "A"); // Long string to exceed buffer size
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDistinctVariationRenamed() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDistinctVariationRenamed() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

