package com.estimote.examples.demos;

public class Exponat {
    int id;
    String name;
    String image;
    String beaconMac;
    String trackingData;
    String target;
    String type;
    String model;


    public Exponat(){}

    public Exponat(int id, String name, String image, String beaconMac, String trackingData, String target, String type, String model) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.beaconMac = beaconMac;
        this.trackingData = trackingData;
        this.target = target;
        this.type = type;
        this.model = model;
    }

    public Exponat(String name, String image, String beaconMac, String trackingData, String target, String type, String model) {
        this.name = name;
        this.image = image;
        this.beaconMac = beaconMac;
        this.trackingData = trackingData;
        this.target = target;
        this.type = type;
        this.model = model;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBeaconMac() {
        return beaconMac;
    }

    public void setBeaconMac(String beaconMac) {
        this.beaconMac = beaconMac;
    }

    public String getTrackingData() {
        return trackingData;
    }

    public void setTrackingData(String trackingData) {
        this.trackingData = trackingData;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}