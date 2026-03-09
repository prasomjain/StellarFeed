package com.roku.metadata.entity;

import com.roku.metadata.model.MediaType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Content ID is required")
    @Size(min = 1, max = 100, message = "Content ID must be between 1 and 100 characters")
    @Column(name = "content_id", nullable = false, unique = true, length = 100)
    private String contentId;

    /**
     * Display title of the content.
     */
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Detailed description of the content (Roku requires 'longDescription').
     */
    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 2000, message = "Description must be between 1 and 2000 characters")
    @Column(name = "long_description", nullable = false, length = 2000)
    private String longDescription;

    /**
     * URL to the streaming video file.
     */
    @NotBlank(message = "Stream URL is required")
    @Size(min = 1, max = 500, message = "Stream URL must be between 1 and 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Stream URL must be a valid HTTP or HTTPS URL")
    @Column(name = "stream_url", nullable = false, length = 500)
    private String streamUrl;

    /**
     * URL to the HD thumbnail image (recommended: 1920x1080).
     */
    @NotBlank(message = "Thumbnail URL is required")
    @Size(min = 1, max = 500, message = "Thumbnail URL must be between 1 and 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Thumbnail URL must be a valid HTTP or HTTPS URL")
    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    /**
     * URL to the SD thumbnail image (recommended: 800x450).
     */
    @Size(max = 500, message = "SD Thumbnail URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "SD Thumbnail URL must be empty or a valid HTTP/HTTPS URL")
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
    @Size(max = 50, message = "Genre must not exceed 50 characters")
    @Column(length = 50)
    private String genre;

    /**
     * Language code for multi-region support (e.g., en, es).
     */
    @Size(max = 10, message = "Language code must not exceed 10 characters")
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
    @Size(max = 10, message = "Rating must not exceed 10 characters")
    @Column(length = 10)
    private String rating;
}
