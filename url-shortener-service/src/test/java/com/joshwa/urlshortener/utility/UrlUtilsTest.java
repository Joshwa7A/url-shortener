package com.joshwa.urlshortener.utility;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UrlUtilsTest {



    @ParameterizedTest
    @MethodSource("provideNormalizeUrlData")
    void normalizeUrlTest(String url, String expectedUrl){
        String normalizedUrl = UrlUtils.normalizeUrl(url);
        assertEquals(normalizedUrl, expectedUrl);
    }
    private static Stream<Arguments> provideNormalizeUrlData() {
        return Stream.of(
                Arguments.of("google.com", "https://google.com"),
                Arguments.of("www.google.com", "https://www.google.com"),
                Arguments.of("http://google.com", "http://google.com"),
                Arguments.of("https://google.com", "https://google.com"),
                Arguments.of("  google.com  ", "https://google.com"),
                Arguments.of("HTTP://google.com", "HTTP://google.com"),
                Arguments.of("https://sub.domain.com/path", "https://sub.domain.com/path"),
                Arguments.of("example.com/path", "https://example.com/path"),
                Arguments.of("example.com?query=1", "https://example.com?query=1"),
                Arguments.of("https://example.com?query=1", "https://example.com?query=1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidUrlData")
    void isValidUrlTest(String url, boolean expectedResult){
        boolean result = UrlUtils.isValidUrl(url);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideValidUrlData() {
        return Stream.of(
                Arguments.of("https://google.com", true),
                Arguments.of("http://google.com", true),
                Arguments.of("https://www.google.com", true),
                Arguments.of("https://sub.domain.com/path", true),
                Arguments.of("https://example.com?query=1", true),
                Arguments.of("https://example.com:8080", true),

                Arguments.of("google.com", false),
                Arguments.of("www.google.com", false),
                Arguments.of("htp://google.com", false),
                Arguments.of("https//google.com", false),
                Arguments.of("://google.com", false),
                Arguments.of("https://", false),
                Arguments.of("randomstring", false),
                Arguments.of("", false),
                Arguments.of("   ", false),
                Arguments.of(null, false)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "1","3","6"
    })
    void generateShortCodeTest(int length) {
        String result = UrlUtils.generateShortCode(length);
        assertNotNull(result);
        assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            assertTrue(Character.isLetterOrDigit(c));
        }
    }
}