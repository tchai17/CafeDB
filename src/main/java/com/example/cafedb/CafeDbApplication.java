package com.example.cafedb;

import com.example.cafedb.bot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class CafeDbApplication {

    private static final Logger logger = LoggerFactory.getLogger(CafeDbApplication.class);

    public static void main(String[] args) throws TelegramApiException {

        logger.info("Application starting...");

        SpringApplication.run(CafeDbApplication.class, args);

        logger.info("Application started");

    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx, Bot bot) throws TelegramApiException {
        return args -> {
            logger.info("Registering bot...");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            logger.info("Bot registered successfully");
        };
    }
}
