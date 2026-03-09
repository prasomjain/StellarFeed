package com.roku.metadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Root DTO for Roku Search Feed JSON response.
 * 
 * Represents the complete feed structure required by Roku's Direct Publisher
 * specification, including provider information and categorized content lists.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RokuFeedResponse {

    @JsonProperty("providerName")
    private String providerName;

    @JsonProperty("language")
    private String language;

    @JsonProperty("lastUpdated")
    private String lastUpdated;

    @JsonProperty("movies")
    private List<ContentItemDto> movies;

    @JsonProperty("series")
    private List<ContentItemDto> series;

    @JsonProperty("shortFormVideos")
    private List<ContentItemDto> shortFormVideos;

    @JsonProperty("totalCount")
    private Integer totalCount;

    /**
     * Calculate total count from all content categories.
     */
    public void calculateTotalCount() {
        int count = 0;
        if (movies != null) count += movies.size();
        if (series != null) count += series.size();
        if (shortFormVideos != null) count += shortFormVideos.size();
        this.totalCount = count;
    }
}
