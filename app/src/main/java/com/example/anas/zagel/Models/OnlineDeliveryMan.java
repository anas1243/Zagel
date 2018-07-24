package com.example.anas.zagel.Models;

/**
 * Created by Toka on 2018-05-02.
 */

public class OnlineDeliveryMan {

    private Location location = new Location("0", "0");
    private String id = "";
    private String currentPackageID = "";
    private boolean acceptedOrder = false;
    private double kilometers = 0.0;

    public double getKilometers() {
        return kilometers;
    }

    public void setKilometers(double kilometers) {
        this.kilometers = kilometers;
    }

    private boolean endTrip = false;
    public OnlineDeliveryMan() {
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAcceptedOrder() {
        return acceptedOrder;
    }

    public void setAcceptedOrder(boolean acceptedOrder) {
        this.acceptedOrder = acceptedOrder;
    }

    public String getCurrentPackageID() {
        return currentPackageID;
    }

    public void setCurrentPackageID(String currentPackageID) {
        this.currentPackageID = currentPackageID;
    }

    public boolean isEndTrip() {
        return endTrip;
    }

    public void setEndTrip(boolean endTrip) {
        this.endTrip = endTrip;
    }
}
