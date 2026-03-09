package com.roku.metadata.service;

import com.roku.metadata.dto.RokuFeedResponse;
import com.roku.metadata.entity.ContentMetadata;
import com.roku.metadata.model.MediaType;
import com.roku.metadata.repository.ContentMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RokuFeedService.
 * Tests business logic, caching behavior, and data transformation.
 */
@ExtendWith(MockitoExtension.class)
class RokuFeedServiceTest {

    @Mock
    private ContentMetadataRepository contentRepository;

    @Mock
    private FeedValidationService validationService;

    @InjectMocks
    private RokuFeedService rokuFeedService;

    private List<ContentMetadata> testContent;

    @BeforeEach
    void setUp() {
        // Create test data
        ContentMetadata movie1 = ContentMetadata.builder()
            .id(1L)
            .contentId("movie-001")
            .title("Test Movie 1")
            .longDescription("A thrilling action movie")
            .streamUrl("https://example.com/movie1.mp4")
            .thumbnailUrl("https://example.com/movie1.jpg")
            .mediaType(MediaType.MOVIE)
            .releaseDate(LocalDateTime.of(2024, 1, 1, 0, 0))
            .genre("Action")
            .language("en")
            .durationMinutes(120)
            .rating("PG-13")
            .build();

        ContentMetadata movie2 = ContentMetadata.builder()
            .id(2L)
            .contentId("movie-002")
            .title("Test Movie 2")
            .longDescription("A dramatic story")
            .streamUrl("https://example.com/movie2.mp4")
            .thumbnailUrl("https://example.com/movie2.jpg")
            .mediaType(MediaType.MOVIE)
            .releaseDate(LocalDateTime.of(2024, 2, 1, 0, 0))
            .genre("Drama")
            .language("es")
            .durationMinutes(110)
            .rating("R")
            .build();

        ContentMetadata series1 = ContentMetadata.builder()
            .id(3L)
            .contentId("series-001")
            .title("Test Series")
            .longDescription("An exciting TV series")
            .streamUrl("https://example.com/series1.mp4")
            .thumbnailUrl("https://example.com/series1.jpg")
            .mediaType(MediaType.SERIES)
            .releaseDate(LocalDateTime.of(2023, 6, 1, 0, 0))
            .genre("Drama")
            .language("en")
            .durationMinutes(480)
            .rating("TV-14")
            .build();

        testContent = Arrays.asList(movie1, movie2, series1);
    }

    @Test
    void getAllContent_ShouldReturnGroupedContent() {
        // Arrange
        when(contentRepository.findAll()).thenReturn(testContent);
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProviderName()).isNotNull();
        assertThat(response.getLastUpdated()).isNotNull();
        assertThat(response.getMovies()).hasSize(2);
        assertThat(response.getSeries()).hasSize(1);
        assertThat(response.getShortFormVideos()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(3);

        verify(contentRepository, times(1)).findAll();
        verify(validationService, times(1)).validateFeedStructure(any());
    }

    @Test
    void getContentByFilters_WithGenre_ShouldFilterCorrectly() {
        // Arrange
        List<ContentMetadata> actionMovies = Collections.singletonList(testContent.get(0));
        when(contentRepository.findByGenre("Action")).thenReturn(actionMovies);
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getContentByFilters("Action", null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMovies()).hasSize(1);
        assertThat(response.getMovies().get(0).getContentId()).isEqualTo("movie-001");
        assertThat(response.getTotalCount()).isEqualTo(1);

        verify(contentRepository, times(1)).findByGenre("Action");
    }

    @Test
    void getContentByFilters_WithLanguage_ShouldFilterCorrectly() {
        // Arrange
        List<ContentMetadata> spanishContent = Collections.singletonList(testContent.get(1));
        when(contentRepository.findByLanguage("es")).thenReturn(spanishContent);
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getContentByFilters(null, "es");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMovies()).hasSize(1);
        assertThat(response.getMovies().get(0).getContentId()).isEqualTo("movie-002");
        assertThat(response.getTotalCount()).isEqualTo(1);

        verify(contentRepository, times(1)).findByLanguage("es");
    }

    @Test
    void getContentByFilters_WithBothFilters_ShouldApplyBoth() {
        // Arrange
        when(contentRepository.findByGenreAndLanguage("Drama", "en"))
            .thenReturn(Collections.singletonList(testContent.get(2)));
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getContentByFilters("Drama", "en");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSeries()).hasSize(1);
        assertThat(response.getSeries().get(0).getContentId()).isEqualTo("series-001");

        verify(contentRepository, times(1)).findByGenreAndLanguage("Drama", "en");
    }

    @Test
    void getContentByFilters_NoFilters_ShouldReturnAll() {
        // Arrange
        when(contentRepository.findAll()).thenReturn(testContent);
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getContentByFilters(null, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(3);

        verify(contentRepository, times(1)).findAll();
    }

    @Test
    void getAllContent_EmptyDatabase_ShouldReturnEmptyFeed() {
        // Arrange
        when(contentRepository.findAll()).thenReturn(Collections.emptyList());
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMovies()).isEmpty();
        assertThat(response.getSeries()).isEmpty();
        assertThat(response.getShortFormVideos()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
    }

    @Test
    void convertToDto_ShouldMapAllFieldsCorrectly() {
        // Arrange
        when(contentRepository.findAll()).thenReturn(Collections.singletonList(testContent.get(0)));
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert
        assertThat(response.getMovies()).hasSize(1);
        var contentItem = response.getMovies().get(0);
        assertThat(contentItem.getContentId()).isEqualTo("movie-001");
        assertThat(contentItem.getTitle()).isEqualTo("Test Movie 1");
        assertThat(contentItem.getLongDescription()).isEqualTo("A thrilling action movie");
        assertThat(contentItem.getThumbnailUrl()).isEqualTo("https://example.com/movie1.jpg");
        assertThat(contentItem.getGenres()).containsExactly("Action");
        assertThat(contentItem.getRating().getRating()).isEqualTo("PG-13");
        assertThat(contentItem.getContent().getVideos()).hasSize(1);
        assertThat(contentItem.getContent().getVideos()[0].getUrl())
            .isEqualTo("https://example.com/movie1.mp4");
    }
}
