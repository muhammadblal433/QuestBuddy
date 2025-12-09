package com.questbuddy.trip;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;


/**
 * Trip class for users
 *
 * Contains start point, destination, from time, to time, optional coordinates, etc
 */
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // tripId

    @Column(nullable = false)
    private Long ownerId; // maps to who made it by Id

    @Column(nullable = false, length = 120)
    private String name; // name of trip

    @Column(length = 120)
    private String destination;

    // Optional starting point (name + coordinates)
    @Column(length = 160)
    private String startLocationName;

    // Nullable; when provided, both lat & lon should be present
    private Double startLat;   // [-90, 90]
    private Double startLon;   // [-180, 180]

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onInsert() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartLocationName() { return startLocationName; }
    public void setStartLocationName(String startLocationName) { this.startLocationName = startLocationName; }
    public Double getStartLat() { return startLat; }
    public void setStartLat(Double startLat) { this.startLat = startLat; }
    public Double getStartLon() { return startLon; }
    public void setStartLon(Double startLon) { this.startLon = startLon; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

