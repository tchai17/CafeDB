package com.example.cafedb.bot;

import com.example.cafedb.googleutil.GoogleAuthorizeUtil;
import com.example.cafedb.googleutil.SheetsServiceUtil;
import com.example.cafedb.models.Shop;
import com.example.cafedb.models.ShopRating;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {

    private enum State {
        NONE,
        AWAITING_SHOP_NAME,
        AWAITING_CATEGORY,
        AWAITING_RATING,
        AWAITING_DETAILS,
        AWAITING_SHOP_SEARCH,
        AWAITING_USER_RATINGS
    }

    private State currentState = State.NONE;
    private Shop currentShop;
    private ShopRating currentRating;

    @Value("${telegram.bot.token}")
    private String botToken;



    public Bot(@Value("${TELEGRAM_BOT_TOKEN}") String botToken) {
        this.botToken = botToken;

    }


    @Override
    public String getBotUsername() {
        return "cafedb_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Value("${telegram.bot.token}")
    public void setBotToken(String token) {
        this.botToken = token;
    }


    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                System.out.println("Received message:" + update.getMessage().getText());
                handleTextMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            // Log the exception for error management
            e.printStackTrace();
        }
    }

    void handleTextMessage(Message message) {
        String text = message.getText();
        Long chatId = message.getChatId();

        if (message.isCommand()) {
            handleCommand(message);
        } else {
            handleUserInput(message, chatId);
        }

    }

    private void handleCommand(Message message) {
        String text = message.getText();
        Long chatId = message.getChatId();

        switch (text) {
            case "/start" -> {
                String welcome = "Welcome to CafeDB! To start, please submit /menu to see all actions.";
                sendMessage(chatId, welcome);
            }
            case "/menu" -> sendMainMenu(chatId);
            case "/stop" -> {
                currentState = State.NONE;
                String stopMessage = "Current action terminated. Please try /menu again to see all actions.";
                sendMessage(chatId, stopMessage);
            }

            default -> {
                String invalidCommandMessage = "This command is not supported. Please try again.";
                sendMessage(chatId, invalidCommandMessage);
            }
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();
        String username = callbackQuery.getFrom().getUserName();

        switch ( callbackData ) {
            case "submit_rating" -> {
                currentState = State.AWAITING_SHOP_NAME;
                sendMessage(chatId, "Please enter the name of the shop:");
            }
            case "view_ratings" -> {
                currentState = State.AWAITING_USER_RATINGS;
                viewUserRatings(chatId, username);
            }
            case "search_shop" -> {
                currentState = State.AWAITING_SHOP_SEARCH;
                sendMessage(chatId, "Please enter the name of the shop to search:");
            }
            case "category_cafe" -> {
                setCategoryAndPromptForRating(chatId, Shop.Category.Cafe);
            }
            case "category_restaurant" -> {
                setCategoryAndPromptForRating(chatId, Shop.Category.Restaurant);
            }
            case "category_drinks" -> {
                setCategoryAndPromptForRating(chatId, Shop.Category.Drinks);
            }
            case "category_foodcourt" -> {
                setCategoryAndPromptForRating(chatId, Shop.Category.FoodCourt);
            }
            case "category_streetstalls" -> {
                setCategoryAndPromptForRating(chatId, Shop.Category.StreetStalls);
            }
            default -> {
                // Handle unexpected callback data
            }
        }
    }

    private void sendMainMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Please choose an action:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        InlineKeyboardButton submitRatingButton = new InlineKeyboardButton();
        submitRatingButton.setText("Submit Rating");
        submitRatingButton.setCallbackData("submit_rating");

        InlineKeyboardButton viewRatingsButton = new InlineKeyboardButton();
        viewRatingsButton.setText("View Ratings");
        viewRatingsButton.setCallbackData("view_ratings");

        InlineKeyboardButton searchShopButton = new InlineKeyboardButton();
        searchShopButton.setText("Search Shop");
        searchShopButton.setCallbackData("search_shop");

        buttons.add(List.of(submitRatingButton));
        buttons.add(List.of(viewRatingsButton));
        buttons.add(List.of(searchShopButton));

        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleUserInput(Message message, Long chatId) {
        String text = message.getText();

        switch (currentState) {
            case AWAITING_SHOP_NAME:
                currentShop = new Shop(text, null);
                currentState = State.AWAITING_CATEGORY;
                sendCategoryOptions(chatId);
                break;
            case AWAITING_RATING:
                try {
                    double rating = Double.parseDouble(text);
                    currentRating.setScore(rating);
                    currentRating.setUsername(message.getFrom().getUserName());
                    currentState = State.AWAITING_DETAILS;
                    sendMessage(chatId, "Please enter any additional details:");
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Invalid rating. Please enter a numeric value.");
                }
                break;
            case AWAITING_DETAILS:
                currentRating.setAdditionalDetails(text);
                saveRating(chatId);
                break;
            case AWAITING_SHOP_SEARCH:
                searchShop(chatId, text);
                break;
            case AWAITING_USER_RATINGS:
                var username = message.getFrom().getUserName();
                viewUserRatings(chatId, username);
                break;
            default:
                sendMessage(chatId, "Please use the /menu command to start.");
        }
    }

    private void sendCategoryOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please choose a category:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(Arrays.asList(
                createCategoryButton("Cafe", "category_cafe"),
                createCategoryButton("Restaurant", "category_restaurant")
        ));
        buttons.add(Arrays.asList(
                createCategoryButton("Drinks", "category_drinks"),
                createCategoryButton("Food Court", "category_foodcourt")
        ));
        buttons.add(List.of(
                createCategoryButton("Street Stalls", "category_streetstalls")
        ));

        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardButton createCategoryButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void setCategoryAndPromptForRating(Long chatId, Shop.Category category) {
        currentShop.setCategory(category);
        currentRating = new ShopRating(currentShop, null, new Date(), 0, null);
        currentState = State.AWAITING_RATING;
        sendMessage(chatId, "Please enter your rating (0-10):");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveRating(Long chatId) {
        try {
            Credential credential = GoogleAuthorizeUtil.authorize();
            Sheets.Spreadsheets spreadsheets = SheetsServiceUtil.getSheetsService(credential);
            List<List<Object>> shopRatings = SheetsServiceUtil.getRatingsByShop(spreadsheets, currentShop.getName());

            double averageScore = SheetsServiceUtil.calculateAverageScore(shopRatings);
            if (!shopRatings.isEmpty()) {
                sendMessage(chatId, "The current average rating for " + currentShop.getName() + " is " + averageScore + ".");
            }

            SheetsServiceUtil.writeToSheet(spreadsheets, currentRating);

            sendMessage(chatId, "Thank you! Your rating has been saved.");
            currentState = State.NONE;
        } catch (Exception e) {
            sendMessage(chatId, "An error occurred while saving your rating. Please try again.");
            e.printStackTrace();
        }
    }

    private void searchShop(Long chatId, String shopName) {
        try {
            Credential credential = GoogleAuthorizeUtil.authorize();
            Sheets.Spreadsheets spreadsheets = SheetsServiceUtil.getSheetsService(credential);
            List<List<Object>> shopRatings = SheetsServiceUtil.getRatingsByShop(spreadsheets, shopName);

            if (shopRatings.isEmpty()) {
                sendMessage(chatId, "No ratings found for shop: " + shopName);
                return;
            }

            double averageScore = SheetsServiceUtil.calculateAverageScore(shopRatings);

            StringBuilder ratingsText = new StringBuilder("Ratings for " + shopName + " (Average Score: " + averageScore + "):\n\n");
            for (List<Object> row : shopRatings) {
                ratingsText.append("User: ").append(row.get(1)).append("\n")
                        .append("Date: ").append(row.get(2)).append("\n")
                        .append("Score: ").append(row.get(3)).append("\n")
                        .append("Details: ").append(row.get(4)).append("\n\n");
            }

            sendMessage(chatId, ratingsText.toString());
        } catch (IOException | GeneralSecurityException e) {
            sendMessage(chatId, "An error occurred while retrieving shop ratings. Please try again.");
            e.printStackTrace();
        }
    }

    private void viewUserRatings(Long chatId, String username) {
        try {
            Credential credential = GoogleAuthorizeUtil.authorize();
            Sheets.Spreadsheets spreadsheets = SheetsServiceUtil.getSheetsService(credential);
            List<List<Object>> userRatings = SheetsServiceUtil.getRatingsByUser(spreadsheets, username);

            if (userRatings.isEmpty()) {
                sendMessage(chatId, "No ratings found for user: " + username);
                return;
            }

            StringBuilder ratingsText = new StringBuilder("Ratings submitted by " + username + ":\n\n");
            for (List<Object> row : userRatings) {
                ratingsText.append("Shop: ").append(row.get(0)).append("\n")
                        .append("Date: ").append(row.get(2)).append("\n")
                        .append("Score: ").append(row.get(3)).append("\n")
                        .append("Details: ").append(row.get(4)).append("\n\n");
            }

            sendMessage(chatId, ratingsText.toString());
        } catch (IOException | GeneralSecurityException e) {
            sendMessage(chatId, "An error occurred while retrieving user ratings. Please try again.");
            e.printStackTrace();
        }
    }
}
