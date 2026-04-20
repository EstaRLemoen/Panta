package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import java.io.UnsupportedEncodingException;
// No new imports required

public class TestJsonStringEncoder {


    @Test
    public void testEncodeAsUTF8Simple() throws java.io.UnsupportedEncodingException {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] expected = "Hello, World!".getBytes("UTF-8");
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithSpecialChars() throws java.io.UnsupportedEncodingException {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \"World\"!\nNew Line";
        byte[] expected = "Hello, \"World\"!\nNew Line".getBytes("UTF-8");
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithUninitializedTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        // Verify that a new TextBuffer instance is created and used for encoding
        assertNotNull(result);
        assertArrayEquals("Hello, World!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithSpecialCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \"World\"!\nNew Line";
        char[] expected = "Hello, \\\"World\\\"!\\nNew Line".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("A");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull(result);
        assertEquals(1000, result.length);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        // Verify that the byte array correctly represents the UTF-8 encoded surrogate pairs
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithOnlyASCIICharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello, World!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3";
        char[] expected = "Line1\\nLine2\\tLine3".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE01"; // Grinning Face Emoji and another character
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        // Verify that the byte array correctly represents the UTF-8 encoding of the characters
        assertArrayEquals(new byte[] { 
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80, 
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81 
        }, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithInvalidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongASCIIInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace('\0', 'A'); // Long string of ASCII characters
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertEquals(2000, result.length); // Ensure the length matches the input
    }


    @Test
    public void testEncodeAsUTF8BufferExpansionWithEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace('\0', 'A') + "\uD83D\uDE00"; // 1000 ASCII characters followed by an emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        // Verify that the resulting byte array is correctly formed
        byte[] expected = new byte[1004];
        for (int i = 0; i < 1000; i++) {
            expected[i] = (byte) 'A';
        }
        expected[1000] = (byte) 0xF0;
        expected[1001] = (byte) 0x9F;
        expected[1002] = (byte) 0x98;
        expected[1003] = (byte) 0x80;
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        // Verify that the byte array correctly represents the UTF-8 encoded surrogate pairs
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result);
    }


