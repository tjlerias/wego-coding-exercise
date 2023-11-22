package com.tj.wegocodingexercise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.locationtech.jts.geom.Point;

@Entity
public class CarPark extends BaseEntity {

    @Id
    @Column(nullable = false, length = 4)
    private String id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;

    public CarPark() {
    }

    public CarPark(String id, String address, Point location) {
        this.id = id;
        this.address = address;
        this.location = location;
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
}
