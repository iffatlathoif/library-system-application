package com.enigma.library_app.service;

import com.enigma.library_app.model.BookSubscription;

import java.util.List;

public interface SubscriptionService {
    void createSubscription(String memberId, String bookId) throws Exception;
    List<BookSubscription> getWishlistByMemberId(String memberId);
    void notifySubscribers(String bookId);
}