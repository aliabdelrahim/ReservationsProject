package com.reservations.reservations.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "troupes")
public class Troupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60, unique = true)
    private String name;

    @Column(length = 255)
    private String logoUrl;

    @OneToMany(mappedBy = "troupe", cascade = CascadeType.ALL)
    private List<Artist> artists;
}

