package com.enigma.library_app.dto.state;

import com.enigma.library_app.enumeration.ConversationFlowState;

import java.util.Map;

public record UserState(ConversationFlowState state, Map<String, Object> data) {}
