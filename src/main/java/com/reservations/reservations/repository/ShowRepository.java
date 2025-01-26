package com.reservations.reservations.repository;

import com.reservations.reservations.model.Location;
import com.reservations.reservations.model.Show;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ShowRepository extends CrudRepository<Show, Long> {
    Show findBySlug(String slug);
    Show findByTitle(String title);
    List<Show> findByLocation(Location location);
}
