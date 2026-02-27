package com.joshwa.urlshortener.mapper;

import com.joshwa.urlshortener.dto.ShortUrlRequestDTO;
import com.joshwa.urlshortener.dto.ShortUrlResponseDTO;
import com.joshwa.urlshortener.dto.ShortUrlStatsResponseDTO;
import com.joshwa.urlshortener.entity.ShortUrl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ShortUrlMapperTest {
    private static final String VALID_SHORT_CODE_FOR_TEST = "2gU8SHsz";
    private static final String VALID_NORMALIZED_URL_FOR_TEST = "https://www.love.com";
    private static final String VALID_SHORT_URL = "https://localhost:8080/" + VALID_NORMALIZED_URL_FOR_TEST;
    private static final Instant INSTANT_TIME_FOR_TEST = Instant.parse("2026-01-01T10:00:00Z");
    private static final OffsetDateTime OFFSET_TIME_FOR_TEST = OffsetDateTime.parse("2026-01-02T10:00:00Z");
    ShortUrlMapper shortUrlMapper = new ShortUrlMapper();

    @ParameterizedTest
    @MethodSource("provideShortUrlRequestDTO")
    void toEntityTest(ShortUrlRequestDTO dto, String shortCode, String normalizedUrl,
                      Instant expectedExpiryDate) {
        ShortUrl shortUrl = shortUrlMapper.toEntity(dto, shortCode, normalizedUrl);
        assertEquals(shortCode, shortUrl.getShortCode());
        assertEquals(normalizedUrl, shortUrl.getOriginalUrl());
        assertEquals(expectedExpiryDate, shortUrl.getExpiryDate());

    }

    private static Stream<Arguments> provideShortUrlRequestDTO() {
        ShortUrlRequestDTO dtoWithoutExpiry = new ShortUrlRequestDTO();
        ShortUrlRequestDTO dtoWithExpiry = new ShortUrlRequestDTO();
        dtoWithExpiry.setExpiryDate(OFFSET_TIME_FOR_TEST);
        return Stream.of(
                Arguments.of(dtoWithExpiry, VALID_SHORT_CODE_FOR_TEST, VALID_NORMALIZED_URL_FOR_TEST,
                        OFFSET_TIME_FOR_TEST.toInstant()),
                Arguments.of(dtoWithoutExpiry, VALID_SHORT_CODE_FOR_TEST, VALID_NORMALIZED_URL_FOR_TEST,
                        null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideShortUrlResponseDTO")
    void toResponseDTOTest(ShortUrl entity,
                           String shortUrl,
                           OffsetDateTime expectedExpiry) {

        ShortUrlResponseDTO responseDTO =
                shortUrlMapper.toResponseDTO(entity, shortUrl);

        assertEquals(expectedExpiry, responseDTO.getExpiryDate());
        assertEquals(shortUrl, responseDTO.getShortUrl());
        assertEquals(entity.getShortCode(), responseDTO.getShortCode());
    }

    private static Stream<Arguments> provideShortUrlResponseDTO() {

        ShortUrl entityWithExpiry = new ShortUrl();
        entityWithExpiry.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        entityWithExpiry.setExpiryDate(INSTANT_TIME_FOR_TEST);
        entityWithExpiry.setCreatedAt(INSTANT_TIME_FOR_TEST);
        ShortUrl entityWithoutExpiry = new ShortUrl();
        entityWithoutExpiry.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        entityWithoutExpiry.setCreatedAt(INSTANT_TIME_FOR_TEST);

        return Stream.of(
                Arguments.of(
                        entityWithExpiry,
                        VALID_SHORT_URL,
                        INSTANT_TIME_FOR_TEST.atOffset(ZoneOffset.UTC)
                ),
                Arguments.of(
                        entityWithoutExpiry,
                        VALID_SHORT_URL,
                        null
                )
        );
    }
    @ParameterizedTest
    @MethodSource("provideShortUrlStatsResponseDTO")
    void toStatsDTOTest(ShortUrl entity,
                           OffsetDateTime expectedExpiry) {

        ShortUrlStatsResponseDTO responseDTO =
                shortUrlMapper.toStatsDTO(entity);

        assertEquals(expectedExpiry, responseDTO.getExpiryDate());
        assertEquals(entity.getShortCode(), responseDTO.getShortCode());
        assertEquals(entity.getOriginalUrl(), responseDTO.getOriginalUrl());
        assertEquals(entity.getClickCount(), responseDTO.getClickCount());
        assertEquals(entity.getCreatedAt().atOffset(ZoneOffset.UTC), responseDTO.getCreatedAt());

    }

    private static Stream<Arguments> provideShortUrlStatsResponseDTO() {

        ShortUrl entityWithExpiry = new ShortUrl();
        entityWithExpiry.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        entityWithExpiry.setExpiryDate(INSTANT_TIME_FOR_TEST);
        entityWithExpiry.setCreatedAt(INSTANT_TIME_FOR_TEST);
        entityWithExpiry.setOriginalUrl(VALID_NORMALIZED_URL_FOR_TEST);
        entityWithExpiry.setClickCount(0L);


        ShortUrl entityWithoutExpiry = new ShortUrl();
        entityWithoutExpiry.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        entityWithoutExpiry.setCreatedAt(INSTANT_TIME_FOR_TEST);
        entityWithoutExpiry.setOriginalUrl(VALID_NORMALIZED_URL_FOR_TEST);
        entityWithoutExpiry.setClickCount(1L);

        return Stream.of(
                Arguments.of(
                        entityWithExpiry,
                        INSTANT_TIME_FOR_TEST.atOffset(ZoneOffset.UTC)
                ),
                Arguments.of(
                        entityWithoutExpiry,
                        null
                )
        );
    }
}