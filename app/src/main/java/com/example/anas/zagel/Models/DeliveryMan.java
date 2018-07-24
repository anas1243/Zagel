package com.example.anas.zagel.Models;

/**
 * Created by Eltobgy on 13-Apr-18.
 */

import java.io.Serializable;

public class DeliveryMan implements Serializable {
    private boolean isAccepted = false; //indecates if this account this accepted by our system
    private String nationalIdUrl = "";
    private String electricityResit = "";
    private String vehicle = ""; //w for walking, d for driving
    private boolean online = true;
    private boolean endTrip = false;

    public DeliveryMan() {
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getVehicle() {
        return vehicle;
    }
    //TODO add fesh w tshbeh

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public String getNationalIdUrl() {
        return nationalIdUrl;
    }

    public void setNationalIdUrl(String nationalIdUrl) {
        this.nationalIdUrl = nationalIdUrl;
    }

    public String getElectricityResit() {
        return electricityResit;
    }

    public void setElectricityResit(String electricityResit) {
        this.electricityResit = electricityResit;
    }

    public boolean isEndTrip() {
        return endTrip;
    }

    public void setEndTrip(boolean endTrip) {
        this.endTrip = endTrip;
    }
}