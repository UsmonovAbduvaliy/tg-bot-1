package org.example;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static TelegramBot bot = new TelegramBot("8171122508:AAHXmoggr5HPmMX1nPjVGEIGxY_xOIF6hKs");
    static {
        try {
            DB.inport();
        } catch (FileNotFoundException e) {

        }
    }
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        bot.setUpdatesListener(updates->{
            for (Update update : updates) {
                executorService.execute(() -> {
                    BotServer.server(update);
                });
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}