package com.example.cafedb.googleutil;
import com.example.cafedb.models.ShopRating;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SheetsServiceUtil {
    private static final String APPLICATION_NAME = "CafeDB";
    private static HashMap<String, String> fieldToColumnMap;
    @Value("${google.spreadsheet.id}")
    private static String spreadsheetID;
    private static final String sheetName = "CafeDB_Main";

    public static Sheets.Spreadsheets getSheetsService(Credential credential) throws IOException, GeneralSecurityException {
        initializeColumnToFieldMap();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
                .spreadsheets();
    }

    public static void writeToSheet(Sheets.Spreadsheets service, ShopRating rating) throws IOException {
        writeToSheet(service, spreadsheetID, sheetName, rating);
    }

    public static void writeToSheet(Sheets.Spreadsheets service, String spreadsheetId, String sheetName, ShopRating rating) throws IOException {
        if (rating == null) {
            throw new IllegalArgumentException("Rating object cannot be null");
        }

        List<List<Object>> values = new ArrayList<>();
        values.add(Arrays.asList(
                rating.getShop().getName(),
                rating.getUsername(),
                rating.getRatingDate().toString(),
                rating.getScore(),
                rating.getAdditionalDetails()
        ));

        String range = determineRange(service, spreadsheetId, sheetName);
        ValueRange body = new ValueRange().setValues(values);
        Sheets.Spreadsheets.Values.Update request = service.values().update(spreadsheetId, range, body);
        request.setValueInputOption("RAW");
        request.execute();
    }

    public static List<List<Object>> readFromSheet(Sheets.Spreadsheets service) throws IOException {
        String range = sheetName + "!A:E"; // Adjust range based on your model
        ValueRange response = service.values().get(spreadsheetID, range).execute();
        return response.getValues();
    }



    public static List<List<Object>> getRatingsByShop(Sheets.Spreadsheets service, String shopName) throws IOException {
        List<List<Object>> allRatings = readFromSheet(service);
        return allRatings.stream()
                .filter(row -> row.get(0).equals(shopName))
                .collect(Collectors.toList());
    }

    public static List<List<Object>> getRatingsByUser(Sheets.Spreadsheets service, String username) throws IOException {
        List<List<Object>> allRatings = readFromSheet(service);
        return allRatings.stream()
                .filter(row -> row.get(1).equals(username))
                .collect(Collectors.toList());
    }

    public static double calculateAverageScore(List<List<Object>> ratings) {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToDouble(row -> Double.parseDouble(row.get(3).toString()))
                .average()
                .orElse(0.0);
    }

    private static String determineRange(Sheets.Spreadsheets service, String spreadsheetId, String sheetName) throws IOException {
        Sheets.Spreadsheets.Values.Get request = service.values().get(spreadsheetId, sheetName + "!A:A");
        ValueRange response = request.execute();
        int lastRow = response.getValues() == null ? 0 : response.getValues().size();
        return sheetName + "!A" + (lastRow + 1) + ":E" + (lastRow + 1);
    }

    private static void initializeColumnToFieldMap() {
        if (fieldToColumnMap == null) {
            fieldToColumnMap = new HashMap<>();
            fieldToColumnMap.put("shop.name", "A");
            fieldToColumnMap.put("username", "B");
            fieldToColumnMap.put("ratingDate", "C");
            fieldToColumnMap.put("score", "D");
            fieldToColumnMap.put("additionalDetails", "E");
        }
    }


}
