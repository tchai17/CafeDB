package com.example.cafedb.models;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ShopRating {
    private Shop shop;
    private String username;
    private Date ratingDate;
    private double score;
    private String additionalDetails;

    public ShopRating(Shop shop, String username, Date ratingDate, double score, String additionalDetails) {
        this.shop = shop;
        this.username = username;
        this.ratingDate = ratingDate;
        this.score = score;
        this.additionalDetails = additionalDetails;
    }
}
