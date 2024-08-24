package com.example.cafedb.service;

import com.example.cafedb.googleutil.GoogleAuthorizeUtil;
import com.example.cafedb.googleutil.SheetsServiceUtil;
import com.example.cafedb.models.Shop;
import com.example.cafedb.models.ShopRating;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class SpreadsheetAccessService {

    public SpreadsheetAccessService() {
    }

    public void saveRating(ShopRating rating, Shop shop) throws IOException, GeneralSecurityException {
        Credential credential = GoogleAuthorizeUtil.authorize();
        Sheets.Spreadsheets spreadsheets = SheetsServiceUtil.getSheetsService(credential);
        List<List<Object>> shopRatings = SheetsServiceUtil.getRatingsByShop(spreadsheets, shop.getName());

        SheetsServiceUtil.writeToSheet(spreadsheets, rating);
    }

    public String searchShop(String shopName) throws IOException, GeneralSecurityException {
        Credential credential = GoogleAuthorizeUtil.authorize();
        Sheets.Spreadsheets spreadsheets = SheetsServiceUtil.getSheetsService(credential);
        List<List<Object>> shopRatings = SheetsServiceUtil.getRatingsByShop(spreadsheets, shopName);

        if ( shopRatings.isEmpty() ) {
            return "No ratings found for shop: " + shopName;
        }

        double averageScore = SheetsServiceUtil.calculateAverageScore(shopRatings);
        StringBuilder ratingsText = new StringBuilder("Ratings for " + shopName + " (Average Score: " + averageScore + "):\n\n");
        for ( List<Object> row : shopRatings ) {
            ratingsText.append("User: ").append(row.get(1)).append("\n")
                    .append("Date: ").append(row.get(2)).append("\n")
                    .append("Score: ").append(row.get(3)).append("\n")
                    .append("Details: ").append(row.get(4)).append("\n\n");
        }

        return ratingsText.toString();
    }

    public String viewUserRatings(String username) throws IOException, GeneralSecurityException {
        Credential credential = GoogleAuthorizeUtil.authorize();
        Sheets.Spreadsheets spreadsheets = SheetsServiceUtil.getSheetsService(credential);
        List<List<Object>> userRatings = SheetsServiceUtil.getRatingsByUser(spreadsheets, username);

        if ( userRatings.isEmpty() ) {
            return "No ratings found for user: " + username;
        }

        StringBuilder ratingsText = new StringBuilder("Ratings submitted by " + username + ":\n\n");
        for ( List<Object> row : userRatings ) {
            ratingsText.append("Shop: ").append(row.get(0)).append("\n")
                    .append("Date: ").append(row.get(2)).append("\n")
                    .append("Score: ").append(row.get(3)).append("\n")
                    .append("Details: ").append(row.get(4)).append("\n\n");
        }

        return ratingsText.toString();
    }
}
