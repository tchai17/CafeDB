package com.example.cafedb.config;

import com.example.cafedb.bot.Bot;
import com.example.cafedb.googleutil.GoogleAuthorizeUtil;
import com.example.cafedb.googleutil.SheetsServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class BotConfig {

    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${GOOGLE_SPREADSHEET_ID}")
    private String spreadsheetId;

    @Value("${GOOGLE_CREDENTIALS_FILE}")
    private String googleCredentialsFile;

    private final Environment env;

    public BotConfig(Environment env) {
        this.env = env;
    }


    @Bean
    public Bot bot() {
        if ( botToken == null || botToken.isEmpty() ) {
            logger.error("TELEGRAM_BOT_TOKEN is not set");
        } else {
            logger.info("TELEGRAM_BOT_TOKEN is set: " + botToken.substring(0, 3) + "...");
        }
        return new Bot(botToken);
    }

    @Bean
    public SheetsServiceUtil sheetsServiceUtil() {
        if ( spreadsheetId == null || spreadsheetId.isEmpty() ) {
            logger.error("GOOGLE_SPREADSHEET_ID is not set");
        } else {
            logger.info("GOOGLE_SPREADSHEET_ID is set: " + spreadsheetId);
        }
        return new SheetsServiceUtil(spreadsheetId);
    }

    @Bean
    public GoogleAuthorizeUtil googleAuthorizeUtil() {
        if ( googleCredentialsFile == null || googleCredentialsFile.isEmpty() ) {
            logger.error("GOOGLE_CREDENTIALS_FILE is not set");
        } else {
            logger.info("GOOGLE_CREDENTIALS_FILE is set");
        }
        return new GoogleAuthorizeUtil(googleCredentialsFile);
    }
}
