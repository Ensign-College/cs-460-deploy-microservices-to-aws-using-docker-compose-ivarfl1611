package com.example.explorecalijpa.recommendation;

/**
 * JPA Projection interface for tour summary data.
 * Used to efficiently retrieve aggregated tour rating statistics from the database.
 *
 * This projection allows Spring Data JPA to return only the required fields
 * rather than loading entire entity objects, improving query performance.
 */
public interface TourSummary {
    /**
     * @return unique identifier of the tour
     */
    Integer getTourId();

    /**
     * @return display name of the tour
     */
    String getTitle();

    /**
     * @return average rating score calculated from all reviews
     */
    Double getAvgScore();

    /**
     * @return total number of reviews for this tour
     */
    Long getReviewCount();
}
