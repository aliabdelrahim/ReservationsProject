package com.reservations.reservations.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Utilisateur ayant effectué la réservation */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Représentation (date/lieu du spectacle) réservée */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "representation_id", nullable = false)
    private Representation representation;

    /** Nombre de places réservées */
    @NotNull
    @Min(1)
    @Column(name = "places", nullable = false)
    private Integer places;

    /** Statut de la réservation */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.PENDING;

    /** Gérés par la base (via DEFAULT CURRENT_TIMESTAMP...) */
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // --- Constructeurs ---
    public Reservation() {}

    public Reservation(User user, Representation representation, Integer places, Status status) {
        this.user = user;
        this.representation = representation;
        this.places = places;
        this.status = status != null ? status : Status.PENDING;
    }

    // --- Enum ---
    public enum Status { PENDING, PAID, CANCELED }

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Representation getRepresentation() {
        return representation;
    }

    public void setRepresentation(Representation representation) {
        this.representation = representation;
    }

    public Integer getPlaces() {
        return places;
    }

    public void setPlaces(Integer places) {
        this.places = places;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = (status != null ? status : Status.PENDING);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // --- equals / hashCode / toString ---
    // Recommandation Hibernate : égalité basée sur l'id (si assigné)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation other = (Reservation) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // stable même avant l'assignation de l'id
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        Long userId = (user != null ? user.getId() : null);
        Long repId  = (representation != null ? representation.getId() : null);
        return "Reservation{" +
                "id=" + id +
                ", userId=" + userId +
                ", representationId=" + repId +
                ", places=" + places +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
