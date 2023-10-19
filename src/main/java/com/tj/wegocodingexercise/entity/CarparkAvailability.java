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
}
