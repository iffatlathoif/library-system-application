package com.enigma.library_app.service.impl.telegram;

import com.enigma.library_app.constan.ConversationFlowState;
import com.enigma.library_app.dto.state.UserState;
import com.enigma.library_app.handlers.CallbackQueryHandler;
import com.enigma.library_app.handlers.CommandHandler;
import com.enigma.library_app.handlers.ConversationHandler;
import com.enigma.library_app.handlers.LibraryBot;
import com.enigma.library_app.service.contract.telegram.UpdateHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateDispatcherImpl implements UpdateHandlerService {

    private final CommandHandler commandHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final ConversationHandler conversationHandler;
    private final ConversationStateService conversationStateService;

    @Override
    public Validable handle(Update update, LibraryBot bot) {

        if (update.hasCallbackQuery()) {
            return callbackQueryHandler.handleCallBackQuery(update.getCallbackQuery(), bot);
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            UserState userState = conversationStateService.getState(chatId);
            if (userState.state() != ConversationFlowState.IDLE) {
                return conversationHandler.handle(update, bot);
            }

            if (text.startsWith("/")) {
                String[] commandParts = text.split(" ", 2);
                String command = commandParts[0];
                return commandHandler.handle(chatId, command, commandParts);
            }

        }
        return new SendMessage(String.valueOf(update.getMessage().getChatId()), "Perintah tidak valid.");
    }
}
