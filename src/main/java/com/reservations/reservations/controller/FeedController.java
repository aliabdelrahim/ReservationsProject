package com.reservations.reservations.controller;

import com.reservations.reservations.model.Representation;
import com.reservations.reservations.repository.RepresentationRepository;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.FeedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feeds")
public class FeedController {

    private final RepresentationRepository repRepo;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public FeedController(RepresentationRepository repRepo) {
        this.repRepo = repRepo;
    }

    @GetMapping(value="/representations.rss", produces="application/rss+xml")
    public ResponseEntity<String> rss() throws FeedException {
        return ResponseEntity.ok(buildFeedXml("rss_2.0"));
    }

    @GetMapping(value="/representations.atom", produces="application/atom+xml")
    public ResponseEntity<String> atom() throws FeedException {
        return ResponseEntity.ok(buildFeedXml("atom_1.0"));
    }

    @GetMapping(value="/representations.json", produces="application/json")
    public Map<String,Object> json() {
        var reps = repRepo.findTop20ByOrderByWhenDesc();
        var items = reps.stream().map(r -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("show", r.getShow().getTitle()); // si tu es dans Reservation; sinon r.getShow()
            // ⬆️ ajuste: ici on est bien sur Representation: r.getShow()
            return m;
        }).collect(Collectors.toList());
        // Correction si on est bien sur Representation :
        items = repRepo.findTop20ByOrderByWhenDesc().stream().map(r -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("show", r.getShow().getTitle());
            m.put("when", r.getWhen().toString()); // ISO
            m.put("location", r.getLocation()!=null ? r.getLocation().getDesignation() : null);
            m.put("capacity", r.getCapacity());
            m.put("url", baseUrl + "/representations/" + r.getId());
            return m;
        }).collect(Collectors.toList());

        Map<String,Object> feed = new LinkedHashMap<>();
        feed.put("title", "Dernières représentations");
        feed.put("home_page_url", baseUrl);
        feed.put("items", items);
        return feed;
    }

    private String buildFeedXml(String type) throws FeedException {
        var reps = repRepo.findTop20ByOrderByWhenDesc();

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType(type); // "rss_2.0" ou "atom_1.0"
        feed.setTitle("Dernières représentations");
        feed.setLink(baseUrl + "/representations");
        feed.setDescription("Les 20 dernières représentations publiées");

        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<SyndEntry> entries = new ArrayList<>();

        for (var r : reps) {
            SyndEntry e = new SyndEntryImpl();
            e.setTitle(r.getShow().getTitle() + " – " + r.getWhen().format(dt));
            String link = baseUrl + "/representations/" + r.getId();
            e.setLink(link);
            e.setUri(link);
            var pub = Date.from(r.getWhen().atZone(ZoneId.systemDefault()).toInstant());
            e.setPublishedDate(pub);

            SyndContent desc = new SyndContentImpl();
            desc.setType("text/html");
            String loc = r.getLocation()!=null ? r.getLocation().getDesignation() : "À déterminer";
            String cap = (r.getCapacity()!=0) ? String.valueOf(r.getCapacity()) : "—";
            desc.setValue("<p><b>Lieu :</b> " + loc + "</p><p><b>Capacité :</b> " + cap + "</p>");
            e.setDescription(desc);

            entries.add(e);
        }
        feed.setEntries(entries);

        return new SyndFeedOutput().outputString(feed);
    }
}
