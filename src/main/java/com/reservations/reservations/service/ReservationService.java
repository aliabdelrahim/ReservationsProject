package com.reservations.reservations.service;



import com.reservations.reservations.model.Representation;
import com.reservations.reservations.model.Reservation;
import com.reservations.reservations.model.User;
import com.reservations.reservations.repository.RepresentationRepository;
import com.reservations.reservations.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired private ReservationRepository reservationRepo;
    @Autowired private RepresentationRepository representationRepo;
    @Autowired private UserService userService;

    @Transactional
    public Reservation reserve(Long representationId, String userLogin, int qty) {
        if (qty <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantité invalide");
        }

        // Verrouille la représentation pour éviter la surréservation concurrente
        Representation rep = representationRepo.lockById(representationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Capacité robuste (si tu n'as pas encore rempli la colonne)
        int capacity = rep.getCapacity(); // ton getter peut renvoyer 0 si null
        if (capacity <= 0) capacity = 150; // fallback

        int reserved = reservationRepo.sumReservedPlaces(representationId);
        int remaining = Math.max(capacity - reserved, 0);

        if (qty > remaining) {
            // ⛔️ Erreur lisible
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pas assez de places disponibles (restantes: " + remaining + ")"
            );
        }

        User user = userService.findByLogin(userLogin);
        Reservation r = new Reservation(user, rep, qty, Reservation.Status.PENDING);
        return reservationRepo.save(r);
    }


    @Transactional
    public void markPaid(Long reservationId) {
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (r.getStatus() == Reservation.Status.CANCELED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Réservation annulée");
        }
        r.setStatus(Reservation.Status.PAID);
        reservationRepo.save(r);
    }

    @Transactional
    public void cancel(Long reservationId, String userLogin) {
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!r.getUser().getLogin().equals(userLogin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        r.setStatus(Reservation.Status.CANCELED);
        reservationRepo.save(r);
    }

    public List<Reservation> myReservations(String userLogin) {
        User me = userService.findByLogin(userLogin);
        return reservationRepo.findByUserOrderByCreatedAtDesc(me);
    }

    public int remainingPlaces(Long representationId) {
        Representation rep = representationRepo.findById(representationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return rep.getCapacity() - reservationRepo.sumReservedPlaces(representationId);
    }

    public Reservation getOwnedPendingReservation(Long reservationId, String userLogin) {
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!r.getUser().getLogin().equals(userLogin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (r.getStatus() != Reservation.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Réservation non payable");
        }
        return r;
    }
}

