package com.reservations.reservations.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "representations")
public class Representation {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Getter
    @Column(name = "`when`", nullable = false)
    private LocalDateTime when;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    @Getter
    @ManyToMany(mappedBy = "representations")
    private List<User> users = new ArrayList<>();

    @Column(nullable=false)
    private Integer capacity;

    public Representation() {}

    public Representation(Show show, LocalDateTime when, Location location) {
        this.show = show;
        this.when = when;
        this.location = location;
    }

    public Representation addUser(User user) {
        if (!this.users.contains(user)) {
            this.users.add(user);
            user.addRepresentation(this);
        }
        return this;
    }

    public Representation removeUser(User user) {
        if (this.users.contains(user)) {
            this.users.remove(user);
            user.getRepresentations().remove(this);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Representation [id=" + id + ", show=" + show + ", when=" + when + ", location=" + location + "]";
    }

    public int getCapacity() {
        return (capacity != null) ? capacity : 0;  // valeur par défaut si non initialisée
    }
}
