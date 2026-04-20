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

public class TestJsonStringEncoder {


    @Test
    public void testEncodeAsUTF8WithSpecialChars() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, \"World\"!\nNew line.";
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals("Hello, \"World\"!\nNew line.".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithNullTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertArrayEquals("Hello, World!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a very long string that should exceed the initial buffer size for the output.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertArrayEquals(input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithNoEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertArrayEquals(input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tTabbed";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        // Expected output should contain escape sequences for newline and tab
        String expected = "Line1\\nLine2\\tTabbed";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithMultiByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\u20AC"; // Euro sign
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testEncodeAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE00"; // Two Grinning Face Emojis
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIllegalSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8BufferCapacity() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that will exceed the initial buffer size for UTF-8 encoding. " +
                       "This should trigger the buffer expansion logic.";
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that will exceed the initial buffer size for UTF-8 encoding. " +
                       "This should trigger the buffer expansion logic.";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersFixed() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tTabbed";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        // Expected output should contain escape sequences for newline and tab
        String expected = "Line1\\nLine2\\tTabbed";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInputFixed() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a very long string that should exceed the initial buffer size for the output.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertArrayEquals(input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test with a newline\nand a tab\tcharacter.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        String expected = "This is a test with a newline\\nand a tab\\tcharacter.";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInputForBufferResizing() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size for testing the resizing logic.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertArrayEquals(input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    // This test is a duplicate and has been removed.


    @Test
    public void testQuoteAsStringWithControlCharactersEscaped() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tTabbed";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        // Expected output should contain escape sequences for newline and tab
        String expected = "Line1\\nLine2\\tTabbed";
        assertArrayEquals(expected.toCharArray(), result);
    }


    @Test
    public void testEncodeAsUTF8WithUninitializedByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithHighSurrogateCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE01"; // Two Grinning Face Emojis
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "A long string that exceeds the initial buffer size for testing the resizing logic.";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

