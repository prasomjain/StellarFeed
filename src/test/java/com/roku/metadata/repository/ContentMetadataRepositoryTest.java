package com.roku.metadata.repository;

import com.roku.metadata.entity.ContentMetadata;
import com.roku.metadata.model.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ContentMetadataRepository.
 * Tests JPA queries and database interactions.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class ContentMetadataRepositoryTest {

    @Autowired
    private ContentMetadataRepository repository;

    @Test
    void findByContentId_WhenExists_ShouldReturnContent() {
        // Arrange
        ContentMetadata content = createTestContent("test-001", "Test Title", MediaType.MOVIE, "Action", "en");
        repository.save(content);

        // Act
        Optional<ContentMetadata> result = repository.findByContentId("test-001");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Title");
    }

    @Test
    void findByContentId_WhenNotExists_ShouldReturnEmpty() {
        // Act
        Optional<ContentMetadata> result = repository.findByContentId("non-existent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByGenre_ShouldReturnMatchingContent() {
        // Arrange
        ContentMetadata action1 = createTestContent("action-001", "Action Movie 1", MediaType.MOVIE, "Action", "en");
        ContentMetadata action2 = createTestContent("action-002", "Action Movie 2", MediaType.MOVIE, "Action", "en");
        ContentMetadata drama = createTestContent("drama-001", "Drama Movie", MediaType.MOVIE, "Drama", "en");
        
        repository.saveAll(List.of(action1, action2, drama));

        // Act
        List<ContentMetadata> results = repository.findByGenre("Action");

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).extracting(ContentMetadata::getGenre).containsOnly("Action");
    }

    @Test
    void findByLanguage_ShouldReturnMatchingContent() {
        // Arrange
        ContentMetadata english = createTestContent("en-001", "English Content", MediaType.MOVIE, "Action", "en");
        ContentMetadata spanish = createTestContent("es-001", "Spanish Content", MediaType.MOVIE, "Action", "es");
        
        repository.saveAll(List.of(english, spanish));

        // Act
        List<ContentMetadata> results = repository.findByLanguage("es");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLanguage()).isEqualTo("es");
    }

    @Test
    void findByMediaType_ShouldReturnMatchingContent() {
        // Arrange
        ContentMetadata movie = createTestContent("movie-001", "Movie", MediaType.MOVIE, "Action", "en");
        ContentMetadata series = createTestContent("series-001", "Series", MediaType.SERIES, "Drama", "en");
        
        repository.saveAll(List.of(movie, series));

        // Act
        List<ContentMetadata> results = repository.findByMediaType(MediaType.SERIES);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMediaType()).isEqualTo(MediaType.SERIES);
    }

    @Test
    void findByGenreAndLanguage_ShouldReturnMatchingContent() {
        // Arrange
        ContentMetadata match = createTestContent("match-001", "Match", MediaType.MOVIE, "Action", "en");
        ContentMetadata wrongGenre = createTestContent("wrong-001", "Wrong Genre", MediaType.MOVIE, "Drama", "en");
        ContentMetadata wrongLang = createTestContent("wrong-002", "Wrong Lang", MediaType.MOVIE, "Action", "es");
        
        repository.saveAll(List.of(match, wrongGenre, wrongLang));

        // Act
        List<ContentMetadata> results = repository.findByGenreAndLanguage("Action", "en");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContentId()).isEqualTo("match-001");
    }

    @Test
    void save_ShouldPersistContent() {
        // Arrange
        ContentMetadata content = createTestContent("save-001", "New Content", MediaType.MOVIE, "Action", "en");

        // Act
        ContentMetadata saved = repository.save(content);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void findAll_ShouldReturnAllContent() {
        // Arrange
        ContentMetadata content1 = createTestContent("all-001", "Content 1", MediaType.MOVIE, "Action", "en");
        ContentMetadata content2 = createTestContent("all-002", "Content 2", MediaType.SERIES, "Drama", "en");
        
        repository.saveAll(List.of(content1, content2));

        // Act
        List<ContentMetadata> results = repository.findAll();

        // Assert
        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
    }

    private ContentMetadata createTestContent(String contentId, String title, MediaType mediaType, 
                                             String genre, String language) {
        return ContentMetadata.builder()
            .contentId(contentId)
            .title(title)
            .longDescription("Test description for " + title)
            .streamUrl("https://example.com/" + contentId + ".mp4")
            .thumbnailUrl("https://example.com/" + contentId + ".jpg")
            .mediaType(mediaType)
            .releaseDate(LocalDateTime.now())
            .genre(genre)
            .language(language)
            .durationMinutes(120)
            .rating("PG-13")
            .build();
    }
}
