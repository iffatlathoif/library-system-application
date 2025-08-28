package com.enigma.library_app.repository;

import com.enigma.library_app.model.master.member.entity.Member;
import com.enigma.library_app.model.master.member.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, String> {
    Optional<TelegramUser> findByChatId(long chatId);
}
