package com.roku.metadata.repository;

import com.roku.metadata.entity.ContentMetadata;
import com.roku.metadata.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ContentMetadata entity.
 * 
 * Provides Spring Data JPA query methods for filtering content
 * based on Roku's multi-region and categorization requirements.
 */
@Repository
public interface ContentMetadataRepository extends JpaRepository<ContentMetadata, Long> {

    /**
     * Find content by unique content ID.
     */
    Optional<ContentMetadata> findByContentId(String contentId);

    /**
     * Find all content by genre.
     */
    List<ContentMetadata> findByGenre(String genre);

    /**
     * Find all content by language.
     */
    List<ContentMetadata> findByLanguage(String language);

    /**
     * Find all content by media type.
     */
    List<ContentMetadata> findByMediaType(MediaType mediaType);

    /**
     * Find content by genre and language (multi-filter support).
     */
    List<ContentMetadata> findByGenreAndLanguage(String genre, String language);

    /**
     * Find content by media type and genre.
     */
    List<ContentMetadata> findByMediaTypeAndGenre(MediaType mediaType, String genre);

    /**
     * Find content by media type and language.
     */
    List<ContentMetadata> findByMediaTypeAndLanguage(MediaType mediaType, String language);
}
