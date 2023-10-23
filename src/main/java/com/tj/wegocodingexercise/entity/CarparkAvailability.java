package com.tj.wegocodingexercise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class CarparkAvailability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "carpark_id")
    private Carpark carpark;

    @Column(name = "total_lots")
    private int totalLots;

    @Column(name = "available_lots")
    private int availableLots;

    public CarparkAvailability() {
    }

    public CarparkAvailability(Carpark carpark, int totalLots, int availableLots) {
        this.carpark = carpark;
        this.totalLots = totalLots;
        this.availableLots = availableLots;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carpark getCarpark() {
        return carpark;
    }

    public void setCarpark(Carpark carpark) {
        this.carpark = carpark;
    }

    public int getTotalLots() {
        return totalLots;
    }

    public void setTotalLots(int totalLots) {
        this.totalLots = totalLots;
    }

    public int getAvailableLots() {
        return availableLots;
    }

    public void setAvailableLots(int availableLots) {
        this.availableLots = availableLots;
    }
}
