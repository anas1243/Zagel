package com.example.anas.zagel.Models;

import java.io.Serializable;

public class User implements Serializable {
    //el line dah by3ml eh?
    public DeliveryMan deliveryMode = new DeliveryMan();
    private String id = "";
    private String name = "";
    private String phone = "";
    private String email = "";
    private String photoUrl = "";
    private String userType = "";   //c for customer, d for delivery man, b for both modes
    private String currentUserType = "";
    private int ratingsSum = 0;
    private int ratingCounter = 0;
    private String gender = "u"; //u:unknown, f:female, m:male
    private Birthday birthday = new Birthday(0, 0, 0);
    private boolean basicInfo = false;
    private int ordersCounter = 0;
    private String token="";
    private String attachedDeliveryId = "";
    private int rating = 0;

    public User() {
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRatingCounter() {
        return ratingCounter;
    }

    public void setRatingCounter(int ratingCounter) {
        this.ratingCounter = ratingCounter;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getOrdersCounter() {
        return ordersCounter;
    }

    public void setOrdersCounter(int ordersCounter) {
        this.ordersCounter = ordersCounter;
    }

    public String getCurrentUserType() {
        return currentUserType;
    }

    public void setCurrentUserType(String currentUserType) {
        this.currentUserType = currentUserType;
    }

    public int increaseCounterByOne() {
        ordersCounter++;
        return ordersCounter;
    }

    public boolean isBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(boolean basicInfo) {
        this.basicInfo = basicInfo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Birthday getBirthday() {
        return birthday;
    }

    public void setBirthday(Birthday birthday) {
        this.birthday = birthday;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getRatingsSum() {
        return ratingsSum;
    }

    public void setRatingsSum(int rating) {
        this.ratingsSum = rating;
    }

    public DeliveryMan getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(DeliveryMan deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getAttachedDeliveryId() {
        return attachedDeliveryId;
    }

    public void setAttachedDeliveryId(String attachedDeliveryId) {
        this.attachedDeliveryId = attachedDeliveryId;
    }
}