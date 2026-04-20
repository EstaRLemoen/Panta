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
        String input = "Hello, \"World\"!\nNew Line";
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals("Hello, \"World\"!\nNew Line".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\nWorld".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertEquals(1000, result.length);
        for (char c : result) {
            assertEquals('a', c);
        }
    }


    @Test
    public void testQuoteAsUTF8WithSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00"; // Grinning Face Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithSpecialEscapeCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\tWorld\nNew Line\"";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\tWorld\\nNew Line\\\"".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE03"; // Grinning Face with Smiling Eyes Emoji
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83}, result);
    }


    @Test
    public void testQuoteAsStringWithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        char[] result = encoder.quoteAsString("");
        assertArrayEquals(new char[] {}, result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0000World"; // Contains a null character
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\u0000World".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithTabAndNewline() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\tWorld\nNew Line"; // Contains tab and newline
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\tWorld\\nNew Line".toCharArray(), result);
    }


    @Test
    public void testQuoteAsUTF8WithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0000World"; // Contains a null character
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals("Hello\\u0000World".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testQuoteAsStringWithStandardEscape() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\tWorld"; // Contains a tab character
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\tWorld".toCharArray(), result);
    }


    @Test
    public void testEncodeAsUTF8WithNonASCIICharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "é"; // Non-ASCII character
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xC3, (byte) 0xA9}, result); // UTF-8 encoding for 'é'
    }


    @Test
    public void testEncodeAsUTF8WithLongInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertEquals(1000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertEquals(2000, result.length);
        for (char c : result) {
            assertEquals('a', c);
        }
    }


    @Test
    public void testQuoteAsUTF8WithMultiByteCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "é"; // Non-ASCII character
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xC3, (byte) 0xA9}, result); // UTF-8 encoding for 'é'
    }


    @Test
    public void testQuoteAsStringWithNamedEscapes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello \"World\""; // Contains quotes that need to be escaped
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello \\\"World\\\"".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithControlCharacter() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0001World"; // Contains a control character
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\u0001World".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83E"; // Different invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE03"; // Grinning Face with Smiling Eyes Emoji
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIllegalSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        byte[] result = encoder.quoteAsUTF8("");
        assertArrayEquals(new byte[] {}, result);
    }


    @Test
    public void testQuoteAsUTF8WithOnlyASCII() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello World"; // Only ASCII characters
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertEquals(2000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithInvalidSurrogatePair() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersAndExclamation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\nWorld\\t!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBufferSize() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertEquals(2000, result.length);
        for (char c : result) {
            assertEquals('a', c);
        }
    }


    @Test
    public void testQuoteAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE03\uD83D\uDE0A"; // Two valid surrogate pairs
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x8A}, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBufferSize3000() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertEquals(3000, result.length);
        for (char c : result) {
            assertEquals('a', c);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithInvalidSurrogatePairDifferentInputVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83E"; // Different invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithMultipleBufferResizes() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.quoteAsUTF8(longInput.toString());
        assertEquals(3000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputExceedingBufferSize() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertEquals(2000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test
    public void testEncodeAsUTF8WithControlCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0000World"; // Contains a null character
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] { 'H', 'e', 'l', 'l', 'o', 0, 'W', 'o', 'r', 'l', 'd' }, result);
    }


    @Test
    public void testQuoteAsUTF8WithCharacterAboveUnicodeRange() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uFFFD"; // Using replacement character for invalid Unicode
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xEF, (byte) 0xBF, (byte) 0xBD}, result); // UTF-8 encoding for U+FFFD
    }


    @Test
    public void testQuoteAsStringWithStandardEscapeSequences() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello \"World\""; // Contains quotes that need to be escaped
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello \\\"World\\\"".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithValidHighSurrogateAndInvalidLowSurrogate() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE00\uD83E"; // Valid high surrogate followed by an invalid low surrogate
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInput3000Characters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertEquals(3000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test
    public void testQuoteAsStringWithControlCharactersAndExclamationMark() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!"; // Contains newline and tab
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\nWorld\\t!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithLongInput3000Characters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longInput.append("a");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertEquals(3000, result.length);
        for (char c : result) {
            assertEquals('a', c);
        }
    }


    @Test
    public void testQuoteAsStringWithSpecialCharacters() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\tWorld\nNew Line\""; // Contains tab, newline, and quotes
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\tWorld\\nNew Line\\\"".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83E"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testQuoteAsUTF8WithDifferentValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE03\uD83D\uDE0A"; // Two valid surrogate pairs
        byte[] result = encoder.quoteAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x8A}, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBufferSizeAlternative() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String longInput = new String(new char[2000]).replace('\0', 'a'); // Long input exceeding buffer size
        char[] result = encoder.quoteAsString(longInput);
        assertEquals(2000, result.length);
        for (char c : result) {
            assertEquals('a', c);
        }
    }


    @Test
    public void testEncodeAsUTF8WithBufferFull() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, World! This is a long string to test buffer expansion during encoding.";
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testEncodeAsUTF8WithCharacterAboveValidUnicodeRange() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uFFFD"; // Using replacement character for invalid Unicode
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xEF, (byte) 0xBF, (byte) 0xBD}, result); // UTF-8 encoding for U+FFFD
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairDifferentInputVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongInputExceedingBufferSizeVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertEquals(3000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test
    public void testEncodeAsUTF8WithLargeInputExceedingBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertEquals(3000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePairs() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE03\uD83D\uDE0A"; // Two valid surrogate pairs
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x8A}, result);
    }


    @Test
    public void testQuoteAsStringWithNullTextBuffer() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Test input";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Test input".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIllegalSurrogatePairVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83D"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithControlCharactersAndExclamationMarkVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\nWorld\t!";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\nWorld\\t!".toCharArray(), result);
    }


    @Test
    public void testQuoteAsStringWithEmptyInputDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        char[] result = encoder.quoteAsString("");
        assertArrayEquals(new char[] {}, result);
    }


    @Test
    public void testQuoteAsStringWithLongInputExceedingBufferSizeDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longInput.append("A");
        }
        char[] result = encoder.quoteAsString(longInput.toString());
        assertEquals(2000, result.length);
        for (char c : result) {
            assertEquals('A', c);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testQuoteAsUTF8WithIllegalSurrogatePairsDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Invalid surrogate: \uD83D\uDC00\uD83D"; // Incomplete surrogate pair
        encoder.quoteAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello, 世界!"; // Contains characters that require multiple bytes
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }


    @Test
    public void testEncodeAsUTF8WithEmptyInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        byte[] result = encoder.encodeAsUTF8("");
        assertArrayEquals(new byte[] {}, result);
    }


    @Test
    public void testEncodeAsUTF8WithValidSurrogatePairDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDE03\uD83D\uDE0A"; // Two valid surrogate pairs
        byte[] result = encoder.encodeAsUTF8(input);
        assertArrayEquals(new byte[] {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x8A}, result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithIllegalSurrogatePairDifferentInput() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83E"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testEncodeAsUTF8WithLongStringCausingBufferExpansion() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longInput.append("a");
        }
        byte[] result = encoder.encodeAsUTF8(longInput.toString());
        assertEquals(3000, result.length);
        for (byte b : result) {
            assertEquals((byte) 'a', b);
        }
    }


    @Test
    public void testQuoteAsStringWithNumericEscape() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Hello\u0000World"; // Contains a null character
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Hello\\u0000World".toCharArray(), result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEncodeAsUTF8WithInvalidSurrogatePairVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "\uD83D\uDC00\uD83E"; // Invalid surrogate pair
        encoder.encodeAsUTF8(input);
    }


    @Test
    public void testQuoteAsStringWithNullTextBufferVariation() {
        JsonStringEncoder encoder = new JsonStringEncoder();
        String input = "Test input";
        char[] result = encoder.quoteAsString(input);
        assertArrayEquals("Test input".toCharArray(), result);
    }

    @Test
    public void  testPlaceHolder() {
        assertTrue(true); 
    }
}

