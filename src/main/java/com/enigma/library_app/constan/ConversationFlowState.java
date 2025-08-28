package com.enigma.library_app.constan;

// Enum untuk setiap langkah dalam alur percakapan
public enum ConversationFlowState {
    IDLE,
    AWAITING_CREDENTIALS,
    AWAITING_OTP,
    AWAITING_RATING,
    AWAITING_REVIEW_TEXT,
    AWAITING_NEW_PASSWORD,
    AWAITING_PASSWORD_CHANGE_OTP
}