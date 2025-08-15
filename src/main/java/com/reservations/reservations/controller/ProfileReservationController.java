package com.reservations.reservations.controller;

import com.reservations.reservations.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileReservationController {
    @Autowired
    private ReservationService reservationService;

    @GetMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    public String myReservations(Model model, Principal principal){
        model.addAttribute("reservations", reservationService.myReservations(principal.getName()));
        return "profile";
    }

    @PostMapping("/reservations/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        reservationService.cancel(id, principal.getName());
        ra.addFlashAttribute("message", "Réservation annulée.");
        return "redirect:/profile";     // <-- redirection vers /profile
    }

    @PostMapping("/reservations/{id}/paid")
    @PreAuthorize("hasRole('ADMIN')") // ou via webhook public non authentifié
    public String markPaid(@PathVariable Long id, RedirectAttributes ra) {
        reservationService.markPaid(id);
        ra.addFlashAttribute("message", "Paiement confirmé.");
        return "redirect:/profile";     // <-- redirection vers /profile
    }

}

