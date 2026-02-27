package com.joshwa.urlshortener.controller;

import com.joshwa.urlshortener.dto.ShortUrlRequestDTO;
import com.joshwa.urlshortener.dto.ShortUrlResponseDTO;
import com.joshwa.urlshortener.dto.ShortUrlStatsResponseDTO;
import com.joshwa.urlshortener.exception.*;
import org.springframework.http.ResponseEntity;


public interface ShortUrlController {

    ResponseEntity<ShortUrlResponseDTO> createShortUrl(ShortUrlRequestDTO dto)
            throws InvalidUrlException, InvalidExpiryException, ShortCodeGenerationException;
    ResponseEntity<Void> redirect(String shortCode)
            throws ShortUrlNotFoundException, ShortUrlExpiredException;
    ResponseEntity<ShortUrlStatsResponseDTO> getShortUrlStats(String shortCode) throws ShortUrlNotFoundException;

}
