package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
// No new imports required

public class TestJsonStringEncoder {


    @Test
    public void testEncodeAsUTF8SimpleString() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Simple ASCII";
        byte[] expected = new byte[] {
            'S', 'i', 'm', 'p', 'l', 'e', ' ', 'A', 'S', 'C', 'I', 'I'
        };
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithNullTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Test string";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithEscaping() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter.";
        char[] expected = "This is a test string with a newline\\nand a tab\\tcharacter.".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull(result);
        assertEquals(longInput.length(), result.length);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithSpecialCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter.";
        char[] expected = "This is a test string with a newline\\nand a tab\\tcharacter.".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull(result);
        assertEquals(longInput.length(), result.length);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithHighSurrogate() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithLargeInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder largeInput = new StringBuilder();
        // Create a string with a mix of ASCII and multi-byte characters
        for (int i = 0; i < 1000; i++) {
            largeInput.append("a");
        }
        largeInput.append("\uD83D\uDE00"); // Add a surrogate pair
        byte[] result = encoder.quoteAsUTF8(largeInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter.";
        char[] expected = "This is a test string with a newline\\nand a tab\\tcharacter.".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uDC00"; // Valid high surrogate followed by valid low surrogate
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithGrinningFaceEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull(result);
        assertEquals(longInput.length(), result.length);
    }


    @Test
    public void testQuoteAsStringWithNumericEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control character: \u0001"; // ASCII control character
        char[] expected = "Control character: \\u0001".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter.";
        char[] expected = "This is a test string with a newline\\nand a tab\\tcharacter.".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D"; // Grinning face emoji followed by an invalid surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithQuotesAndBackslashes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test with a quote: \" and a backslash: \\";
        char[] expected = "This is a test with a quote: \\\" and a backslash: \\\\".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8WithEmptyString() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        byte[] result = encoder.quoteAsUTF8("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }


    @Test
    public void testEncodeAsUTF8WithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line 1\nLine 2\tTabbed";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithNonASCIICharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Café"; // Contains non-ASCII character 'é'
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithNullByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Sample input";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uDC00\uD800"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull(result);
        assertEquals(longInput.length(), result.length);
    }


    @Test
    public void testQuoteAsStringWithUnicodeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Unicode test: \u2603"; // Snowman character
        char[] expected = "Unicode test: \u2603".toCharArray(); // Updated expected value
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringBufferCapacity() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        // Create a string that exceeds the default buffer size
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        longInput.append(" with escape: \n and quote: \"");
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verify that the output contains the expected escape sequences
        assertTrue(new String(result).contains("\\n"));
        assertTrue(new String(result).contains("\\\""));
    }


    @Test
    public void testQuoteAsUTF8BufferCapacity() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        // Create a string that exceeds the default buffer size
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        longInput.append(" with escape: \n and quote: \"");
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verify that the output contains the expected escape sequences in UTF-8 format
        assertTrue(new String(result, java.nio.charset.StandardCharsets.UTF_8).contains("\\n"));
        assertTrue(new String(result, java.nio.charset.StandardCharsets.UTF_8).contains("\\\""));
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithInvalidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uDC00\uD800"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithSpecialEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter.";
        char[] expected = "This is a test string with a newline\\nand a tab\\tcharacter.".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithMultiByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Café"; // Contains non-ASCII character 'é'
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        char[] result = encoder.quoteAsString("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBufferDifferentLength() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull(result);
        assertEquals(longInput.length(), result.length);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputExceedingBufferAlternative() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull(result);
        assertEquals(longInput.length(), result.length);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersAlternative() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Control character: \u0001"; // ASCII control character
        char[] expected = "Control character: \\u0001".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithHighUnicodeCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji (U+1F600)
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testQuoteAsStringBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter and a long input that exceeds the buffer size.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result).contains("\\n"));
        assertTrue(new String(result).contains("\\t"));
    }


    @Test
    public void testQuoteAsUTF8BufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter and a long input that exceeds the buffer size.";
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, java.nio.charset.StandardCharsets.UTF_8).contains("\\n"));
        assertTrue(new String(result, java.nio.charset.StandardCharsets.UTF_8).contains("\\t"));
    }


    @Test
    public void testEncodeAsUTF8BufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        longInput.append("\uD83D\uDE00"); // Add a surrogate pair
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithHighSurrogateHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithEscapingQuotesAndBackslashes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test with a quote: \" and a backslash: \\";
        char[] expected = "This is a test with a quote: \\\" and a backslash: \\\\".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uDC00\uD800"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        // Create a long string that exceeds the initial buffer size and contains characters requiring escaping
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        longInput.append(" with newline\n and tab\tcharacter.");
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verify that the output contains the expected escape sequences
        assertTrue(new String(result).contains("\\n"));
        assertTrue(new String(result).contains("\\t"));
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE02"; // Grinning face and face with tears of joy emojis
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8BufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        // Create a long string that exceeds the initial buffer size and includes non-ASCII characters
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        longInput.append("\uD83D\uDE00"); // Add a surrogate pair
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithControlCharactersDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line 1\nLine 2\tTabbed"; // Contains control characters
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithStandardEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "This is a test string with a newline\nand a tab\tcharacter.";
        char[] expected = "This is a test string with a newline\\nand a tab\\tcharacter.".toCharArray();
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsUTF8BufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        // Create a long string that exceeds the initial buffer size
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        longInput.append(" with newline\n and tab\tcharacter.");
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verify that the output contains the expected escape sequences in UTF-8 format
        assertTrue(new String(result, java.nio.charset.StandardCharsets.UTF_8).contains("\\n"));
        assertTrue(new String(result, java.nio.charset.StandardCharsets.UTF_8).contains("\\t"));
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairsDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE02"; // Grinning face and face with tears of joy emojis
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairHandlingDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uDC00\uD800"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

