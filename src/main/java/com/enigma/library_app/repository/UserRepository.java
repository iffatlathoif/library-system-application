package com.enigma.library_app.repository;

import com.enigma.library_app.model.User;
import com.enigma.library_app.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByMember_MemberId(String memberId);

    Optional<User> findByMember(Member member);

}
