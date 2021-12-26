package com.byteme.baatcheet.modal;

public class Message {
    private String message,senderId,image;
    private String timeStamp;
    private boolean isseen;

    public Message(String message, String senderId, String timeStamp, boolean isseen, String image) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.isseen = isseen;
        this.image = image;
    }

    public Message() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}


