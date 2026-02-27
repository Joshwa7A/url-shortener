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
public class ShortUrlResponseDTO {

    private String shortCode;
    private String shortUrl;
    private OffsetDateTime expiryDate;
    private OffsetDateTime createdAt;

}
