package com.reservations.reservations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@Entity
@Table(name = "artists")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String firstname;
    private String lastname;

    @ManyToMany(mappedBy = "artists")
    private List<Type> types = new ArrayList<>();

    public List<Type> getTypes() {
        return types;
    }

    public Artist addType(Type type) {
        if (!this.types.contains(type)) {
            this.types.add(type);
            type.addArtist(this);
        }
        return this;
    }

    public Artist removeType(Type type) {
        if (this.types.contains(type)) {
            this.types.remove(type);
            type.getArtists().remove(this);
        }
        return this;
    }
}
