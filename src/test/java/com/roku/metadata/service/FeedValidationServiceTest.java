package com.roku.metadata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roku.metadata.dto.RokuFeedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FeedValidationService.
 */
class FeedValidationServiceTest {

    private FeedValidationService validationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validationService = new FeedValidationService(objectMapper);
    }

    @Test
    void validateFeedStructure_WithValidFeed_ShouldReturnTrue() {
        // Arrange
        RokuFeedResponse validFeed = RokuFeedResponse.builder()
            .providerName("Test Provider")
            .language("en")
            .lastUpdated("2024-01-01T00:00:00")
            .movies(Collections.emptyList())
            .series(Collections.emptyList())
            .shortFormVideos(Collections.emptyList())
            .totalCount(0)
            .build();

        // Act
        boolean result = validationService.validateFeedStructure(validFeed);

        // Assert - should pass even without schema file (permissive fallback)
        assertThat(result).isTrue();
    }

    @Test
    void validateFeedStructure_WithNullFeed_ShouldHandleGracefully() {
        // Act
        boolean result = validationService.validateFeedStructure(null);

        // Assert - should handle null gracefully
        assertThat(result).isFalse();
    }
}
