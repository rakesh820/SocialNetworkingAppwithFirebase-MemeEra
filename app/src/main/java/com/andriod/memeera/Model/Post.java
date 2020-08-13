package com.andriod.memeera.Model;

public class Post {
    public String  time, date;
    private String imageurl;
    private String postid;
    private String publisher;
    private String description;

    public Post()
    {

    }

    public Post(String postid, String time, String date, String publisher,String imageurl,String description)
    {
        this.postid = postid;
        this.time = time;
        this.date = date;
        this.imageurl = imageurl;
        this.description=description;
        this.publisher= publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
