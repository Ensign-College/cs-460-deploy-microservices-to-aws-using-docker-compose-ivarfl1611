package com.example.explorecalijpa.recommendation;

import com.example.explorecalijpa.repo.TourRatingRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for tour recommendation functionality.
 * Handles business logic for generating top-rated tours and customer-specific recommendations.
 *
 * Features:
 * - Top-N tour recommendations based on average rating and review count
 * - Customer-specific recommendations excluding already-rated tours
 * - Caching support with Caffeine for improved performance
 * - Deterministic sorting: avg score DESC, review count DESC, title ASC
 */
@CacheConfig(cacheNames = "topTourRecs")
@Service
public class RecommendationService {

    private final TourRatingRepository repo;

    public RecommendationService(TourRatingRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieves top-N tour recommendations based on average rating.
     *
     * Sorting criteria (deterministic):
     * 1. Average score (highest first)
     * 2. Review count (most reviewed first)
     * 3. Title (alphabetical for tie-breaking)
     *
     * @param limit maximum number of recommendations to return (1-100)
     * @return list of tour recommendations, empty list if no data available
     */
    @Cacheable(key = "'top:' + #limit")
    @Transactional(readOnly = true)
    public List<TourRecommendation> recommendTopN(int limit) {
        var page = PageRequest.of(0, limit);
        return repo.findTopTours(page).stream()
                .map(s -> new TourRecommendation(
                        s.getTourId(),
                        s.getTitle(),
                        s.getAvgScore(),
                        s.getReviewCount()
                ))
                .toList();
    }

    /**
     * Generates tour recommendations for a specific customer.
     * Excludes tours that the customer has already rated to avoid duplicate suggestions.
     *
     * @param customerId ID of the customer to generate recommendations for (must be >= 1)
     * @param limit maximum number of recommendations to return (1-100)
     * @return list of tour recommendations excluding already-rated tours, empty list if none available
     */
    @Cacheable(key = "'cust:' + #customerId + ':' + #limit")
    @Transactional(readOnly = true)
    public List<TourRecommendation> recommendForCustomer(int customerId, int limit) {
        var page = PageRequest.of(0, limit);
        return repo.findRecommendedForCustomer(customerId, page).stream()
                .map(s -> new TourRecommendation(
                        s.getTourId(),
                        s.getTitle(),
                        s.getAvgScore(),
                        s.getReviewCount()
                ))
                .toList();
    }

    /**
     * Clears all cached recommendation data.
     * Useful for testing, demos, or when fresh data is required.
     */
    @CacheEvict(allEntries = true)
    public void evictAll() {
        // Cache eviction handled by Spring's @CacheEvict annotation
        // No additional logic required - annotation clears all cached entries
    }
}
