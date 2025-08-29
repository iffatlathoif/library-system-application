package com.enigma.library_app.service.impl;

import com.enigma.library_app.enumeration.ConversationFlowState;
import com.enigma.library_app.dto.state.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationStateService {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public UserState getState(long chatId) {
        return userStates.getOrDefault(chatId, new UserState(ConversationFlowState.IDLE, new ConcurrentHashMap<>()));
    }

    public void setState(long chatId, ConversationFlowState state, String key, Object value) {
        UserState currentState = getState(chatId);
        currentState.data().put(key, value);
        userStates.put(chatId, new UserState(state, currentState.data()));
    }

    public void clearState(long chatId) {
        userStates.remove(chatId);
    }
}
