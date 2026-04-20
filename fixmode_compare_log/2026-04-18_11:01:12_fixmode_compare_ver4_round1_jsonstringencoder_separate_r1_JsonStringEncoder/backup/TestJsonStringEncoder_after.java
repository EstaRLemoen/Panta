package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
// No new imports required

public class TestJsonStringEncoder {


    @Test
    public void testEncodeAsUTF8() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Simple text.";
        byte[] expected = new byte[] {
            83, 105, 109, 112, 108, 101, 32, 116, 101, 120, 116, 46
        };
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(expected, result);
    }


    @Test
    public void testQuoteAsStringEmpty() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "";
        char[] expected = new char[] {};
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8InvalidSurrogate() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringInitializesTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Ensure _text is null initially by creating new instance
        // Provide a simple string without escapes to trigger text buffer initialization branch
        String input = "abc";
        char[] result = encoder.quoteAsString(input);
        // Result should match input chars as no escaping needed
        assertArrayEquals(new char[] {'a', 'b', 'c'}, result);
    }


    @Test
    public void testQuoteAsStringEscapingCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Input includes quote and backslash which require escaping
        String input = "He said \"Hello\\World\"";
        char[] result = encoder.quoteAsString(input);
        String expected = "He said \\\"Hello\\\\World\\\"";
        assertEquals(expected, new String(result));
    }


    @Test
    public void testQuoteAsStringBufferOverflowHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Create a long string to exceed initial buffer segment size
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String input = sb.toString();
        char[] result = encoder.quoteAsString(input);
        // Result should be same length and content as input (no escapes)
        assertEquals(input.length(), result.length);
        assertEquals(input, new String(result));
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Use a string with a valid surrogate pair (e.g. emoji U+1F600)
        String input = "Smile \uD83D\uDE00";
        byte[] result = encoder.quoteAsUTF8(input);
        // The UTF-8 encoding of U+1F600 is F0 9F 98 80
        // Check that the output contains these bytes at the end
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80};
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8IllegalSurrogatePairThrows() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Construct a string with an illegal surrogate pair: high surrogate followed by invalid low surrogate
        char highSurrogate = '\uD800'; // valid high surrogate
        char invalidLowSurrogate = 'a'; // invalid low surrogate (not in low surrogate range)
        String input = new String(new char[] {highSurrogate, invalidLowSurrogate});
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithEscapedCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tTabbed\"Quote\"";
        char[] result = encoder.quoteAsString(input);
        String expected = "Line1\\nLine2\\tTabbed\\\"Quote\\\"";
        assertEquals(expected, new String(result));
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Surrogate pair: \uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80}; // UTF-8 encoding of U+1F600
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }


    @Test
    public void testQuoteAsStringWithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals(new char[] {}, result);
    }


    @Test
    public void testEncodeAsUTF8BufferOverflow() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Create a long string to exceed initial buffer segment size
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String input = sb.toString();
        byte[] result = encoder.encodeAsUTF8(input);
        // Result should match the expected length and content
        assertEquals(input.length(), result.length);
        for (int i = 0; i < input.length(); i++) {
            assertEquals((byte) input.charAt(i), result[i]);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8IllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Construct a string with an illegal surrogate pair
        String input = "\uD800\uD800"; // Two high surrogates
        encoder.quoteAsUTF8(input);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8IllegalCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Use a string with a character above the valid Unicode range
        String input = "\uD83C\uFFFF"; // Invalid character
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8BufferOverflowWithMixedCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Create a long string with a mix of ASCII and non-ASCII characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a"); // ASCII
        }
        sb.append("\uD83D\uDE00"); // Add a surrogate pair (grinning face emoji)
        String input = sb.toString();
        byte[] result = encoder.encodeAsUTF8(input);
        // Check that the result is not null and has the expected length
        assertNotNull(result);
        assertTrue(result.length > 1000); // Expecting more than 1000 bytes due to emoji
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Use a string with a valid surrogate pair (e.g. emoji U+1F600)
        String input = "Smile \uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.encodeAsUTF8(input);
        // The UTF-8 encoding of U+1F600 is F0 9F 98 80
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80};
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8IllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Construct a string with an illegal surrogate pair: high surrogate without a low surrogate
        String input = "\uD800"; // High surrogate
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Use a string that includes control characters like newline (\n) and tab (\t)
        String input = "Line1\nLine2\tTabbed\"Quote\"";
        char[] result = encoder.quoteAsString(input);
        String expected = "Line1\\nLine2\\tTabbed\\\"Quote\\\"";
        assertEquals(expected, new String(result));
    }


    @Test
    public void testQuoteAsStringWithNonEmptyInputInitializesTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        // Verify that the result matches the input as no escaping is needed
        assertArrayEquals(input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringHandlesEscapeSequences() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        String expected = "Hello\\nWorld\\t!";
        assertEquals(expected, new String(result));
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Smile \uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80};
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8IllegalSurrogatePairDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Two high surrogates
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithNonASCIICharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Emoji: \uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80};
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Surrogate pair: \uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80};
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }


    @Test
    public void testQuoteAsUTF8BufferOverflow() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Create a long string to exceed initial buffer segment size
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String input = sb.toString();
        byte[] result = encoder.quoteAsUTF8(input);
        // Result should match the expected length and content
        assertEquals(input.length(), result.length);
        for (int i = 0; i < input.length(); i++) {
            assertEquals((byte) input.charAt(i), result[i]);
        }
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Line1\nLine2\tTabbed\"Quote\"";
        char[] result = encoder.quoteAsString(input);
        String expected = "Line1\\nLine2\\tTabbed\\\"Quote\\\"";
        assertEquals(expected, new String(result));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8InvalidCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Use a string with a character above the valid Unicode range
        String input = "\uD83C\uFFFF"; // Invalid character
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithMultiByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Emoji: \uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80};
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }


    @Test
    public void testQuoteAsUTF8BufferOverflowWithDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Create a long string to exceed initial buffer segment size
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String input = sb.toString();
        byte[] result = encoder.quoteAsUTF8(input);
        // Result should match the expected length and content
        assertEquals(input.length(), result.length);
        for (int i = 0; i < input.length(); i++) {
            assertEquals((byte) input.charAt(i), result[i]);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8IllegalSurrogatePairWithDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Construct a string with an illegal surrogate pair
        String input = "\uD800\uD800"; // Two high surrogates
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8BufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Create a long string with a mix of ASCII and non-ASCII characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a"); // ASCII
        }
        sb.append("\uD83D\uDE00"); // Add a surrogate pair (grinning face emoji)
        String input = sb.toString();
        byte[] result = encoder.encodeAsUTF8(input);
        // Check that the result is not null and has the expected length
        assertNotNull(result);
        assertTrue(result.length > 1000); // Expecting more than 1000 bytes due to emoji
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8IllegalSurrogatePairHandling() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Construct a string with an illegal surrogate pair: high surrogate without a low surrogate
        String input = "\uD800"; // High surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringInitializesTextBufferWithDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        char[] result = encoder.quoteAsString(input);
        // Verify that the result matches the input as no escaping is needed
        assertArrayEquals(input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringBufferResizing() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Provide a long input string with regular characters and escape sequences
        String input = "This is a long string with an escape sequence: \" and a newline \n and a tab \t.";
        char[] result = encoder.quoteAsString(input);
        String expected = "This is a long string with an escape sequence: \\\" and a newline \\n and a tab \\t.";
        assertEquals(expected, new String(result));
    }


    @Test
    public void testEncodeAsUTF8WithNullByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Call encodeAsUTF8 with a normal string input
        String input = "Normal string input.";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull(result);
        assertEquals(input.length(), result.length);
        for (int i = 0; i < input.length(); i++) {
            assertEquals((byte) input.charAt(i), result[i]);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Use a string that includes an invalid surrogate pair
        String input = "\uD800\uD800"; // Two high surrogates
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairEmoji() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        // Provide a string that includes valid surrogate pairs (e.g., emoji)
        String input = "Surrogate pair: \uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expectedSuffix = new byte[] {(byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80}; // UTF-8 encoding of U+1F600
        assertTrue(result.length >= expectedSuffix.length);
        for (int i = 0; i < expectedSuffix.length; i++) {
            assertEquals(expectedSuffix[i], result[result.length - expectedSuffix.length + i]);
        }
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

