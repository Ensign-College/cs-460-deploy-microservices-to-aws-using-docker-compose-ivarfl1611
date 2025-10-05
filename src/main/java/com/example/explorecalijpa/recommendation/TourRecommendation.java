package com.example.explorecalijpa.recommendation;

/**
 * Data Transfer Object for tour recommendation responses.
 * Represents a tour recommendation with rating metrics.
 *
 * @param tourId unique identifier of the tour
 * @param title display name of the tour
 * @param averageScore average rating score (typically 1.0 to 5.0)
 * @param reviewCount total number of reviews for this tour
 */
public record TourRecommendation(
        Integer tourId,
        String title,
        Double averageScore,
        Long reviewCount
) {}
