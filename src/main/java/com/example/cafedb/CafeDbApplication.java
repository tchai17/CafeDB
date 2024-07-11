package com.example.cafedb;

import com.example.cafedb.bot.Bot;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class CafeDbApplication {

	public static Environment env;

	public static void main(String[] args) throws TelegramApiException {

		// Load environment variables
		Dotenv dotenv = Dotenv.load();

		SpringApplication application = new SpringApplication(CafeDbApplication.class);
		application.addInitializers((applicationContext) -> {
			ConfigurableEnvironment environment = applicationContext.getEnvironment();
			environment.getSystemProperties().put("TELEGRAM_BOT_TOKEN", dotenv.get("TELEGRAM_BOT_TOKEN"));
			environment.getSystemProperties().put("GOOGLE_SPREADSHEET_ID", dotenv.get("GOOGLE_SPREADSHEET_ID"));
			environment.getSystemProperties().put("GOOGLE_CREDENTIALS_FILE", dotenv.get("GOOGLE_CREDENTIALS_FILE"));
		});
		application.run(args);
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
		botsApi.registerBot(new Bot(
				dotenv.get("TELEGRAM_BOT_TOKEN")
				));


	}
}
