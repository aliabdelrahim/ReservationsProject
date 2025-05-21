package com.reservations.reservations.repository;

import com.reservations.reservations.model.Troupe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TroupeRepository extends JpaRepository<Troupe, Long> {
}
