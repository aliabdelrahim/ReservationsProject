package com.reservations.reservations.controller;

import com.reservations.reservations.model.Show;
import com.reservations.reservations.model.Artist;
import com.reservations.reservations.model.ArtistType;
import com.reservations.reservations.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;

@Controller
public class ShowController {

    @Autowired
    private ShowService service;

    // Méthode pour afficher la liste des spectacles
    @GetMapping("/shows")
    public String index(Model model) {
        List<Show> shows = service.getAllShows(); // Appel à getAll()

        model.addAttribute("shows", shows);
        model.addAttribute("title", "Liste des spectacles");

        return "show/index";
    }

    // Méthode pour afficher un spectacle en particulier
    @GetMapping("/shows/{id}")
    public String show(Model model, @PathVariable("id") String id) {
        // On récupère l'objet Show, et on vérifie si il existe
        Show show = (Show) service.getShowById(Long.valueOf(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spectacle non trouvé"));
        //Récupérer les artistes du spectacle et les grouper par type
        Map<String,ArrayList<Artist>> collaborateurs = new TreeMap<>();

        for(ArtistType at : show.getArtistTypes()) {
            String type = at.getType().getType();

            if(collaborateurs.get(type) == null) {
                collaborateurs.put(type, new ArrayList<>());
            }

            collaborateurs.get(type).add(at.getArtist());
        }

        model.addAttribute("collaborateurs", collaborateurs);
        model.addAttribute("show", show);
        model.addAttribute("title", "Fiche d'un spectacle");

        return "show/show";
    }
}
