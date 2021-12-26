package com.byteme.baatcheet.modal;

public class User {
    private String name;
    private String uid;
    private String phoneNumber;
    private String profileImage;

    public User(String name, String uid, String phoneNumber, String profileImage) {
        this.name = name;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
