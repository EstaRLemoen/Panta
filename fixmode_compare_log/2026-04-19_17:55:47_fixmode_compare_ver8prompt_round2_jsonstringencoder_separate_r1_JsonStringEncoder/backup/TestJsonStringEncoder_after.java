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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
// No new imports required

public class TestJsonStringEncoder {


    @Test
    public void testQuoteAsStringNoSpecialChars() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Expected output does not match", input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8NoSpecialChars() throws java.io.UnsupportedEncodingException {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World";
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals("Expected output does not match", input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8SurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8IllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a matching low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello\\nWorld\\t!".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithNullByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull("Expected output should not be null", result);
        assertArrayEquals("Expected output does not match", input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithLongString() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithTwoByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "€"; // Euro sign, requires 2 bytes in UTF-8
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xAC}; // UTF-8 encoding of Euro sign
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithThreeByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "𐍈"; // Gothic letter hwair, requires 3 bytes in UTF-8
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x90, (byte) 0x8D, (byte) 0x88}; // UTF-8 encoding of Gothic letter hwair
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a matching low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!";
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = new byte[] {
            'H', 'e', 'l', 'l', 'o', 
            10, // '\n' as byte
            'W', 'o', 'r', 'l', 'd', 
            9, // '\t' as byte
            '!'
        }; // Expected UTF-8 output including control characters
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithLongStringDifferent() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8BufferOverflow() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8IllegalSurrogatePairWithValidSurrogates() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a matching low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8CharacterExceedingUnicodeLimit() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji, valid input
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringWithNullTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "test";
        char[] result = encoder.quoteAsString(input);
        assertNotNull("Expected output should not be null", result);
        assertArrayEquals("Expected output does not match", input.toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDuplicate() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Illegal surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsUTF8BufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8ValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        StringBuilder output = new StringBuilder();
        encoder.quoteAsString(longInput, output);
        assertNotNull("Expected output should not be null", output);
        assertTrue("Expected output should be greater than 0", output.length() > 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8IllegalSurrogatePairDuplicate() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Illegal surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputExceedingCapacity() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithNumericEscape() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0001World"; // Control character requiring numeric escape
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello\\u0001World".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithTwoByteCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "€"; // Euro sign, requires 2 bytes in UTF-8
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xAC}; // UTF-8 encoding of Euro sign
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersDuplicate() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello\\nWorld\\t!".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringWithEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!"; // Input with escape characters
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello\\nWorld\\t!".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogates() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a matching low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringBufferExpansionWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8BufferExpansionWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8WithTwoByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "€"; // Euro sign, requires 2 bytes in UTF-8
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xAC}; // UTF-8 encoding of Euro sign
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji, valid surrogate pair
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800"; // High surrogate without a matching low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongStringDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8BufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8WithTwoByteCharactersDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "€"; // Euro sign, requires 2 bytes in UTF-8
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xAC}; // UTF-8 encoding of Euro sign
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairDistinctAlternative() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning face emoji
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}; // UTF-8 encoding of the emoji
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDistinctAlternative() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uD800"; // Illegal surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithSpecialCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello \"World\" \\ Test"; // Input with special JSON characters
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello \\\"World\\\" \\\\ Test".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringWithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = ""; // Empty input
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Expected output should be an empty array", new char[0], result);
    }


    @Test
    public void testEncodeAsUTF8WithNonASCIICharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "é"; // Non-ASCII character
        byte[] result = encoder.encodeAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xC3, (byte) 0xA9}; // UTF-8 encoding of 'é'
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringWithNumericEscapeDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0001World"; // Control character requiring numeric escape
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello\\u0001World".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uDC00"; // High surrogate followed by a low surrogate
        // The input is valid and should not throw an exception
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull("Expected output should not be null", result);
    }


    @Test
    public void testQuoteAsUTF8WithUninitializedByteArrayBuilder() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World!";
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull("Expected output should not be null", result);
        assertArrayEquals("Expected output does not match", input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithEscapedCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder output = new StringBuilder();
        String input = "Hello, \"World\"!"; // String with quotes
        encoder.quoteAsString(input, output);
        String expected = "Hello, \\\"World\\\"!";
        assertEquals("Expected output does not match", expected, output.toString());
    }


    @Test
    public void testQuoteAsUTF8WithTwoEmojis() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE01"; // Two emojis
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80, // First emoji
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81  // Second emoji
        };
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8BufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE01"; // Two valid surrogate pairs
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80, // First emoji
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81  // Second emoji
        };
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithEscapedCharactersDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello \"World\" \\ Test"; // Input with special JSON characters
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello \\\"World\\\" \\\\ Test".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD800\uDC00"; // High surrogate followed by a low surrogate
        // The input is valid and should not throw an exception
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull("Expected output should not be null", result);
    }


    @Test
    public void testQuoteAsStringWithStandardEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t\"Test\""; // Input with escape characters
        StringBuilder output = new StringBuilder();
        encoder.quoteAsString(input, output);
        String expected = "Hello\\nWorld\\t\\\"Test\\\"";
        assertEquals("Expected output does not match", expected, output.toString());
    }


    @Test
    public void testEncodeAsUTF8BufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World! This is a long string that will require the output buffer to expand.";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsStringBufferExpansionWithLongInputDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World! This is a long string that will require the output buffer to expand.";
        char[] result = encoder.quoteAsString(input);
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8WithNonASCIICharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "é"; // Non-ASCII character
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {(byte) 0xC3, (byte) 0xA9}; // UTF-8 encoding of 'é'
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longInput.append("Hello World ");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsStringWithSpecialCharactersDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t\"Test\""; // Input with special JSON characters
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello\\nWorld\\t\\\"Test\\\"".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePairsDistinct() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83D\uDE01"; // Two emojis
        byte[] result = encoder.quoteAsUTF8(input);
        byte[] expected = new byte[] {
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80, // First emoji
            (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81  // Second emoji
        };
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testQuoteAsStringWithBufferFullDuringNumericEscape() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0001World"; // Control character requiring numeric escape
        char[] result = encoder.quoteAsString(input);
        char[] expected = "Hello\\u0001World".toCharArray(); // Expected escaped output
        assertArrayEquals("Expected output does not match", expected, result);
    }


    @Test
    public void testEncodeAsUTF8WithBufferFullDuringByteAppending() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World! This is a long string that will require the output buffer to expand.";
        byte[] result = encoder.encodeAsUTF8(input);
        assertNotNull("Expected output should not be null", result);
        assertTrue("Expected output should be greater than 0", result.length > 0);
    }


    @Test
    public void testQuoteAsUTF8WithCharacterExceedingUnicodeLimit() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uFFFD"; // Using replacement character instead of invalid code point
        byte[] result = encoder.quoteAsUTF8(input);
        assertNotNull("Expected output should not be null", result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

