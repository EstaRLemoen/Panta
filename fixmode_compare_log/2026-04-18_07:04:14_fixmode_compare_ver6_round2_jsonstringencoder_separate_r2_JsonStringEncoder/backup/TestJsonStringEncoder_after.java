package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import java.io.UnsupportedEncodingException;

public class TestJsonStringEncoder {


    @Test
    public void testEncodeAsUTF8WithSimpleString() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Simple ASCII string";
        byte[] expected = input.getBytes(); // direct conversion for ASCII
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "";
        char[] expected = new char[] {}; // expected output for empty input
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "";
        byte[] expected = new byte[] {}; // expected output for empty input
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a long string that should exceed the initial buffer size to test resizing functionality.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithEscapedCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\n and a tab\t character.";
        char[] expected = new char[] {
            'T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 't', 'e', 's', 't', ' ', 
            's', 't', 'r', 'i', 'n', 'g', ' ', 'w', 'i', 't', 'h', ' ', 'a', ' ', 
            'n', 'e', 'w', 'l', 'i', 'n', 'e', '\\', 'n', ' ', 'a', 'n', 'd', ' ', 
            'a', ' ', 't', 'a', 'b', '\\', 't', ' ', 'c', 'h', 'a', 'r', 'a', 'c', 't', 'e', 'r', '.', 
        };
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testEncodeAsUTF8WithLongString() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("This is a long string that should exceed the initial buffer size. ");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsUTF8WithNonASCIICharacters() throws java.io.UnsupportedEncodingException {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test with emoji: 😊 and accented letters: é, ñ.";
        byte[] expected = input.getBytes("UTF-8");
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithLongStringAndMultipleBytes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("This is a long string with emoji: 😊 and accented letters: é, ñ. ");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithLongInputAndEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("This is a long string with a newline\n and a tab\t character. ");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsUTF8WithNullByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Test string to trigger ByteArrayBuilder initialization.";
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsUTF8WithTwoByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "€"; // Euro sign, which requires 2 bytes in UTF-8
        byte[] expected = new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xAC}; // Expected UTF-8 bytes for Euro sign
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsUTF8WithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("This is a test string with a Euro sign: € ");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This string contains a newline\n and a tab\t character.";
        char[] expected = new char[] {
            'T', 'h', 'i', 's', ' ', 's', 't', 'r', 'i', 'n', 'g', ' ', 'c', 'o', 'n', 't', 'a', 'i', 'n', 's', ' ', 
            'a', ' ', 'n', 'e', 'w', 'l', 'i', 'n', 'e', '\\', 'n', ' ', 'a', 'n', 'd', ' ', 'a', ' ', 't', 'a', 'b', 
            '\\', 't', ' ', 'c', 'h', 'a', 'r', 'a', 'c', 't', 'e', 'r', '.'
        };
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithDifferentIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithSpecialCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Special characters: \n, \t, \\"; // Includes newline, tab, and backslash
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsStringWithNullTextBuffer() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance(); // Use the static method to get an instance
        String input = "Test string to trigger TextBuffer initialization.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testEncodeAsUTF8WithNonASCIICharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Non-ASCII characters: é, 中, 😊"; // Includes accented and non-Latin characters
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
    }


    @Test
    public void testQuoteAsUTF8WithCharacterExceeding0xFF() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Character: 𝄞"; // Musical symbol G clef, which exceeds 0xFF
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0); // Ensure output is not empty
        // Additional assertions can be added to verify the correct UTF-8 encoding
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithBrokenSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

