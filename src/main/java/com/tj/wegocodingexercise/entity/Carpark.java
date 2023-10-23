package com.tj.wegocodingexercise.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
public class Carpark extends BaseEntity {

    @Id
    @Column(nullable = false, length = 4)
    private String id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @OneToOne(mappedBy = "carpark", cascade = CascadeType.ALL)
    private CarparkAvailability availability;

    public Carpark() {
    }

    public Carpark(String id, String address, Point location) {
        this.id = id;
        this.address = address;
        this.location = location;
    }

    public Carpark(
        String id,
        String address,
        Point location,
        int totalLots,
        int availableLots,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.address = address;
        this.location = location;
        this.availability = new CarparkAvailability(this, totalLots, availableLots);
        this.availability.setUpdatedAt(updatedAt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public CarparkAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(CarparkAvailability availability) {
        this.availability = availability;
    }
}