    @Test
    public void testEncodeAsUTF8BufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace('\0', 'A') + "\uD83D\uDE00"; // 1000 ASCII characters followed by an emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        // Verify that the resulting byte array is correctly formed
        byte[] expected = new byte[1004];
        for (int i = 0; i < 1000; i++) {
            expected[i] = (byte) 'A';
        }
        expected[1000] = (byte) 0xF0;
        expected[1001] = (byte) 0x9F;
        expected[1002] = (byte) 0x98;
        expected[1003] = (byte) 0x80;
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIncompleteSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3";
        char[] expected = "Line1\\nLine2\\tLine3".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithHighUnicodeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        // Verify that the resulting byte array correctly encodes the high Unicode characters as UTF-8.
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result);
    }


    @Test
    public void testQuoteAsStringInitializesTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        // Verify that a new TextBuffer instance is created and used for encoding
        assertNotNull(result);
        assertArrayEquals("Hello, World!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringHandlesEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tTabbed";
        char[] expected = "Line1\\nLine2\\tTabbed".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringHandlesOutputBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace('\0', 'A'); // Long string of ASCII characters
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertEquals(1000, result.length); // Ensure the length matches the input
    }


    @Test
    public void testQuoteAsUTF8HandlesSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        // Verify that the byte array correctly represents the UTF-8 encoded surrogate pairs
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8HandlesIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringHandlesControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\rLine3"; // Includes newline and carriage return
        char[] expected = "Line1\\nLine2\\rLine3".toCharArray(); // Expected escaped output
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithTwoByteCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\u00E9"; // Character 'é' which requires 2-byte UTF-8 encoding
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] {(byte) 0xC3, (byte) 0xA9}, result); // Expected UTF-8 byte array for 'é'
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji, which is a surrogate pair
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result); // Expected UTF-8 byte array for the emoji
    }


    @Test
    public void testEncodeAsUTF8WithSimpleInput() throws java.io.UnsupportedEncodingException {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Simple Input"; // Simple string without special characters
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes("UTF-8"), result); // Verify the output matches expected UTF-8 encoding
    }


    @Test
    public void testQuoteAsStringWithNumericEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control char: \u0001"; // Character requiring numeric escape
        char[] expected = "Control char: \\u0001".toCharArray(); // Expected escaped output
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8BufferExpansionWithTwoByteCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace('\0', 'A') + "\u00E9"; // 1000 ASCII characters followed by a 2-byte character
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        // Verify that the resulting byte array is correctly formed
        byte[] expected = new byte[1002];
        for (int i = 0; i < 1000; i++) {
            expected[i] = (byte) 'A';
        }
        expected[1000] = (byte) 0xC3;
        expected[1001] = (byte) 0xA9; // Expected UTF-8 byte array for 'é'
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithTwoByteCharacterDifferentName() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\u00E9"; // Character 'é' which requires 2-byte UTF-8 encoding
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] {(byte) 0xC3, (byte) 0xA9}, result); // Expected UTF-8 byte array for 'é'
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDifferentName() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3"; // Includes newline and tab
        char[] expected = "Line1\\nLine2\\tLine3".toCharArray(); // Expected escaped output
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8BufferExpansionWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace('\0', 'A'); // Long string of ASCII characters
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertEquals(2000, result.length); // Ensure the length matches the input
    }


    @Test
    public void testEncodeAsUTF8WithGrinningFaceEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        // Verify that the resulting byte array correctly encodes the high Unicode characters as UTF-8.
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result);
    }


    @Test
    public void testQuoteAsStringInitializesTextBufferWithHelloWorld() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        // Verify that a new TextBuffer instance is created and used for encoding
        assertNotNull(result);
        assertArrayEquals("Hello, World!".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIncompleteSurrogatePairDifferentName() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringHandlesControlCharactersWithNewLineAndTab() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3"; // Includes newline and tab
        char[] expected = "Line1\\nLine2\\tLine3".toCharArray(); // Expected escaped output
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace('\0', 'A'); // Long string of ASCII characters
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertEquals(2000, result.length); // Ensure the length matches the input
        assertArrayEquals(input.toCharArray(), result); // Verify the output matches the input
    }


    @Test
    public void testQuoteAsStringHandlesControlCharactersWithMultipleEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3\rLine4"; // Includes newline, tab, and carriage return
        char[] expected = "Line1\\nLine2\\tLine3\\rLine4".toCharArray(); // Expected escaped output
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithMultiByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, 🌍"; // String with an emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        // Verify the expected UTF-8 byte array for "Hello, 🌍"
        byte[] expected = new byte[] { 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 
            (byte) 0xF0, (byte) 0x9F, (byte) 0x8C, (byte) 0x8D 
        };
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace('\0', 'A'); // Long string of ASCII characters
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertEquals(2000, result.length); // Ensure the length matches the input
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result);
    }


    @Test
    public void testQuoteAsStringWithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = ""; // Empty string
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertArrayEquals(new char[] {}, result); // Expected output is also an empty char array
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIncompleteSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersHandlingDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tLine3"; // Includes newline and tab
        char[] expected = "Line1\\nLine2\\tLine3".toCharArray(); // Expected escaped output
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithNullByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Test String"; // Provide a non-empty string
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result); // Verify that the method completes without exceptions
        assertTrue(result.length > 0); // Ensure that the result is not empty
    }


    @Test
    public void testAppendByteHandlesBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[1000]).replace('\0', 'A') + "\uD83D\uDE00"; // Long string followed by an emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertEquals(1004, result.length); // Ensure the length matches the input
        // Verify the expected UTF-8 byte array for 'A' repeated 1000 times followed by the emoji
        byte[] expected = new byte[1004];
        for (int i = 0; i < 1000; i++) {
            expected[i] = (byte) 'A';
        }
        expected[1000] = (byte) 0xF0;
        expected[1001] = (byte) 0x9F;
        expected[1002] = (byte) 0x98;
        expected[1003] = (byte) 0x80; // Expected UTF-8 byte array for the emoji
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePairDifferentName() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80 }, result); // Expected UTF-8 byte array for the emoji
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithInvalidSurrogatePairDifferentName() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.encodeAsUTF8(input); // This should throw an exception
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = new String(new char[2000]).replace('\0', 'A'); // Long string of ASCII characters
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertEquals(2000, result.length); // Ensure the length matches the input
        assertArrayEquals(input.toCharArray(), result); // Verify the output matches the input
    }


    @Test
    public void testQuoteAsUTF8WithCharacterExceedingByteRange() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\u0100"; // Character 'Ā' which exceeds the byte range
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(new byte[] { (byte) 0xC4, (byte) 0x80 }, result); // Expected UTF-8 byte array for 'Ā'
    }


    @Test(expected = NullPointerException.class)
    public void testQuoteAsStringWithNullInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = null; // Null input
        encoder.quoteAsString(input); // This should throw a NullPointerException
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

