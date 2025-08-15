package com.reservations.reservations.repository;

import com.reservations.reservations.model.Reservation;
import com.reservations.reservations.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
    SELECT COALESCE(SUM(r.places),0)
    FROM Reservation r
    WHERE r.representation.id = :repId AND r.status <> 'CANCELED'
  """)
    int sumReservedPlaces(@Param("repId") Long representationId);

    List<Reservation> findByUserOrderByCreatedAtDesc(User user);
}
