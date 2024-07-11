package com.example.cafedb;

import com.example.cafedb.bot.Bot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class CafeDbApplication {

    public static void main(String[] args) throws TelegramApiException {
        SpringApplication.run(CafeDbApplication.class, args);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");

        if (botToken.isEmpty() || botToken.isBlank()) {
            System.out.println("botToken not loaded");

        } else {
            System.out.println(botToken.substring(0, 3));
        }
        botsApi.registerBot(new Bot(botToken));

    }

}
