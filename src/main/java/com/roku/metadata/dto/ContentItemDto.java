package com.roku.metadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.roku.metadata.model.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a single content item in Roku's Direct Publisher format.
 * 
 * This class maps internal ContentMetadata to Roku-compliant JSON structure
 * with exact field naming conventions required by Roku devices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentItemDto {

    @JsonProperty("id")
    private String contentId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("longDescription")
    private String longDescription;

    @JsonProperty("thumbnail")
    private String thumbnailUrl;

    @JsonProperty("shortDescription")
    private String shortDescription;

    @JsonProperty("content")
    private VideoContent content;

    @JsonProperty("genres")
    private String[] genres;

    @JsonProperty("releaseDate")
    private String releaseDate;

    @JsonProperty("rating")
    private ContentRating rating;

    /**
     * Nested class for video content details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoContent {
        @JsonProperty("dateAdded")
        private String dateAdded;

        @JsonProperty("videos")
        private Video[] videos;

        @JsonProperty("duration")
        private Integer durationSeconds;

        @JsonProperty("language")
        private String language;
    }

    /**
     * Nested class for video stream information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Video {
        @JsonProperty("url")
        private String url;

        @JsonProperty("quality")
        private String quality;

        @JsonProperty("videoType")
        private String videoType;
    }

    /**
     * Nested class for content rating information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentRating {
        @JsonProperty("rating")
        private String rating;

        @JsonProperty("ratingSource")
        private String ratingSource;
    }
}
