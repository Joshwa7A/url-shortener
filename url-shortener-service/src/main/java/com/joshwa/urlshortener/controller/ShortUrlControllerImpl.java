package com.joshwa.urlshortener.controller;

import com.joshwa.urlshortener.dto.ShortUrlRequestDTO;
import com.joshwa.urlshortener.dto.ShortUrlResponseDTO;
import com.joshwa.urlshortener.dto.ShortUrlStatsResponseDTO;
import com.joshwa.urlshortener.exception.*;
import com.joshwa.urlshortener.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequiredArgsConstructor
public class ShortUrlControllerImpl implements ShortUrlController {

    private final ShortUrlService shortUrlService;

    @Override
    @Operation(summary = "Create short URL",
            description = "Generates a unique short URL for the provided original URL with optional expiry.")
    @PostMapping("/api/v1/short-urls")
    public ResponseEntity<ShortUrlResponseDTO> createShortUrl(@Valid @RequestBody ShortUrlRequestDTO dto) throws InvalidUrlException, InvalidExpiryException, ShortCodeGenerationException {
        ShortUrlResponseDTO responseDTO = shortUrlService.createShortUrl(dto);
        return ResponseEntity.created(URI.create(responseDTO.getShortUrl())).body(responseDTO);
    }

    @Override
    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL",
            description = "Redirects to the original URL if the short code exists and is not expired.")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode)
            throws ShortUrlNotFoundException, ShortUrlExpiredException {
        String originalUrl=shortUrlService.getOriginalUrlForRedirect(shortCode);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @Override
    @GetMapping("/api/v1/short-urls/{shortCode}/stats")
    @Operation(
            summary = "Retrieve short URL statistics",
            description = "Returns analytics and metadata for the specified short URL, including original URL, click count, creation time, and expiry details."
    )
    public ResponseEntity<ShortUrlStatsResponseDTO> getShortUrlStats(@PathVariable String shortCode) throws ShortUrlNotFoundException {
        ShortUrlStatsResponseDTO responseDTO = shortUrlService.getShortUrlStats(shortCode);
        return ResponseEntity.ok(responseDTO);
    }

}
