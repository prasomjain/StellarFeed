package com.roku.metadata.controller;

import com.roku.metadata.dto.ContentItemDto;
import com.roku.metadata.dto.RokuFeedResponse;
import com.roku.metadata.service.RokuFeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RokuFeedController.
 * Tests REST endpoints, request/response handling, and HTTP behavior.
 */
@WebMvcTest(RokuFeedController.class)
class RokuFeedControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RokuFeedService rokuFeedService;

    @Test
    void getFeed_WithNoFilters_ShouldReturnAllContent() throws Exception {
        // Arrange
        RokuFeedResponse mockResponse = createMockFeedResponse();
        when(rokuFeedService.getAllContent()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/feed"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.providerName").value("StellarFeed Platform"))
            .andExpect(jsonPath("$.lastUpdated").exists())
            .andExpect(jsonPath("$.movies").isArray())
            .andExpect(jsonPath("$.series").isArray())
            .andExpect(jsonPath("$.shortFormVideos").isArray())
            .andExpect(jsonPath("$.totalCount").value(2))
            .andExpect(header().exists("Cache-Control"))
            .andExpect(header().string("Cache-Control", containsString("max-age")));
    }

    @Test
    void getFeed_WithGenreFilter_ShouldReturnFilteredContent() throws Exception {
        // Arrange
        RokuFeedResponse mockResponse = createFilteredFeedResponse();
        when(rokuFeedService.getContentByFilters(eq("Action"), any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/feed")
                .param("genre", "Action"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.movies").isArray())
            .andExpect(jsonPath("$.movies", hasSize(1)));
    }

    @Test
    void getFeed_WithLanguageFilter_ShouldReturnFilteredContent() throws Exception {
        // Arrange
        RokuFeedResponse mockResponse = createFilteredFeedResponse();
        when(rokuFeedService.getContentByFilters(any(), eq("es"))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/feed")
                .param("language", "es"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void getFeed_WithBothFilters_ShouldApplyBothFilters() throws Exception {
        // Arrange
        RokuFeedResponse mockResponse = createFilteredFeedResponse();
        when(rokuFeedService.getContentByFilters(eq("Drama"), eq("en"))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/feed")
                .param("genre", "Drama")
                .param("language", "en"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").exists());
    }

    @Test
    void health_ShouldReturnOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("stellarfeed-api"));
    }

    @Test
    void getFeed_WhenServiceThrowsException_ShouldReturn500() throws Exception {
        // Arrange
        when(rokuFeedService.getAllContent()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/feed"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void getFeed_ShouldIncludeSecurityHeaders() throws Exception {
        // Arrange
        RokuFeedResponse mockResponse = createMockFeedResponse();
        when(rokuFeedService.getAllContent()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/feed"))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().exists("X-Frame-Options"))
            .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    private RokuFeedResponse createMockFeedResponse() {
        ContentItemDto movie = ContentItemDto.builder()
            .contentId("movie-001")
            .title("Test Movie")
            .longDescription("A test movie")
            .thumbnailUrl("https://example.com/thumb.jpg")
            .build();

        ContentItemDto series = ContentItemDto.builder()
            .contentId("series-001")
            .title("Test Series")
            .longDescription("A test series")
            .thumbnailUrl("https://example.com/series.jpg")
            .build();

        RokuFeedResponse response = RokuFeedResponse.builder()
            .providerName("StellarFeed Platform")
            .language("en")
            .lastUpdated("2024-01-01T00:00:00")
            .movies(List.of(movie))
            .series(List.of(series))
            .shortFormVideos(Collections.emptyList())
            .build();
        
        response.calculateTotalCount();
        return response;
    }

    private RokuFeedResponse createFilteredFeedResponse() {
        ContentItemDto movie = ContentItemDto.builder()
            .contentId("filtered-001")
            .title("Filtered Movie")
            .longDescription("A filtered movie")
            .thumbnailUrl("https://example.com/filtered.jpg")
            .build();

        RokuFeedResponse response = RokuFeedResponse.builder()
            .providerName("StellarFeed Platform")
            .language("en")
            .lastUpdated("2024-01-01T00:00:00")
            .movies(List.of(movie))
            .series(Collections.emptyList())
            .shortFormVideos(Collections.emptyList())
            .build();
        
        response.calculateTotalCount();
        return response;
    }
}
