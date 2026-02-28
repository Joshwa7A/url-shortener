package com.joshwa.urlshortener.service;

import com.joshwa.urlshortener.dto.ShortUrlRequestDTO;
import com.joshwa.urlshortener.dto.ShortUrlResponseDTO;
import com.joshwa.urlshortener.dto.ShortUrlStatsResponseDTO;
import com.joshwa.urlshortener.entity.ShortUrl;
import com.joshwa.urlshortener.exception.*;
import com.joshwa.urlshortener.mapper.ShortUrlMapper;
import com.joshwa.urlshortener.repository.ShortUrlRepository;
import static com.joshwa.urlshortener.utility.UrlUtils.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class ShortUrlServiceImplTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;
    private static final String VALID_URL_FOR_TEST="https://www.love.com";
    private static final String VALID_SHORT_CODE_FOR_TEST="2gU8SHsz";
    private static final String VALID_CACHE_KEY_FOR_TEST="shorturl:"+VALID_SHORT_CODE_FOR_TEST;
    private static final Clock clock=Clock.fixed(
            Instant.parse("2026-01-01T10:00:00Z"),
            ZoneOffset.UTC
    );
    private static final OffsetDateTime FUTURE_VALID_EXPIRY=OffsetDateTime.parse("2026-01-02T10:00:00Z");
    private static final OffsetDateTime PAST_INVALID_EXPIRY=OffsetDateTime.parse("2025-12-31T10:00:00Z");


    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private ShortUrlMapper shortUrlMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    ShortUrlServiceImpl shortUrlServiceImpl;

    @BeforeEach
    void setUp() {
       shortUrlServiceImpl= new ShortUrlServiceImpl(shortUrlRepository,clock,shortUrlMapper,redisTemplate);
    }

    @Test
    void shouldCreateShortUrl_whenValidRequestWithoutExpiry() throws InvalidUrlException, InvalidExpiryException, ShortCodeGenerationException {
        ShortUrlRequestDTO requestDTO=new ShortUrlRequestDTO();
        requestDTO.setOriginalUrl(VALID_URL_FOR_TEST);
        ShortUrl shortUrl=new ShortUrl();
        ShortUrlResponseDTO expectedResponseDTO=new ShortUrlResponseDTO();
        when(shortUrlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(shortUrlMapper.toEntity(any(ShortUrlRequestDTO.class),anyString(),anyString()))
                .thenReturn(shortUrl);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);
        when(shortUrlMapper.toResponseDTO(any(ShortUrl.class),anyString()))
                .thenReturn(expectedResponseDTO);
        ShortUrlResponseDTO resultDTO=shortUrlServiceImpl.createShortUrl(requestDTO);
        assertEquals(expectedResponseDTO,resultDTO);
        verify(shortUrlMapper).toEntity(any(ShortUrlRequestDTO.class),anyString(),anyString());
        verify(shortUrlMapper).toResponseDTO(any(ShortUrl.class),anyString());
        verify(shortUrlRepository).save(any(ShortUrl.class));
        verify(shortUrlRepository).findByShortCode(anyString());


    }
    @Test
    void shouldCreateShortUrl_whenValidRequestWithFutureExpiry() throws InvalidUrlException, InvalidExpiryException, ShortCodeGenerationException {
        ShortUrlRequestDTO requestDTO=new ShortUrlRequestDTO();
        requestDTO.setOriginalUrl(VALID_URL_FOR_TEST);
        requestDTO.setExpiryDate(FUTURE_VALID_EXPIRY);
        ShortUrl shortUrl=new ShortUrl();
        shortUrl.setOriginalUrl(VALID_URL_FOR_TEST);
        ShortUrlResponseDTO expectedResponseDTO=new ShortUrlResponseDTO();
        expectedResponseDTO.setExpiryDate(FUTURE_VALID_EXPIRY);
        when(shortUrlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(shortUrlMapper.toEntity(any(ShortUrlRequestDTO.class),anyString(),anyString()))
                .thenReturn(shortUrl);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);
        when(shortUrlMapper.toResponseDTO(any(ShortUrl.class),anyString()))
                .thenReturn(expectedResponseDTO);
        ShortUrlResponseDTO resultDTO=shortUrlServiceImpl.createShortUrl(requestDTO);
        assertEquals(expectedResponseDTO,resultDTO);
        verify(shortUrlMapper).toEntity(any(ShortUrlRequestDTO.class),anyString(),anyString());
        verify(shortUrlMapper).toResponseDTO(any(ShortUrl.class),anyString());
        verify(shortUrlRepository).save(any(ShortUrl.class));
        verify(shortUrlRepository).findByShortCode(anyString());
    }

    @Test
    void shouldThrowInvalidExpiryException_whenExpiryIsBeforeNow(){

        ShortUrlRequestDTO requestDTO=new ShortUrlRequestDTO();
        requestDTO.setExpiryDate(PAST_INVALID_EXPIRY);
        requestDTO.setOriginalUrl(VALID_URL_FOR_TEST);
        InvalidExpiryException exception=
                assertThrows(InvalidExpiryException.class,
                        ()->shortUrlServiceImpl.createShortUrl(requestDTO)
                );
        assertEquals(INVALID_EXPIRY_EXCEPTION_MESSAGE,exception.getMessage());
        verify(shortUrlMapper,never()).toEntity(any(ShortUrlRequestDTO.class),anyString(),anyString());
        verify(shortUrlMapper,never()).toResponseDTO(any(ShortUrl.class),anyString());
        verify(shortUrlRepository,never()).save(any(ShortUrl.class));
        verify(shortUrlRepository,never()).findByShortCode(anyString());


    }
    @Test
    void shouldThrowInvalidUrlException_whenUrlIsInvalid(){
        ShortUrlRequestDTO requestDTO=new ShortUrlRequestDTO();
        requestDTO.setOriginalUrl("1.23/ioe");
        InvalidUrlException exception=
                assertThrows(InvalidUrlException.class,
                        ()->shortUrlServiceImpl.createShortUrl(requestDTO)
                );
        assertEquals(INVALID_URL_EXCEPTION_MESSAGE,exception.getMessage());
        verify(shortUrlMapper,never()).toEntity(any(ShortUrlRequestDTO.class),anyString(),anyString());
        verify(shortUrlMapper,never()).toResponseDTO(any(ShortUrl.class),anyString());
        verify(shortUrlRepository,never()).save(any(ShortUrl.class));
        verify(shortUrlRepository,never()).findByShortCode(anyString());

    }

    @Test
    void shouldThrowShortCodeGenerationException_whenAllAttemptsCollide(){
        ShortUrlRequestDTO requestDTO=new ShortUrlRequestDTO();
        requestDTO.setOriginalUrl(VALID_URL_FOR_TEST);
        ShortUrl shortUrlForCollision=new ShortUrl();
        shortUrlForCollision.setOriginalUrl(VALID_URL_FOR_TEST);
        when(shortUrlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrlForCollision));
        ShortCodeGenerationException exception=
                assertThrows(ShortCodeGenerationException.class,
                        ()->shortUrlServiceImpl.createShortUrl(requestDTO)
                );
        assertEquals(SHORT_CODE_GENERATION_EXCEPTION_MESSAGE,exception.getMessage());
        verify(shortUrlMapper,never()).toEntity(any(ShortUrlRequestDTO.class),anyString(),anyString());
        verify(shortUrlMapper,never()).toResponseDTO(any(ShortUrl.class),anyString());
        verify(shortUrlRepository,never()).save(any(ShortUrl.class));
        verify(shortUrlRepository,times(ShortUrlServiceImpl.MAX_SHORT_CODE_GENERATION_ATTEMPTS)).findByShortCode(anyString());

    }
    @Test
    void shouldReturnCachedUrlAndIncrementClickCount_whenCacheHit() throws ShortUrlNotFoundException, ShortUrlExpiredException {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(VALID_URL_FOR_TEST);
        String resultUrl=shortUrlServiceImpl.getOriginalUrlForRedirect(VALID_SHORT_CODE_FOR_TEST);
        assertEquals(VALID_URL_FOR_TEST,resultUrl);
        verify(shortUrlRepository).atomicUpdateClickCount(VALID_SHORT_CODE_FOR_TEST);
        verify(shortUrlRepository, never()).findByShortCode(anyString());
        verify(valueOps).get(VALID_CACHE_KEY_FOR_TEST);
    }

    @Test
    void shouldFetchFromDbCacheWithTtlAndIncrement_whenCacheMissAndNotExpired() throws ShortUrlNotFoundException, ShortUrlExpiredException {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);
        ShortUrl shortUrl=new ShortUrl();
        shortUrl.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        shortUrl.setOriginalUrl(VALID_URL_FOR_TEST);
        shortUrl.setExpiryDate(FUTURE_VALID_EXPIRY.toInstant());
        when(shortUrlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));
        String resultUrl=shortUrlServiceImpl.getOriginalUrlForRedirect(VALID_SHORT_CODE_FOR_TEST);
        assertEquals(VALID_URL_FOR_TEST,resultUrl);
        verify(shortUrlRepository).atomicUpdateClickCount(VALID_SHORT_CODE_FOR_TEST);
        verify(shortUrlRepository).findByShortCode(VALID_SHORT_CODE_FOR_TEST);
        verify(valueOps).set(anyString(),anyString(),any(Duration.class));
        verify(valueOps).get(VALID_CACHE_KEY_FOR_TEST);

    }
    @Test
    void shouldCacheWithoutTtl_whenNoExpiryAndCacheMiss() throws ShortUrlNotFoundException, ShortUrlExpiredException {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);
        ShortUrl shortUrl=new ShortUrl();
        shortUrl.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        shortUrl.setOriginalUrl(VALID_URL_FOR_TEST);
        when(shortUrlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));
        String resultUrl=shortUrlServiceImpl.getOriginalUrlForRedirect(VALID_SHORT_CODE_FOR_TEST);
        assertEquals(VALID_URL_FOR_TEST,resultUrl);
        verify(shortUrlRepository).atomicUpdateClickCount(VALID_SHORT_CODE_FOR_TEST);
        verify(shortUrlRepository).findByShortCode(VALID_SHORT_CODE_FOR_TEST);
        verify(valueOps).set(VALID_CACHE_KEY_FOR_TEST,VALID_URL_FOR_TEST);
        verify(valueOps).get(VALID_CACHE_KEY_FOR_TEST);
    }

    @Test
    void shouldThrowShortUrlNotFoundException_whenCacheMissAndDbMiss(){
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);
        when(shortUrlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        ShortUrlNotFoundException exception=
                assertThrows(ShortUrlNotFoundException.class,()->
                    shortUrlServiceImpl.getOriginalUrlForRedirect(VALID_SHORT_CODE_FOR_TEST)
                );
        assertEquals(SHORT_URL_NOT_FOUND_EXCEPTION_MESSAGE,exception.getMessage());
        verify(shortUrlRepository,never()).atomicUpdateClickCount(VALID_SHORT_CODE_FOR_TEST);
        verify(shortUrlRepository).findByShortCode(VALID_SHORT_CODE_FOR_TEST);
        verify(valueOps,never()).set(VALID_CACHE_KEY_FOR_TEST,VALID_URL_FOR_TEST);
        verify(valueOps).get(VALID_CACHE_KEY_FOR_TEST);
    }
    @Test
    void shouldThrowShortUrlExpiredException_whenExpired(){
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);
        ShortUrl shortUrl=new ShortUrl();
        shortUrl.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        shortUrl.setOriginalUrl(VALID_URL_FOR_TEST);
        shortUrl.setExpiryDate(clock.instant());
        when(shortUrlRepository.findByShortCode(anyString())).thenReturn(Optional.of(shortUrl));
        ShortUrlExpiredException exception=
                assertThrows(ShortUrlExpiredException.class,()->
                shortUrlServiceImpl.getOriginalUrlForRedirect(VALID_SHORT_CODE_FOR_TEST)
                );
        assertEquals(SHORT_URL_EXPIRED_EXCEPTION_MESSAGE,exception.getMessage());
        verify(shortUrlRepository,never()).atomicUpdateClickCount(VALID_SHORT_CODE_FOR_TEST);
        verify(shortUrlRepository).findByShortCode(VALID_SHORT_CODE_FOR_TEST);
        verify(valueOps,never()).set(anyString(),anyString(),any(Duration.class));
        verify(valueOps).get(VALID_CACHE_KEY_FOR_TEST);
    }
    @Test
    void shouldReturnStats_whenShortUrlExists() throws ShortUrlNotFoundException {
        ShortUrl shortUrl=new ShortUrl();
        shortUrl.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        shortUrl.setOriginalUrl(VALID_URL_FOR_TEST);
        ShortUrlStatsResponseDTO expectedResponseDTO=new ShortUrlStatsResponseDTO();
        expectedResponseDTO.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        expectedResponseDTO.setOriginalUrl(VALID_URL_FOR_TEST);
        when(shortUrlRepository.findByShortCode(VALID_SHORT_CODE_FOR_TEST)).thenReturn(Optional.of(shortUrl));
        when(shortUrlMapper.toStatsDTO(any(ShortUrl.class))).thenReturn(expectedResponseDTO);
        ShortUrlStatsResponseDTO resultResponseDTO=
                shortUrlServiceImpl.getShortUrlStats(VALID_SHORT_CODE_FOR_TEST);
        assertEquals(expectedResponseDTO,resultResponseDTO);
        verify(shortUrlRepository).findByShortCode(VALID_SHORT_CODE_FOR_TEST);
        verify(shortUrlMapper).toStatsDTO(shortUrl);
    }

    @Test
    void shouldThrowShortUrlNotFoundException_whenStatsNotFound(){
        when(shortUrlRepository.findByShortCode(VALID_SHORT_CODE_FOR_TEST)).thenReturn(Optional.empty());
        ShortUrlNotFoundException exception=
                assertThrows(ShortUrlNotFoundException.class,()->
                        shortUrlServiceImpl.getShortUrlStats(VALID_SHORT_CODE_FOR_TEST)
                        );
        assertEquals(SHORT_URL_NOT_FOUND_EXCEPTION_MESSAGE,exception.getMessage());
        verify(shortUrlRepository).findByShortCode(VALID_SHORT_CODE_FOR_TEST);
        verify(shortUrlMapper,never()).toStatsDTO(any(ShortUrl.class));
    }

    @Test
    void shouldReuseExistingUrl_whenExistingAndRequestExpiryAreNull() throws Exception {
        ShortUrlRequestDTO request = new ShortUrlRequestDTO();
        request.setOriginalUrl(VALID_URL_FOR_TEST);

        ShortUrl existing = new ShortUrl();
        existing.setOriginalUrl(VALID_URL_FOR_TEST);
        existing.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        existing.setExpiryDate(null);

        when(shortUrlRepository.findActiveByOriginalUrl(eq(VALID_URL_FOR_TEST), any()))
                .thenReturn(Optional.of(existing));

        ShortUrlResponseDTO responseDTO = new ShortUrlResponseDTO();
        when(shortUrlMapper.toResponseDTO(eq(existing), anyString()))
                .thenReturn(responseDTO);

        ShortUrlResponseDTO result = shortUrlServiceImpl.createShortUrl(request);

        assertEquals(responseDTO, result);
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldReuseExistingUrl_whenExpiryMatchesExactly() throws Exception {
        ShortUrlRequestDTO request = new ShortUrlRequestDTO();
        request.setOriginalUrl(VALID_URL_FOR_TEST);
        request.setExpiryDate(FUTURE_VALID_EXPIRY);

        ShortUrl existing = new ShortUrl();
        existing.setOriginalUrl(VALID_URL_FOR_TEST);
        existing.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        existing.setExpiryDate(FUTURE_VALID_EXPIRY.toInstant());

        when(shortUrlRepository.findActiveByOriginalUrl(eq(VALID_URL_FOR_TEST), any()))
                .thenReturn(Optional.of(existing));

        ShortUrlResponseDTO responseDTO = new ShortUrlResponseDTO();
        when(shortUrlMapper.toResponseDTO(eq(existing), anyString()))
                .thenReturn(responseDTO);

        ShortUrlResponseDTO result = shortUrlServiceImpl.createShortUrl(request);

        assertEquals(responseDTO, result);
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldCreateNewUrl_whenExistingIsPermanentAndRequestHasExpiry() throws Exception {
        ShortUrlRequestDTO request = new ShortUrlRequestDTO();
        request.setOriginalUrl(VALID_URL_FOR_TEST);
        request.setExpiryDate(FUTURE_VALID_EXPIRY);

        ShortUrl existing = new ShortUrl();
        existing.setOriginalUrl(VALID_URL_FOR_TEST);
        existing.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        existing.setExpiryDate(null);

        when(shortUrlRepository.findActiveByOriginalUrl(eq(VALID_URL_FOR_TEST), any()))
                .thenReturn(Optional.of(existing));
        when(shortUrlRepository.findByShortCode(anyString()))
                .thenReturn(Optional.empty());

        ShortUrl entity = new ShortUrl();
        when(shortUrlMapper.toEntity(any(), anyString(), anyString()))
                .thenReturn(entity);
        when(shortUrlRepository.save(any())).thenReturn(entity);
        when(shortUrlMapper.toResponseDTO(any(), anyString()))
                .thenReturn(new ShortUrlResponseDTO());

        shortUrlServiceImpl.createShortUrl(request);

        verify(shortUrlRepository).save(any());
    }

    @Test
    void shouldCreateNewUrl_whenExistingExpiryDiffersFromRequestExpiry() throws Exception {
        ShortUrlRequestDTO request = new ShortUrlRequestDTO();
        request.setOriginalUrl(VALID_URL_FOR_TEST);
        request.setExpiryDate(FUTURE_VALID_EXPIRY);

        ShortUrl existing = new ShortUrl();
        existing.setOriginalUrl(VALID_URL_FOR_TEST);
        existing.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        existing.setExpiryDate(Instant.now(clock).plusSeconds(3600));

        when(shortUrlRepository.findActiveByOriginalUrl(eq(VALID_URL_FOR_TEST), any()))
                .thenReturn(Optional.of(existing));
        when(shortUrlRepository.findByShortCode(anyString()))
                .thenReturn(Optional.empty());

        ShortUrl entity = new ShortUrl();
        when(shortUrlMapper.toEntity(any(), anyString(), anyString()))
                .thenReturn(entity);
        when(shortUrlRepository.save(any())).thenReturn(entity);
        when(shortUrlMapper.toResponseDTO(any(), anyString()))
                .thenReturn(new ShortUrlResponseDTO());

        shortUrlServiceImpl.createShortUrl(request);

        verify(shortUrlRepository).save(any());
    }

    @Test
    void shouldCreateNewUrl_whenExistingHasExpiryAndRequestIsNull() throws Exception {
        ShortUrlRequestDTO request = new ShortUrlRequestDTO();
        request.setOriginalUrl(VALID_URL_FOR_TEST);

        ShortUrl existing = new ShortUrl();
        existing.setOriginalUrl(VALID_URL_FOR_TEST);
        existing.setShortCode(VALID_SHORT_CODE_FOR_TEST);
        existing.setExpiryDate(FUTURE_VALID_EXPIRY.toInstant());

        when(shortUrlRepository.findActiveByOriginalUrl(eq(VALID_URL_FOR_TEST), any()))
                .thenReturn(Optional.of(existing));
        when(shortUrlRepository.findByShortCode(anyString()))
                .thenReturn(Optional.empty());

        ShortUrl entity = new ShortUrl();
        when(shortUrlMapper.toEntity(any(), anyString(), anyString()))
                .thenReturn(entity);
        when(shortUrlRepository.save(any())).thenReturn(entity);
        when(shortUrlMapper.toResponseDTO(any(), anyString()))
                .thenReturn(new ShortUrlResponseDTO());

        shortUrlServiceImpl.createShortUrl(request);

        verify(shortUrlRepository).save(any());
    }

    @Test
    void shouldCreateNewUrl_whenNoActiveExistingUrlFound() throws Exception {
        ShortUrlRequestDTO request = new ShortUrlRequestDTO();
        request.setOriginalUrl(VALID_URL_FOR_TEST);

        when(shortUrlRepository.findActiveByOriginalUrl(eq(VALID_URL_FOR_TEST), any()))
                .thenReturn(Optional.empty());
        when(shortUrlRepository.findByShortCode(anyString()))
                .thenReturn(Optional.empty());

        ShortUrl entity = new ShortUrl();
        when(shortUrlMapper.toEntity(any(), anyString(), anyString()))
                .thenReturn(entity);
        when(shortUrlRepository.save(any())).thenReturn(entity);
        when(shortUrlMapper.toResponseDTO(any(), anyString()))
                .thenReturn(new ShortUrlResponseDTO());

        shortUrlServiceImpl.createShortUrl(request);

        verify(shortUrlRepository).save(any());
    }

}