package com.example.explorecalijpa.recommendation;

public record TourRecommendation(
        Integer tourId,
        String title,
        Double averageScore,
        Long reviewCount
) {}
