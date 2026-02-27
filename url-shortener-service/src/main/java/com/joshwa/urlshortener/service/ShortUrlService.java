package com.joshwa.urlshortener.service;

import com.joshwa.urlshortener.dto.ShortUrlRequestDTO;
import com.joshwa.urlshortener.dto.ShortUrlResponseDTO;
import com.joshwa.urlshortener.dto.ShortUrlStatsResponseDTO;
import com.joshwa.urlshortener.exception.*;

public interface ShortUrlService {
    ShortUrlResponseDTO createShortUrl(ShortUrlRequestDTO urlRequestDTO)
            throws InvalidUrlException, InvalidExpiryException, ShortCodeGenerationException;

    String getOriginalUrlForRedirect(String shortCode) throws ShortUrlNotFoundException, ShortUrlExpiredException;
    ShortUrlStatsResponseDTO getShortUrlStats(String shortCode) throws ShortUrlNotFoundException;

}
