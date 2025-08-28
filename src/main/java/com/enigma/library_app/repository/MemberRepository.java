package com.enigma.library_app.repository;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.model.master.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String>, JpaSpecificationExecutor<Member> {
    Optional<Member> findByUser(User user);
    Optional<Member> findByUser_Username(String username);

    Member findByNisNip(String nisNip);
}
