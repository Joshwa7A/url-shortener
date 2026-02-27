package com.joshwa.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
public class ShortUrlRequestDTO {
    @NotBlank
    @Size(min = 1, max = 2000)
    private String originalUrl;

    private OffsetDateTime expiryDate;
}
