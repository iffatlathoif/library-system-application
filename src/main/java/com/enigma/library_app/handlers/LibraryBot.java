package com.enigma.library_app.handlers;

import com.enigma.library_app.service.contract.telegram.UpdateHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class LibraryBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.username}")
    private String username;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Lazy
    private final UpdateHandlerService updateHandler;

    public LibraryBot(@Lazy UpdateHandlerService updateHandler) {
        super();
        this.updateHandler = updateHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        executor.submit(() -> {
            Validable response = updateHandler.handle(update, this);
            if (response != null) {
                try {
                    if (response instanceof EditMessageText edit) {
                        execute(edit);
                    }
                    else if (response instanceof EditMessageCaption caption) {
                        execute(caption);
                    }

                    else if (response instanceof SendPhoto photo && update.hasCallbackQuery()) {
                        execute(new DeleteMessage(
                                update.getCallbackQuery().getMessage().getChatId().toString(),
                                update.getCallbackQuery().getMessage().getMessageId()
                        ));
                        execute(photo);
                    }
                    else if (response instanceof SendMessage sendMessage && update.hasCallbackQuery()) {
                        execute(new DeleteMessage(
                                update.getCallbackQuery().getMessage().getChatId().toString(),
                                update.getCallbackQuery().getMessage().getMessageId()
                        ));
                        execute(sendMessage);
                    }
                    else if (response instanceof BotApiMethod) {
                        execute((BotApiMethod<? extends Serializable>) response);
                    }
                } catch (TelegramApiException e) {
                    log.error("Gagal mengirim pesan", e);
                }
            }
        });
    }


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    public void sendPhotoToUser(long chatId, String imageUrl, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(imageUrl));
        sendPhoto.setCaption(caption);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToUser(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setParseMode("Markdown");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to chatId: " + chatId, e);
        }
    }

    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            if (!e.getMessage().contains("message to delete not found")) {
                log.error("Gagal menghapus pesan: {}", e.getMessage());
            }
        }
    }
}
