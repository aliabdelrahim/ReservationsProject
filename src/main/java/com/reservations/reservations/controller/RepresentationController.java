package com.reservations.reservations.controller;

import com.reservations.reservations.model.Representation;
import com.reservations.reservations.service.RepresentationService;
import com.reservations.reservations.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@Controller
public class RepresentationController {
    @Autowired
    RepresentationService service;
    @Autowired
    private ReservationService reservationService;

    @GetMapping("/representations")
    public String index(Model model) {
        List<Representation> representations = service.getAll();

        model.addAttribute("representations", representations);
        model.addAttribute("title", "Liste des representations");

        return "representation/index";
    }

    @GetMapping("/representations/{id}")
    public String show(Model model, @PathVariable("id") String id) {
        Representation representation = service.get(id); // ton service existant

        // Calcul des places restantes
        Long repId = representation.getId();
        int remaining = reservationService.remainingPlaces(repId);

        model.addAttribute("representation", representation);
        model.addAttribute("date", representation.getWhen().toLocalDate());
        model.addAttribute("heure", representation.getWhen().toLocalTime());
        model.addAttribute("title", "Fiche d'une representation");
        model.addAttribute("remaining", remaining);

        return "representation/show";
    }

}
