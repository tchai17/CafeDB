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
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;


public class SheetsServiceUtil {
    private static final String APPLICATION_NAME = "CafeDB";
    private static HashMap<String, String> fieldToColumnMap;

    @Value("${GOOGLE_SPREADSHEET_ID}")
    private static String spreadsheetID;
    private final static String sheetName = "CafeDB_Main";


    public static Sheets.Spreadsheets getSheetsService(Credential credential) throws IOException, GeneralSecurityException {
        initializeColumnToFieldMap();

        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
                .spreadsheets();
    }

    public SheetsServiceUtil(@Value("${GOOGLE_SPREADSHEET_ID}") String sheetID) {
        setSpreadsheetID(sheetID);
    }

    @Value("${GOOGLE_SPREADSHEET_ID}")
    public void setSpreadsheetID(String sheetID) {
        spreadsheetID = sheetID;
    }

    public static void writeToSheet(Sheets.Spreadsheets service, ShopRating rating) throws IOException {
        writeToSheet(service, spreadsheetID, sheetName, rating);
    }

    public static void writeToSheet(Sheets.Spreadsheets service, String spreadsheetId, String sheetName, ShopRating rating) throws IOException {
        if ( rating == null ) {
            throw new IllegalArgumentException("Rating object cannot be null");
        }

        if ( rating.getShop().getName() == null || rating.getUsername() == null || rating.getRatingDate() == null || rating.getAdditionalDetails() == null ) {
            throw new IllegalArgumentException("Rating fields cannot be null");
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
        if ( service == null || spreadsheetID == null ) {
            throw new IllegalArgumentException("Service and spreadsheetID cannot be null");
        }

        String range = sheetName + "!A:E";
        ValueRange response = null;
        try {
            response = service.values().get(spreadsheetID, range).execute();
        } catch ( IOException e ) {
            System.err.println("An error occurred while reading from the sheet: " + e.getMessage());
            // Handle the exception or throw a custom exception
        }

        return response != null && response.getValues() != null ? response.getValues() : new ArrayList<>();
    }


    public static List<List<Object>> getRatingsByShop(Sheets.Spreadsheets service, String shopName) throws IOException {
        if ( service == null || shopName == null ) {
            throw new IllegalArgumentException("Service and shopName cannot be null");
        }

        List<List<Object>> allRatings = readFromSheet(service);
        List<List<Object>> cachedRatings = new ArrayList<>(allRatings);
        return cachedRatings.stream()
                .filter(row -> Objects.equals(row.get(0), shopName))
                .collect(Collectors.toList());
    }

    public static List<List<Object>> getRatingsByUser(Sheets.Spreadsheets service, String username) throws IOException {
        if ( service == null || username == null ) {
            throw new IllegalArgumentException("Service and username cannot be null");
        }

        List<List<Object>> allRatings = readFromSheet(service);
        List<List<Object>> cachedRatings = new ArrayList<>(allRatings);

        return cachedRatings.stream()
                .filter(row -> Objects.equals(row.get(1), username))
                .collect(Collectors.toList());
    }

    public static double calculateAverageScore(List<List<Object>> ratings) {
        if ( ratings.isEmpty() ) {
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
        if ( fieldToColumnMap == null ) {
            fieldToColumnMap = new HashMap<>();
            fieldToColumnMap.put("shop.name", "A");
            fieldToColumnMap.put("username", "B");
            fieldToColumnMap.put("ratingDate", "C");
            fieldToColumnMap.put("score", "D");
            fieldToColumnMap.put("additionalDetails", "E");
        }
    }


}
