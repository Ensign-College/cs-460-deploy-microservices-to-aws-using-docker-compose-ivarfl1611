package com.example.explorecalijpa.recommendation;

import com.example.explorecalijpa.repo.TourRatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecommendationService
 * Tests service layer logic including mapping, business rules, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private TourRatingRepository tourRatingRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private List<TourSummary> mockTourSummaries;

    @BeforeEach
    void setUp() {
        // Create mock TourSummary objects with proper ordering
        // Sorted by: avg score DESC, review count DESC, title ASC
        TourSummary tour1 = createMockTourSummary(1, "Amazing Tour", 4.8, 150L);
        TourSummary tour2 = createMockTourSummary(2, "Great Adventure", 4.7, 100L);
        TourSummary tour3 = createMockTourSummary(3, "Good Experience", 4.5, 80L);

        mockTourSummaries = Arrays.asList(tour1, tour2, tour3);
    }

    @Test
    void recommendTopN_ShouldReturnMappedRecommendations() {
        // Given
        int limit = 5;
        when(tourRatingRepository.findTopTours(any(Pageable.class)))
            .thenReturn(mockTourSummaries);

        // When
        List<TourRecommendation> result = recommendationService.recommendTopN(limit);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify proper mapping and ordering
        TourRecommendation first = result.get(0);
        assertEquals(1, first.tourId());
        assertEquals("Amazing Tour", first.title());
        assertEquals(4.8, first.averageScore());
        assertEquals(150L, first.reviewCount());

        // Verify second item has lower score
        TourRecommendation second = result.get(1);
        assertEquals(4.7, second.averageScore());
        assertTrue(first.averageScore() > second.averageScore());

        // Verify repository called with correct pagination
        verify(tourRatingRepository).findTopTours(PageRequest.of(0, limit));
    }

    @Test
    void recommendTopN_ShouldReturnEmptyListWhenNoData() {
        // Given
        int limit = 5;
        when(tourRatingRepository.findTopTours(any(Pageable.class)))
            .thenReturn(Collections.emptyList());

        // When
        List<TourRecommendation> result = recommendationService.recommendTopN(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tourRatingRepository).findTopTours(PageRequest.of(0, limit));
    }

    @Test
    void recommendForCustomer_ShouldReturnMappedRecommendations() {
        // Given
        int customerId = 123;
        int limit = 3;
        when(tourRatingRepository.findRecommendedForCustomer(eq(customerId), any(Pageable.class)))
            .thenReturn(mockTourSummaries);

        // When
        List<TourRecommendation> result = recommendationService.recommendForCustomer(customerId, limit);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify mapping is correct
        TourRecommendation first = result.get(0);
        assertEquals(1, first.tourId());
        assertEquals("Amazing Tour", first.title());
        assertEquals(4.8, first.averageScore());
        assertEquals(150L, first.reviewCount());

        // Verify repository called with correct parameters
        verify(tourRatingRepository).findRecommendedForCustomer(customerId, PageRequest.of(0, limit));
    }

    @Test
    void recommendForCustomer_ShouldReturnEmptyListWhenNoRecommendations() {
        // Given - customer has rated all tours
        int customerId = 456;
        int limit = 5;
        when(tourRatingRepository.findRecommendedForCustomer(eq(customerId), any(Pageable.class)))
            .thenReturn(Collections.emptyList());

        // When
        List<TourRecommendation> result = recommendationService.recommendForCustomer(customerId, limit);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tourRatingRepository).findRecommendedForCustomer(customerId, PageRequest.of(0, limit));
    }

    @Test
    void recommendTopN_ShouldHandleEdgeCaseLimits() {
        // Given
        when(tourRatingRepository.findTopTours(any(Pageable.class)))
            .thenReturn(mockTourSummaries);

        // Test minimum limit
        List<TourRecommendation> result1 = recommendationService.recommendTopN(1);
        verify(tourRatingRepository).findTopTours(PageRequest.of(0, 1));

        // Test maximum limit
        List<TourRecommendation> result100 = recommendationService.recommendTopN(100);
        verify(tourRatingRepository).findTopTours(PageRequest.of(0, 100));
    }

    @Test
    void evictAll_ShouldCompleteWithoutError() {
        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> recommendationService.evictAll());
    }

    @Test
    void recommendTopN_ShouldPreserveOrderFromRepository() {
        // Given - Tours with same score but different review counts
        TourSummary tour1 = createMockTourSummary(1, "B Tour", 4.5, 200L);
        TourSummary tour2 = createMockTourSummary(2, "A Tour", 4.5, 100L);
        List<TourSummary> orderedTours = Arrays.asList(tour1, tour2);

        when(tourRatingRepository.findTopTours(any(Pageable.class)))
            .thenReturn(orderedTours);

        // When
        List<TourRecommendation> result = recommendationService.recommendTopN(5);

        // Then - should preserve repository ordering
        assertEquals(2, result.size());
        assertEquals("B Tour", result.get(0).title()); // Higher review count first
        assertEquals("A Tour", result.get(1).title());
    }

    /**
     * Helper method to create mock TourSummary objects for testing
     */
    private TourSummary createMockTourSummary(Integer tourId, String title, Double avgScore, Long reviewCount) {
        return new TourSummary() {
            @Override
            public Integer getTourId() { return tourId; }

            @Override
            public String getTitle() { return title; }

            @Override
            public Double getAvgScore() { return avgScore; }

            @Override
            public Long getReviewCount() { return reviewCount; }
        };
    }
}
