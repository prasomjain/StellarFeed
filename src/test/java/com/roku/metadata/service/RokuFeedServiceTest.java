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

    @Test
    void getAllContent_WithNullReleaseDate_ShouldUseDefaultDate() {
        // Arrange: Create content with null release date
        ContentMetadata contentWithNullDate = ContentMetadata.builder()
            .id(10L)
            .contentId("null-date-001")
            .title("Content with Null Date")
            .longDescription("Test content")
            .streamUrl("https://example.com/test.mp4")
            .thumbnailUrl("https://example.com/test.jpg")
            .mediaType(MediaType.MOVIE)
            .releaseDate(null)  // Null release date!
            .genre("Action")
            .language("en")
            .durationMinutes(90)
            .rating("PG")
            .build();

        when(contentRepository.findAll()).thenReturn(Collections.singletonList(contentWithNullDate));
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert: Should not throw NPE and should use default date
        assertThat(response).isNotNull();
        assertThat(response.getMovies()).hasSize(1);
        assertThat(response.getMovies().get(0).getReleaseDate()).isEqualTo("1970-01-01");
        assertThat(response.getMovies().get(0).getContent().getDateAdded()).isEqualTo("1970-01-01T00:00:00");
    }

    @Test
    void getAllContent_WithNullOptionalFields_ShouldUseDefaults() {
        // Arrange: Create content with null optional fields
        ContentMetadata sparseContent = ContentMetadata.builder()
            .id(11L)
            .contentId("sparse-001")
            .title("Sparse Content")
            .longDescription("Minimal metadata")
            .streamUrl("https://example.com/sparse.mp4")
            .thumbnailUrl("https://example.com/sparse.jpg")
            .mediaType(MediaType.MOVIE)
            .releaseDate(LocalDateTime.now())
            .genre(null)  // Null genre
            .language(null)  // Null language
            .durationMinutes(null)  // Null duration
            .rating(null)  // Null rating
            .build();

        when(contentRepository.findAll()).thenReturn(Collections.singletonList(sparseContent));
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert: Should handle nulls gracefully
        assertThat(response).isNotNull();
        assertThat(response.getMovies()).hasSize(1);
        var movie = response.getMovies().get(0);
        assertThat(movie.getGenres()).isEmpty();  // Empty array for null genre
        assertThat(movie.getContent().getLanguage()).isEqualTo("en");  // Default language
        assertThat(movie.getContent().getDurationSeconds()).isEqualTo(0);  // Default duration
        assertThat(movie.getRating().getRating()).isEqualTo("NR");  // Default rating
    }

    @Test
    void getAllContent_WithNullContentList_ShouldReturnEmptyFeed() {
        // Arrange: Repository returns null
        when(contentRepository.findAll()).thenReturn(null);
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert: Should handle null gracefully
        assertThat(response).isNotNull();
        assertThat(response.getMovies()).isEmpty();
        assertThat(response.getSeries()).isEmpty();
        assertThat(response.getShortFormVideos()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
    }

    @Test
    void getContentByFilters_WithEmptyResult_ShouldReturnEmptyFeed() {
        // Arrange
        when(contentRepository.findByGenre("NonExistent")).thenReturn(Collections.emptyList());
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getContentByFilters("NonExistent", null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(0);
    }

    @Test
    void getAllContent_WithValidationFailure_ShouldStillReturnResponse() {
        // Arrange
        when(contentRepository.findAll()).thenReturn(testContent);
        when(validationService.validateFeedStructure(any())).thenReturn(false);

        // Act: Validation fails but should not throw exception
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert: Response should still be returned
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(3);
    }

    @Test
    void getAllContent_WithLongDescription_ShouldTruncateShortDescription() {
        // Arrange: Create content with very long description
        String longDescription = "A".repeat(300);  // 300 characters
        ContentMetadata longDescContent = ContentMetadata.builder()
            .id(12L)
            .contentId("long-desc-001")
            .title("Long Description Content")
            .longDescription(longDescription)
            .streamUrl("https://example.com/long.mp4")
            .thumbnailUrl("https://example.com/long.jpg")
            .mediaType(MediaType.MOVIE)
            .releaseDate(LocalDateTime.now())
            .genre("Drama")
            .language("en")
            .durationMinutes(100)
            .rating("PG")
            .build();

        when(contentRepository.findAll()).thenReturn(Collections.singletonList(longDescContent));
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert: Short description should be truncated with ellipsis
        assertThat(response.getMovies()).hasSize(1);
        String shortDesc = response.getMovies().get(0).getShortDescription();
        assertThat(shortDesc.length()).isLessThanOrEqualTo(200);
        assertThat(shortDesc).endsWith("...");
    }

    @Test
    void getAllContent_WithEmptyStringOptionalFields_ShouldHandleGracefully() {
        // Arrange: Create content with empty strings instead of nulls
        ContentMetadata emptyFieldContent = ContentMetadata.builder()
            .id(13L)
            .contentId("empty-001")
            .title("Empty Fields Content")
            .longDescription("Test")
            .streamUrl("https://example.com/empty.mp4")
            .thumbnailUrl("https://example.com/empty.jpg")
            .mediaType(MediaType.MOVIE)
            .releaseDate(LocalDateTime.now())
            .genre("")  // Empty string
            .language("")  // Empty string
            .durationMinutes(100)
            .rating("")  // Empty string
            .build();

        when(contentRepository.findAll()).thenReturn(Collections.singletonList(emptyFieldContent));
        when(validationService.validateFeedStructure(any())).thenReturn(true);

        // Act
        RokuFeedResponse response = rokuFeedService.getAllContent();

        // Assert: Should handle empty strings
        assertThat(response).isNotNull();
        assertThat(response.getMovies()).hasSize(1);
        var movie = response.getMovies().get(0);
        assertThat(movie.getGenres()).isEmpty();  // Empty genre becomes empty array
        assertThat(movie.getRating().getRating()).isEqualTo("NR");  // Empty rating becomes "NR"
    }
}

