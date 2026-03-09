package com.roku.metadata.entity;

import com.roku.metadata.model.MediaType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing content metadata that conforms to Roku's Direct Publisher specification.
 * 
 * This entity stores all required fields for Roku Search Feed JSON format,
 * including content identification, media details, and streaming information.
 */
@Entity
@Table(name = "content_metadata", indexes = {
    @Index(name = "idx_content_id", columnList = "content_id", unique = true),
    @Index(name = "idx_media_type", columnList = "media_type"),
    @Index(name = "idx_genre", columnList = "genre"),
    @Index(name = "idx_language", columnList = "language")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier for the content item (Roku requirement).
     */
    @NotBlank
    @Column(name = "content_id", nullable = false, unique = true, length = 100)
    private String contentId;

    /**
     * Display title of the content.
     */
    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Detailed description of the content (Roku requires 'longDescription').
     */
    @NotBlank
    @Column(name = "long_description", nullable = false, length = 2000)
    private String longDescription;

    /**
     * URL to the streaming video file.
     */
    @NotBlank
    @Column(name = "stream_url", nullable = false, length = 500)
    private String streamUrl;

    /**
     * URL to the HD thumbnail image (recommended: 1920x1080).
     */
    @NotBlank
    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    /**
     * URL to the SD thumbnail image (recommended: 800x450).
     */
    @Column(name = "sd_thumbnail_url", length = 500)
    private String sdThumbnailUrl;

    /**
     * Type of media content: MOVIE, SERIES, or SHORTFORM.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    /**
     * Release date in ISO 8601 format (Roku requirement).
     */
    @NotNull
    @Column(name = "release_date", nullable = false)
    private LocalDateTime releaseDate;

    /**
     * Content genre for filtering (e.g., Action, Drama, Comedy).
     */
    @Column(length = 50)
    private String genre;

    /**
     * Language code for multi-region support (e.g., en, es).
     */
    @Column(length = 10)
    private String language;

    /**
     * Duration in minutes (optional, useful for movies and episodes).
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * Content rating (e.g., PG, PG-13, R).
     */
    @Column(length = 10)
    private String rating;
}
