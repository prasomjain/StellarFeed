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
     * Cache key: Dynamic based on filter parameters
     */
    @Cacheable(value = "rokuFeed", key = "'genre:' + #genre + ':language:' + #language")
    @Transactional(readOnly = true)
    public RokuFeedResponse getContentByFilters(String genre, String language) {
        log.info("Fetching content with filters - genre: {}, language: {} (cache miss)", genre, language);
        
        List<ContentMetadata> filteredContent;
        
        if (genre != null && language != null) {
            filteredContent = contentRepository.findByGenreAndLanguage(genre, language);
        } else if (genre != null) {
            filteredContent = contentRepository.findByGenre(genre);
        } else if (language != null) {
            filteredContent = contentRepository.findByLanguage(language);
        } else {
            filteredContent = contentRepository.findAll();
        }
        
        return buildFeedResponse(filteredContent);
    }

    /**
     * Build Roku feed response from content metadata list.
     */
    private RokuFeedResponse buildFeedResponse(List<ContentMetadata> contentList) {
        // Group content by media type
        List<ContentItemDto> movies = contentList.stream()
            .filter(c -> c.getMediaType() == MediaType.MOVIE)
            .map(this::convertToDto)
            .collect(Collectors.toList());

        List<ContentItemDto> series = contentList.stream()
            .filter(c -> c.getMediaType() == MediaType.SERIES)
            .map(this::convertToDto)
            .collect(Collectors.toList());

        List<ContentItemDto> shortFormVideos = contentList.stream()
            .filter(c -> c.getMediaType() == MediaType.SHORTFORM)
            .map(this::convertToDto)
            .collect(Collectors.toList());

        // Build response
        RokuFeedResponse response = RokuFeedResponse.builder()
            .providerName(providerName)
            .language("en")
            .lastUpdated(LocalDateTime.now().format(ISO_FORMATTER))
            .movies(movies)
            .series(series)
            .shortFormVideos(shortFormVideos)
            .build();

        response.calculateTotalCount();

        // Validate before returning
        validationService.validateFeedStructure(response);

        log.info("Built feed response with {} total items (movies: {}, series: {}, shortForm: {})",
            response.getTotalCount(), movies.size(), series.size(), shortFormVideos.size());

        return response;
    }

    /**
     * Convert ContentMetadata entity to Roku-compliant DTO.
     */
    private ContentItemDto convertToDto(ContentMetadata content) {
        // Build video content
        ContentItemDto.Video video = ContentItemDto.Video.builder()
            .url(content.getStreamUrl())
            .quality("HD")
            .videoType("MP4")
            .build();

        ContentItemDto.VideoContent videoContent = ContentItemDto.VideoContent.builder()
            .dateAdded(content.getReleaseDate().format(ISO_FORMATTER))
            .videos(new ContentItemDto.Video[]{video})
            .durationSeconds(content.getDurationMinutes() != null ? content.getDurationMinutes() * 60 : null)
            .language(content.getLanguage())
            .build();

        // Build rating
        ContentItemDto.ContentRating rating = ContentItemDto.ContentRating.builder()
            .rating(content.getRating() != null ? content.getRating() : "NR")
            .ratingSource("MPAA")
            .build();

        // Build main content item
        return ContentItemDto.builder()
            .contentId(content.getContentId())
            .title(content.getTitle())
            .longDescription(content.getLongDescription())
            .shortDescription(truncateDescription(content.getLongDescription()))
            .thumbnailUrl(content.getThumbnailUrl())
            .content(videoContent)
            .genres(content.getGenre() != null ? new String[]{content.getGenre()} : new String[0])
            .releaseDate(content.getReleaseDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .rating(rating)
            .build();
    }

    /**
     * Truncate long description to create short description (max 200 chars).
     */
    private String truncateDescription(String longDescription) {
        if (longDescription == null) return null;
        if (longDescription.length() <= 200) return longDescription;
        return longDescription.substring(0, 197) + "...";
    }
}
