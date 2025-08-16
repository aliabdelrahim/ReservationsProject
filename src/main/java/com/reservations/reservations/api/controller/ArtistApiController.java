package com.reservations.reservations.api.controller;

import com.reservations.reservations.model.Artist;
import com.reservations.reservations.repository.ArtistRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ArtistApiController {

    private final ArtistRepository repository;

    public ArtistApiController(ArtistRepository repository) {
        this.repository = repository;
    }

    // ----------------------------
    // LECTURE (public)
    // ----------------------------

    // GET /api/artists : liste (DTO sans associations)
    @GetMapping("/artists")
    public List<ArtistDto> all() {
        Iterable<Artist> it = repository.findAll();
        List<ArtistDto> out = new ArrayList<>();
        for (Artist a : it) {
            out.add(ArtistDto.from(a));
        }
        return out;
    }

    // GET /api/artists/{id} : détail
    @GetMapping("/artists/{id}")
    public ArtistDto one(@PathVariable Long id) {
        Artist a = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
        return ArtistDto.from(a);
    }

    // ----------------------------
    // ÉCRITURE (ADMIN)
    // ----------------------------

    // POST /api/admin/artists : créer
    @PostMapping("/admin/artists")
    @PreAuthorize("hasRole('ADMIN')")
    public ArtistDto create(@RequestBody @Valid UpsertArtistDto dto) {
        Artist a = new Artist();
        a.setFirstname(dto.firstname());
        a.setLastname(dto.lastname());
        Artist saved = repository.save(a);
        return ArtistDto.from(saved);
    }

    // PUT /api/admin/artists/{id} : mettre à jour (404 si introuvable)
    @PutMapping("/admin/artists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ArtistDto update(@RequestBody @Valid UpsertArtistDto dto, @PathVariable Long id) {
        Artist a = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
        a.setFirstname(dto.firstname());
        a.setLastname(dto.lastname());
        Artist saved = repository.save(a);
        return ArtistDto.from(saved);
    }

    // DELETE /api/admin/artists/{id} : supprimer
    @DeleteMapping("/admin/artists/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found");
        }
        repository.deleteById(id);
    }

    // ============================
    // DTOs (pour éviter la récursion JSON)
    // ============================

    // Réponse JSON
    public static record ArtistDto(Long id, String firstname, String lastname) {
        public static ArtistDto from(Artist a) {
            return new ArtistDto(a.getId(), a.getFirstname(), a.getLastname());
        }
    }

    // Payload création/màj (pas de slug ici)
    public static record UpsertArtistDto(
            @NotBlank String firstname,
            @NotBlank String lastname
    ) {}
}
