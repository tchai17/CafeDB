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
        botsApi.registerBot(new Bot(System.getenv("TELEGRAM_BOT_TOKEN")));
    }

}
