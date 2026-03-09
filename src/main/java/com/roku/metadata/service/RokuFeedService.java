package com.roku.metadata.service;

import com.roku.metadata.dto.ContentItemDto;
import com.roku.metadata.dto.RokuFeedResponse;
import com.roku.metadata.entity.ContentMetadata;
import com.roku.metadata.model.MediaType;
import com.roku.metadata.repository.ContentMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core service for generating streaming-compliant content feeds.
 * 
 * Implements caching strategy for high-performance delivery to millions of streaming devices.
 * All public methods return cached results to minimize database load.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RokuFeedService {

    private final ContentMetadataRepository contentRepository;
    private final FeedValidationService validationService;

    @Value("${streaming.feed.provider-name}")
    private String providerName;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Get all content organized by media type with Redis caching.
     * Cache key: "allContent"
     * TTL: Configured in application.yml (default: 1 hour)
     */
    @Cacheable(value = "rokuFeed", key = "'allContent'")
    @Transactional(readOnly = true)
    public RokuFeedResponse getAllContent() {
        log.info("Fetching all content from database (cache miss)");
        
        List<ContentMetadata> allContent = contentRepository.findAll();
        return buildFeedResponse(allContent);
    }

    /**
     * Get content filtered by genre and/or language.
     * Cache key: Dynamic based on filter parameters (handles nulls safely)
     */
    @Cacheable(value = "rokuFeed", 
               key = "'genre:' + (#genre != null ? #genre : 'null') + ':language:' + (#language != null ? #language : 'null')")
    @Transactional(readOnly = true)
    public RokuFeedResponse getContentByFilters(String genre, String language) {
        log.info("Fetching content with filters - genre: {}, language: {} (cache miss)", genre, language);
        
        List<ContentMetadata> filteredContent;
        
        try {
            if (genre != null && language != null) {
                filteredContent = contentRepository.findByGenreAndLanguage(genre, language);
            } else if (genre != null) {
                filteredContent = contentRepository.findByGenre(genre);
            } else if (language != null) {
                filteredContent = contentRepository.findByLanguage(language);
            } else {
                filteredContent = contentRepository.findAll();
            }
            
            if (filteredContent == null) {
                log.warn("Repository returned null for filters - genre: {}, language: {}", genre, language);
                filteredContent = List.of();
            }
        } catch (Exception e) {
            log.error("Database error while fetching filtered content - genre: {}, language: {}", genre, language, e);
            throw new RuntimeException("Failed to fetch content from database", e);
        }
        
        return buildFeedResponse(filteredContent);
    }

    /**
     * Build streaming feed response from content metadata list.
     * Handles empty lists and null values gracefully.
     */
    private RokuFeedResponse buildFeedResponse(List<ContentMetadata> contentList) {
        // Handle null or empty content list
        if (contentList == null) {
            log.warn("Received null content list, returning empty feed");
            contentList = List.of();
        }
        
        if (contentList.isEmpty()) {
            log.info("No content found, returning empty feed");
        }
        
        // Group content by media type with null safety
        List<ContentItemDto> movies = contentList.stream()
            .filter(c -> c != null && c.getMediaType() == MediaType.MOVIE)
            .map(this::convertToDto)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());

        List<ContentItemDto> series = contentList.stream()
            .filter(c -> c != null && c.getMediaType() == MediaType.SERIES)
            .map(this::convertToDto)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());

        List<ContentItemDto> shortFormVideos = contentList.stream()
            .filter(c -> c != null && c.getMediaType() == MediaType.SHORTFORM)
            .map(this::convertToDto)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());

        // Validate provider name is configured
        String safeProviderName = (providerName != null && !providerName.trim().isEmpty()) 
            ? providerName 
            : "StellarFeed Platform";

        // Build response
        RokuFeedResponse response = RokuFeedResponse.builder()
            .providerName(safeProviderName)
            .language("en")
            .lastUpdated(LocalDateTime.now().format(ISO_FORMATTER))
            .movies(movies)
            .series(series)
            .shortFormVideos(shortFormVideos)
            .build();

        response.calculateTotalCount();

        // Validate before returning
        try {
            validationService.validateFeedStructure(response);
        } catch (Exception e) {
            log.warn("Feed validation failed, but continuing with response", e);
        }

        log.info("Built feed response with {} total items (movies: {}, series: {}, shortForm: {})",
            response.getTotalCount(), movies.size(), series.size(), shortFormVideos.size());

        return response;
    }

    /**
     * Convert ContentMetadata entity to streaming-compliant DTO.
     * Handles all null values and edge cases gracefully.
     */
    private ContentItemDto convertToDto(ContentMetadata content) {
        if (content == null) {
            log.warn("Attempted to convert null ContentMetadata");
            return null;
        }
        
        try {
            // Validate required fields
            if (content.getContentId() == null || content.getTitle() == null) {
                log.error("Content missing required fields - id: {}, title: {}", 
                    content.getContentId(), content.getTitle());
                return null;
            }
            
            // Build video content with null safety
            ContentItemDto.Video video = ContentItemDto.Video.builder()
                .url(content.getStreamUrl() != null ? content.getStreamUrl() : "")
                .quality("HD")
                .videoType("MP4")
                .build();

            // Handle release date with null check
            String dateAdded = "1970-01-01T00:00:00";
            if (content.getReleaseDate() != null) {
                try {
                    dateAdded = content.getReleaseDate().format(ISO_FORMATTER);
                } catch (Exception e) {
                    log.warn("Failed to format release date for content {}: {}", 
                        content.getContentId(), e.getMessage());
                }
            }

            ContentItemDto.VideoContent videoContent = ContentItemDto.VideoContent.builder()
                .dateAdded(dateAdded)
                .videos(new ContentItemDto.Video[]{video})
                .durationSeconds(content.getDurationMinutes() != null ? content.getDurationMinutes() * 60 : 0)
                .language(content.getLanguage() != null ? content.getLanguage() : "en")
                .build();

            // Build rating with defaults
            ContentItemDto.ContentRating rating = ContentItemDto.ContentRating.builder()
                .rating(content.getRating() != null && !content.getRating().trim().isEmpty() 
                    ? content.getRating() : "NR")
                .ratingSource("MPAA")
                .build();

            // Handle release date for main field
            String releaseDate = "1970-01-01";
            if (content.getReleaseDate() != null) {
                try {
                    releaseDate = content.getReleaseDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    log.warn("Failed to format ISO date for content {}", content.getContentId());
                }
            }

            // Build main content item
            return ContentItemDto.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .longDescription(content.getLongDescription() != null ? content.getLongDescription() : "")
                .shortDescription(truncateDescription(content.getLongDescription()))
                .thumbnailUrl(content.getThumbnailUrl() != null ? content.getThumbnailUrl() : "")
                .content(videoContent)
                .genres(content.getGenre() != null && !content.getGenre().trim().isEmpty() 
                    ? new String[]{content.getGenre()} : new String[0])
                .releaseDate(releaseDate)
                .rating(rating)
                .build();
        } catch (Exception e) {
            log.error("Failed to convert content to DTO - id: {}", 
                content.getContentId(), e);
            return null;
        }
    }

    /**
     * Truncate long description to create short description (max 200 chars).
     * Handles null and empty strings gracefully.
     */
    private String truncateDescription(String longDescription) {
        if (longDescription == null || longDescription.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = longDescription.trim();
        if (trimmed.length() <= 200) {
            return trimmed;
        }
        
        // Truncate at word boundary if possible
        String truncated = trimmed.substring(0, 197);
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > 150) {
            truncated = truncated.substring(0, lastSpace);
        }
        
        return truncated + "...";
    }
}
