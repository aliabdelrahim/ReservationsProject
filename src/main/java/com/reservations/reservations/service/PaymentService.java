package com.reservations.reservations.service;


import com.reservations.reservations.model.Reservation;
import com.reservations.reservations.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PaymentService {

    public long amountInCents(Reservation r) {
        var show = r.getRepresentation().getShow();
        BigDecimal price = BigDecimal.valueOf(show.getPrice());
        if (price == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prix du spectacle indisponible");
        }
        try {
            // EUR cents = euros * 100
            return price.movePointRight(2).longValueExact() * r.getPlaces();
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prix invalide");
        }
    }
}

