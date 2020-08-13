package com.andriod.memeera.Model;

public class Chat {
    String message,receiver,timeStamp,sender;
    boolean isSeen;
    public Chat() {
    }

    public Chat(String message, String receiver, String timeStamp, String sender, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.timeStamp = timeStamp;
        this.sender = sender;
        this.isSeen = isSeen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
