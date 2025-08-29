package com.enigma.library_app.service;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;

public interface TelegramBotService {
    void executeMessage(BotApiMethod<? extends Serializable> method);
}
