package com.example.anas.zagel.Models;

import java.io.Serializable;

/**
 * Created by Eltobgy on 06-Apr-18.
 */

public class Package implements Serializable {
    private String name = "";
    private String id = "";
    private String description = "";
    private String source = "";
    private String destination = "";
    private String photoUrl = "";
    private int weight = 0;
    private String uid_sender = "";
    private String uid_receiver = "";
    private boolean breakable = false;
    private String payPoint = "s"; //s for source, d for destination and its by default at source
    private String price = "0.0";

    public Package() {
    }

    public Package(String uid_sender, String uid_receiver, String name, int weight) {
        this.name = name;
        this.weight = weight;
        this.uid_receiver = uid_receiver;
        this.uid_sender = uid_sender;
        photoUrl = "";
        description = "No Specific Description";
    }

    public Package(String packName, int weightLevel, String packDescription) {
        this.name = packName;
        this.weight = weightLevel;
        photoUrl = "";
        this.description = packDescription;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPayPoint() {
        return payPoint;
    }

    public void setPayPoint(String payPoint) {
        this.payPoint = payPoint;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public void setBreakable(boolean breakable) {
        this.breakable = breakable;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = this.photoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid_sender() {
        return uid_sender;
    }

    public void setUid_sender(String uid_sender) {
        this.uid_sender = uid_sender;
    }

    public String getUid_receiver() {
        return uid_receiver;
    }

    public void setUid_receiver(String uid_receiver) {
        this.uid_receiver = uid_receiver;
    }


}
