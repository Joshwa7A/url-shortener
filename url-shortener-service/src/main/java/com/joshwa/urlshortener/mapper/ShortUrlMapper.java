package com.joshwa.urlshortener.mapper;

import com.joshwa.urlshortener.dto.ShortUrlRequestDTO;
import com.joshwa.urlshortener.dto.ShortUrlResponseDTO;
import com.joshwa.urlshortener.dto.ShortUrlStatsResponseDTO;
import com.joshwa.urlshortener.entity.ShortUrl;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class ShortUrlMapper {

    public ShortUrl toEntity(ShortUrlRequestDTO dto,String shortCode,String normalizedUrl){
        ShortUrl shortUrl=new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(normalizedUrl);
        if(null!=dto.getExpiryDate()) {
            shortUrl.setExpiryDate(dto.getExpiryDate().toInstant());
        }
        return shortUrl;
    }
    public ShortUrlResponseDTO toResponseDTO(ShortUrl entity, String shortUrl){
        ShortUrlResponseDTO responseDTO=new ShortUrlResponseDTO();
        responseDTO.setShortUrl(shortUrl);
        responseDTO.setShortCode(entity.getShortCode());
        if(null!=entity.getExpiryDate()) {
            responseDTO.setExpiryDate(entity.getExpiryDate().atOffset(ZoneOffset.UTC));
        }
        responseDTO.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        return responseDTO;
    }

    public ShortUrlStatsResponseDTO toStatsDTO(ShortUrl entity){
        ShortUrlStatsResponseDTO responseDTO=new ShortUrlStatsResponseDTO();
        responseDTO.setShortCode(entity.getShortCode());
        responseDTO.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        responseDTO.setOriginalUrl(entity.getOriginalUrl());
        responseDTO.setClickCount(entity.getClickCount());
        if(null!=entity.getExpiryDate()) {
            responseDTO.setExpiryDate(entity.getExpiryDate().atOffset(ZoneOffset.UTC));
        }
        return responseDTO;
    }
}
