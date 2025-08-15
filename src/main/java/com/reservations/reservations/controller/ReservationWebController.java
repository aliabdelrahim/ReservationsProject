package com.reservations.reservations.controller;

import com.reservations.reservations.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/representations")
public class ReservationWebController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping("/{id}/reserve")
    @PreAuthorize("isAuthenticated()")
    public String reserve(@PathVariable("id") Long id,
                          @RequestParam("qty") int qty,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            var r = reservationService.reserve(id, principal.getName(), qty);
            ra.addFlashAttribute("message",
                    "Réservation #" + r.getId() + " créée (" + r.getPlaces() + " place(s)).");
        } catch (ResponseStatusException ex) {
            // On redirige proprement vers /profile avec un message d'erreur
            ra.addFlashAttribute("error", ex.getReason() != null ? ex.getReason() : "Impossible de réserver.");
        }
        return "redirect:/profile"; // ou "/profil" si c’est ton chemin
    }
}

