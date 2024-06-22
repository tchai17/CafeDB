package com.example.cafedb.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Shop {
    private String name;
    private List<ShopRating> ratings;
    private Category category;

    public Shop(String name, Category category) {
        this.name = name;
        this.category = category;
        this.ratings = new ArrayList<>();
    }

    public enum Category {
        Cafe,
        Restaurant,
        Drinks,
        FoodCourt,
        StreetStalls
    }
}
