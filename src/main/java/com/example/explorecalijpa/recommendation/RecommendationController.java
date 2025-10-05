package com.example.explorecalijpa.recommendation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for tour recommendation endpoints.
 * Provides APIs for retrieving top-rated tours and customer-specific recommendations.
 *
 * Endpoints:
 * - GET /recommendations/top/{limit} - Top-N tour recommendations
 * - GET /recommendations/customer/{customerId}?limit={limit} - Customer-specific recommendations
 * - DELETE /recommendations/cache - Clear recommendation cache
 *
 * All endpoints return JSON arrays and handle validation errors with 400 Bad Request.
 * Empty results return [] with 200 OK status instead of errors.
 */
@RestController
@RequestMapping(path = "/recommendations")
@Validated
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    /**
     * Retrieves top-N tour recommendations ordered by rating.
     *
     * @param limit number of recommendations to return (must be between 1 and 100)
     * @return JSON array of tour recommendations with tourId, title, averageScore, reviewCount
     * @throws jakarta.validation.ConstraintViolationException if limit is out of range (returns 400)
     */
    @GetMapping("/top/{limit}")
    public List<TourRecommendation> top(
            @PathVariable @Min(1) @Max(100) int limit) {
        return service.recommendTopN(limit);
    }

    /**
     * Retrieves tour recommendations for a specific customer.
     * Excludes tours that the customer has already rated.
     *
     * @param customerId ID of the customer (must be >= 1)
     * @param limit maximum number of recommendations (1-100, defaults to 5)
     * @return JSON array of tour recommendations excluding already-rated tours
     * @throws jakarta.validation.ConstraintViolationException if parameters are invalid (returns 400)
     */
    @GetMapping("/customer/{customerId}")
    public List<TourRecommendation> forCustomer(
            @PathVariable @Min(1) int customerId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int limit) {
        return service.recommendForCustomer(customerId, limit);
    }

    /**
     * Clears all cached recommendation data.
     * Useful for testing, demos, or when fresh data is required.
     *
     * @return 204 No Content status with empty response body
     */
    @DeleteMapping("/cache")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCache() {
        service.evictAll();
    }
}
