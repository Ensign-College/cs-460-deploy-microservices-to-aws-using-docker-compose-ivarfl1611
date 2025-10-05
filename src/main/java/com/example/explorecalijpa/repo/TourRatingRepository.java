package com.example.explorecalijpa.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.example.explorecalijpa.recommendation.TourSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.explorecalijpa.model.TourRating;

/**
 * Tour Rating Repository Interface
 * <p>
 * Created by Mary Ellen Bowman
 */
@RepositoryRestResource(exported = false)
public interface TourRatingRepository extends JpaRepository<TourRating, Integer>, CrudRepository<TourRating, Integer> {


    /**
     * Lookup all the TourRatings for a tour.
     *
     * @param tourId is the tour Identifier
     * @return a List of any found TourRatings
     */
    List<TourRating> findByTourId(Integer tourId);

    /**
     * Lookup a TourRating by the TourId and Customer Id
     *
     * @param tourId
     * @param customerId
     * @return TourRating if found, null otherwise.
     */
    Optional<TourRating> findByTourIdAndCustomerId(Integer tourId, Integer customerId);

    @Query("""
            select tr.tour.id as tourId,
                   tr.tour.title as title,
                   avg(tr.score)  as avgScore,
                   count(tr.id)   as reviewCount
            from TourRating tr
            group by tr.tour.id, tr.tour.title
            order by avg(tr.score) desc, count(tr.id) desc, tr.tour.title asc
            """)
    List<TourSummary> findTopTours(Pageable pageable);

    @Query("""
               select tr.tour.id as tourId,
                      tr.tour.title as title,
                      avg(tr.score)  as avgScore,
                      count(tr.id)   as reviewCount
               from TourRating tr
               where tr.tour.id not in (
                   select r.tour.id from TourRating r where r.customerId = :customerId
               )
               group by tr.tour.id, tr.tour.title
               order by avg(tr.score) desc, count(tr.id) desc, tr.tour.title asc
            """)
    List<TourSummary> findRecommendedForCustomer(int customerId, org.springframework.data.domain.Pageable pageable);


}
