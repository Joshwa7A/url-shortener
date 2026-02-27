package com.joshwa.urlshortener.repository;

import com.joshwa.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    @Modifying
    @Query("""
    UPDATE ShortUrl s
       SET s.clickCount = s.clickCount + 1,
           s.updatedAt = CURRENT_TIMESTAMP
     WHERE s.shortCode = :shortCode
       AND (
            s.expiryDate IS NULL
            OR s.expiryDate > CURRENT_TIMESTAMP
            )
    """)
    void atomicUpdateClickCount(@Param("shortCode") String shortCode);

    int deleteByExpiryDateBefore(Instant instant);

}
