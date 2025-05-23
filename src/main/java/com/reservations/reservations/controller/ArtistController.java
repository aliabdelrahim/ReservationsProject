package com.reservations.reservations.controller;

import java.util.List;

import com.reservations.reservations.model.Artist;
import com.reservations.reservations.model.Troupe;
import com.reservations.reservations.service.ArtistService;
import com.reservations.reservations.service.TroupeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;


@Controller
public class ArtistController {
    @Autowired
    ArtistService service;

    @Autowired
    TroupeService troupeService;

    @GetMapping("/artists")
    public String index(Model model) {
        List<Artist> artists = service.getAllArtists();

        model.addAttribute("artists", artists);
        model.addAttribute("title", "Liste des artistes");

        return "artist/index";
    }

    @GetMapping("/artists/{id}")
    public String show(Model model, @PathVariable("id") long id) {
        Artist artist = service.getArtist(id);

        model.addAttribute("artist", artist);
        model.addAttribute("title", "Fiche d'un artiste");

        List<Troupe> troupes = troupeService.getAllTroupes();
        model.addAttribute("troupes", troupes);

        return "artist/show";
    }

    @GetMapping("/artists/{id}/edit")
    public String edit(Model model, @PathVariable("id") long id, HttpServletRequest request) {
        Artist artist = service.getArtist(id);

        model.addAttribute("artist", artist);


        //Générer le lien retour pour l'annulation
        String referrer = request.getHeader("Referer");

        if(referrer!=null && !referrer.equals("")) {
            model.addAttribute("back", referrer);
        } else {
            model.addAttribute("back", "/artists/"+artist.getId());
        }

        return "artist/edit";
    }

    @PutMapping("/artists/{id}/edit")
    public String update(@Valid @ModelAttribute("artist") Artist artist, BindingResult bindingResult, @PathVariable("id") long id, Model model) {

        if (bindingResult.hasErrors()) {
            return "artist/edit";
        }

        Artist existing = service.getArtist(id);

        if(existing==null) {
            return "artist/index";
        }

        service.updateArtist(id, artist);

        return "redirect:/artists/"+artist.getId();
    }

    @GetMapping("/artists/create")
    public String create(Model model) {
        Artist artist = new Artist();

        model.addAttribute("artist", artist);

        return "artist/create";
    }

    @PostMapping("/artists/create")
    public String store(@Valid @ModelAttribute("artist") Artist artist, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "artist/create";
        }

        service.addArtist(artist);

        return "redirect:/artists/"+artist.getId();
    }

    @DeleteMapping("/artists/{id}")
    public String delete(@PathVariable("id") long id, Model model) {
        Artist existing = service.getArtist(id);

        if(existing!=null) {
            service.deleteArtist(id);
        }

        return "redirect:/artists";
    }

    @PostMapping("/artists/{id}/troupe")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateTroupe(@PathVariable("id") long id,
                               @RequestParam("troupeId") String troupeId) {

        Artist artist = service.getArtist(id);

        if ("__null__".equals(troupeId)) {
            // L'utilisateur a sélectionné "Non affilié"
            artist.setTroupe(null);
        } else {
            try {
                Long parsedId = Long.parseLong(troupeId);
                Troupe troupe = troupeService.getTroupe(parsedId);
                artist.setTroupe(troupe);
            } catch (NumberFormatException e) {
                // Cas improbable : valeur non numérique et différente de "__null__"
                artist.setTroupe(null);
            }
        }

        service.updateArtist(id, artist);
        return "redirect:/artists/" + id;
    }


}