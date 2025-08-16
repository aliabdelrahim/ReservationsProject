package com.reservations.reservations.repository;

import com.reservations.reservations.model.Location;
import com.reservations.reservations.model.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    Show findBySlug(String slug);

    Show findByTitle(String title);

    Optional<Show> findByTitleIgnoreCase(String title);

    List<Show> findByLocation(Location location);

    //  Recherche par mot-clé (LIKE)
    @Query("SELECT s FROM Show s JOIN s.tags t WHERE LOWER(t.tag) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Show> findByTagKeyword(@Param("keyword") String keyword);

    // Spectacles ne contenant pas un mot-clé
    @Query("SELECT s FROM Show s WHERE :excluded NOT IN (SELECT t.tag FROM s.tags t)")
    List<Show> findByTagNot(@Param("excluded") String excluded);

    Page<Show> findByTagsTagContainingIgnoreCase(String tag, Pageable pageable);
    Page<Show> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Show> findByTitleContainingIgnoreCaseAndTagsTagContainingIgnoreCase(String title, String tag, Pageable pageable);
    Page<Show> findAll(Pageable pageable);


}
