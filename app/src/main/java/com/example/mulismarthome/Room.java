package com.example.mulismarthome;

import androidx.annotation.NonNull;

public class Room {
    private String name;
    private String imageUrl;
    private String imageUrl2;

    public Room(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Room(String name, String imageUrl, String imageUrl2){
        this.name = name;
        this.imageUrl = imageUrl;
        this.imageUrl2 = imageUrl2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }



    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
