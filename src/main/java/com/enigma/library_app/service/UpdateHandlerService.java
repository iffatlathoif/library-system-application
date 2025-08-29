package com.enigma.library_app.service;

import com.enigma.library_app.handlers.LibraryBot;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandlerService {
    public Validable handle(Update update, LibraryBot bot);

}
