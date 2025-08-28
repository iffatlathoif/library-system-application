//package com.enigma.library_app.config;
////
//import com.enigma.library_app.auth.constant.Role;
//import com.enigma.library_app.auth.entity.User;
//import com.enigma.library_app.model.master.member.entity.Member;
//import com.enigma.library_app.model.master.member.entity.TelegramUser;
//import com.enigma.library_app.model.master.member.enumeration.Status;
//import com.enigma.library_app.model.master.member.enumeration.Type;
//import com.enigma.library_app.repository.MemberRepository;
//import com.enigma.library_app.repository.TelegramUserRepository;
//import com.enigma.library_app.repository.UserRepository;
//import jakarta.persistence.EntityManager;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional; // Tambahkan import ini
//
//import java.time.LocalDate;
//
//@Component
//@RequiredArgsConstructor
//public class DataSeeder implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final MemberRepository memberRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final TelegramUserRepository telegramUserRepository;
//    private final EntityManager entityManager;
//
//    @Override
//    @Transactional // Anotasi ini penting untuk memastikan semua operasi dalam satu transaksi
//    public void run(String... args) throws Exception {
//        if (userRepository.count() > 0) {
//            return;
//        }
//
//        // 1. Buat data Member (TANPA set ID manual)
//        Member adminMember = new Member();
////        adminMember.setMemberId("dee068c6-a954-47d5-b813-14d055766dd6");
//        adminMember.setName("Nur Farid");
//        adminMember.setNisNip("000001");
//        adminMember.setEmail("nurfarid776@gmail.com");
//        adminMember.setPhone("081122334455");
//        adminMember.setType(Type.DOSEN);
//        adminMember.setStatus(Status.ACTIVE);
//        adminMember.setJoinDate(LocalDate.now());
//
//        // Simpan Member, dan tangkap hasilnya yang sudah memiliki ID
//        Member savedMember = entityManager.merge(adminMember);
//
//        // 2. Buat data User
//        User adminUser = new User();
//        adminUser.setUsername("nurfarid");
//        adminUser.setPasswordHash(passwordEncoder.encode("12345678"));
//        adminUser.setEmail("nurfarid776@gmail.com");
//        adminUser.setRoles(Role.MEMBER);
//
//        // Gunakan objek 'savedMember' yang sudah memiliki ID dari database
//        adminUser.setMember(savedMember);
//        savedMember.setUser(adminUser);
//        userRepository.save(adminUser);
//
//        // --- TAMBAHKAN BLOK INI UNTUK MEMBUAT TELEGRAM USER ---
//        // Asumsikan chatId Anda untuk testing adalah 123456789L
//        // Ganti dengan chatId Anda yang sebenarnya jika perlu.
//        TelegramUser adminTelegramUser = new TelegramUser();
//        adminTelegramUser.setChatId(5779717556L); // CONTOH CHAT ID
//        adminTelegramUser.setMember(savedMember); // Hubungkan dengan member yang sudah ada
//        telegramUserRepository.save(adminTelegramUser);
//    }
//}
//
//
