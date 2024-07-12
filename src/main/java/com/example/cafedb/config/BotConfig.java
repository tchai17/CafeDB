package com.example.cafedb.config;

import com.example.cafedb.bot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Bean
    public Bot bot() {
        if (botToken == null || botToken.isEmpty()) {
            logger.error("TELEGRAM_BOT_TOKEN is not set");
        } else {
            logger.info("TELEGRAM_BOT_TOKEN is set");
        }
        return new Bot(botToken);
    }
}
