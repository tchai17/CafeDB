package com.example.cafedb.googleutil;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleAuthorizeUtil {

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private static String credentials;

    public GoogleAuthorizeUtil(@Value("${GOOGLE_CREDENTIALS_FILE}") String credentialsFile) {
        credentials = credentialsFile;
    }

    public static Credential authorize() throws IOException, GeneralSecurityException {
        // Load Google credentials from environment variable
        String credentialsJson = System.getenv("GOOGLE_CREDENTIALS_FILE");

        if ( credentialsJson.isEmpty() || credentialsJson == null ) {
            throw new IOException("Google credentials file not found.");
        }
        InputStream in = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));

        // Build Google Credential object
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return GoogleCredential.fromStream(in, httpTransport, jsonFactory)
                .createScoped(SCOPES);
    }
}
