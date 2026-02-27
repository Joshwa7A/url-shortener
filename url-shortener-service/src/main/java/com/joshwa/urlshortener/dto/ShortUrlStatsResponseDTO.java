package com.joshwa.urlshortener.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
public class ShortUrlStatsResponseDTO {
    private String originalUrl;
    private Long clickCount;
    private String shortCode;
    private OffsetDateTime expiryDate;
    private OffsetDateTime createdAt;
}
