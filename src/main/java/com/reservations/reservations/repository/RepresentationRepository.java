package com.reservations.reservations.repository;

import com.reservations.reservations.model.Location;
import com.reservations.reservations.model.Representation;
import com.reservations.reservations.model.Show;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepresentationRepository extends CrudRepository<Representation, Long> {
    List<Representation> findByShow(Show show);
    List<Representation> findByLocation(Location location);
    List<Representation> findByWhen(LocalDateTime when);

    // 👇 Méthode qui "verrouille" la ligne pendant la transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Representation r WHERE r.id = :id")
    Optional<Representation> lockById(@Param("id") Long id);
}