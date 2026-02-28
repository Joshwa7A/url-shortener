package com.joshwa.urlshortener.service;

import com.joshwa.urlshortener.dto.ShortUrlRequestDTO;
import com.joshwa.urlshortener.dto.ShortUrlResponseDTO;
import com.joshwa.urlshortener.dto.ShortUrlStatsResponseDTO;
import com.joshwa.urlshortener.entity.ShortUrl;
import com.joshwa.urlshortener.exception.*;
import com.joshwa.urlshortener.mapper.ShortUrlMapper;
import com.joshwa.urlshortener.repository.ShortUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.joshwa.urlshortener.utility.UrlUtils.*;

@Slf4j
@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    @Value("${joshwa.shorturlapp.base-url}")
    private String baseAppUrl;
    private static final String CACHE_KEY_PREFIX="shorturl:";
    private final ShortUrlRepository shortUrlRepository;
    private final Clock clock;
    private final ShortUrlMapper shortUrlMapper;
    private final StringRedisTemplate redisTemplate;

    ShortUrlServiceImpl(ShortUrlRepository shortUrlRepository, Clock clock, ShortUrlMapper shortUrlMapper, StringRedisTemplate redisTemplate) {
        this.shortUrlRepository = shortUrlRepository;
        this.clock = clock;
        this.shortUrlMapper = shortUrlMapper;
        this.redisTemplate = redisTemplate;
    }

    public static final int SHORT_CODE_LENGTH=8;
    public static final int MAX_SHORT_CODE_GENERATION_ATTEMPTS=10;

    @Override
    @Transactional
    public ShortUrlResponseDTO createShortUrl(ShortUrlRequestDTO urlRequestDTO)
            throws InvalidUrlException, InvalidExpiryException, ShortCodeGenerationException {

        String normalizedUrl = normalizeUrl(urlRequestDTO.getOriginalUrl());
        OffsetDateTime now = OffsetDateTime.now(clock);

        if (!isValidUrl(normalizedUrl)) {
            throw new InvalidUrlException(INVALID_URL_EXCEPTION_MESSAGE);
        }

        if (urlRequestDTO.getExpiryDate() != null &&
                urlRequestDTO.getExpiryDate().isBefore(now)) {
            throw new InvalidExpiryException(INVALID_EXPIRY_EXCEPTION_MESSAGE);
        }

        Instant nowInstant = now.toInstant();
        Instant requestedExpiryInstant = urlRequestDTO.getExpiryDate() != null
                ? urlRequestDTO.getExpiryDate().toInstant()
                : null;

        Optional<ShortUrl> existingUrl =
                shortUrlRepository.findActiveByOriginalUrl(normalizedUrl, nowInstant);

        if (existingUrl.isPresent()) {
            ShortUrl existing = existingUrl.get();
            Instant existingExpiry = existing.getExpiryDate();

            if (Objects.equals(existingExpiry, requestedExpiryInstant)) {
                String existingShortUrl = buildShortUrl(existing.getShortCode(), baseAppUrl);
                return shortUrlMapper.toResponseDTO(existing, existingShortUrl);
            }
        }

        String shortCode = generateUniqueShortCode();
        String shortUrlValue = buildShortUrl(shortCode, baseAppUrl);

        ShortUrl shortUrl =
                shortUrlMapper.toEntity(urlRequestDTO, shortCode, normalizedUrl);

        shortUrlRepository.save(shortUrl);

        return shortUrlMapper.toResponseDTO(shortUrl, shortUrlValue);
    }

    @Override
    @Transactional
    public String getOriginalUrlForRedirect(String shortCode)
            throws ShortUrlNotFoundException, ShortUrlExpiredException {

        String cacheKey=CACHE_KEY_PREFIX+shortCode;
        String cachedUrl=redisTemplate.opsForValue().get(cacheKey);
        if(null!=cachedUrl){
            shortUrlRepository.atomicUpdateClickCount(shortCode);
            return cachedUrl;
        }
        Instant now=clock.instant();
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(SHORT_URL_NOT_FOUND_EXCEPTION_MESSAGE));

        if (shortUrl.getExpiryDate() != null &&
                !shortUrl.getExpiryDate().isAfter(now)) {
            throw new ShortUrlExpiredException(SHORT_URL_EXPIRED_EXCEPTION_MESSAGE);
        }
        if(shortUrl.getExpiryDate() != null){
            Duration remaining=Duration.between(now,shortUrl.getExpiryDate());
            redisTemplate.opsForValue().set(cacheKey, shortUrl.getOriginalUrl(),remaining);
        }
        else{
            redisTemplate.opsForValue().set(cacheKey, shortUrl.getOriginalUrl());
        }
        shortUrlRepository.atomicUpdateClickCount(shortCode);
        return shortUrl.getOriginalUrl();
    }

    @Override
    public ShortUrlStatsResponseDTO getShortUrlStats(String shortCode) throws ShortUrlNotFoundException {
        ShortUrl shortUrl =shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(()->new ShortUrlNotFoundException(SHORT_URL_NOT_FOUND_EXCEPTION_MESSAGE));

        return shortUrlMapper.toStatsDTO(shortUrl);
    }

    private String generateUniqueShortCode() throws ShortCodeGenerationException {
        String shortCode=generateShortCode(SHORT_CODE_LENGTH);
        int countAttempt=0;
        while(countAttempt<MAX_SHORT_CODE_GENERATION_ATTEMPTS){
            if(shortUrlRepository.findByShortCode(shortCode).isPresent()){
                shortCode=generateShortCode(SHORT_CODE_LENGTH);
                countAttempt++;
            }
            else {
                return shortCode;
            }
        }
        throw new ShortCodeGenerationException(SHORT_CODE_GENERATION_EXCEPTION_MESSAGE);
    }

    private String buildShortUrl(String shortCode,String baseAppUrl){
        return baseAppUrl+"/"+shortCode;
    }

}
