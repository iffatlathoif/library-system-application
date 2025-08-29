package com.enigma.library_app.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.enigma.library_app.enumeration.Role;
import com.enigma.library_app.model.User;
import com.enigma.library_app.exception.NotFoundException;
import com.enigma.library_app.service.ValidationService;
import com.enigma.library_app.dto.member.request.CreateMemberRequest;
import com.enigma.library_app.dto.member.request.UpdateMemberRequest;
import com.enigma.library_app.dto.member.response.MemberResponse;
import com.enigma.library_app.model.Faculty;
import com.enigma.library_app.model.Member;
import com.enigma.library_app.model.TelegramUser;
import com.enigma.library_app.enumeration.CardPrintStatus;
import com.enigma.library_app.enumeration.Status;
import com.enigma.library_app.repository.*;
import com.enigma.library_app.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private TelegramUserRepository telegramUserRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private Cloudinary cloudinary;

    @Transactional
    @Override
    public MemberResponse createByAdmin(CreateMemberRequest request) {
        validationService.validate(request);
        // 1. Ambil Faculty berdasarkan kode
        Faculty faculty = facultyRepository.findById(request.getFacultyCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

        Member member = new Member();
        member.setNisNip(request.getNisNip());
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setFaculty(faculty);
        member.setType(request.getType());
        member.setStatus(Status.ACTIVE);
        member.setJoinDate(LocalDate.now());
        member.setCardPrintStatus(CardPrintStatus.NONE);

        memberRepository.save(member);


        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getName());
        user.setRoles(Role.MEMBER);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setMember(member);
        userRepository.save(user);

        member.setUser(user);
        memberRepository.save(member);


        if (request.getTelegramId() != null) {
            try {
                Long chatId = Long.parseLong(request.getTelegramId());
                TelegramUser telegramUser = TelegramUser.builder()
                        .chatId(chatId)
                        .member(member)
                        .build();
                telegramUserRepository.save(telegramUser);
                member.setTelegramUser(telegramUser); // agar muncul di response
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Telegram ID (must be numeric)");
            }
        }
        return toMemberResponse(member);
    }
private MemberResponse toMemberResponse(Member member){
    User user = member.getUser();
return MemberResponse.builder()
        .id(member.getMemberId())
        .username(user != null ? user.getUsername() : null)
        .name(member.getName())
        .nisNip(member.getNisNip())
        .email(member.getEmail())
        .phone(member.getPhone())
        .telegramId(
                member.getTelegramUser() != null ? member.getTelegramUser().getTelegramId() : null
        )
        .facultyCode(
                member.getFaculty() != null ? member.getFaculty().getFacultyCode() : null
        )
        .type(member.getType())
        .status(member.getStatus())
        .photo(member.getPhoto())
        .build();
}

    @Transactional
    @Override
    public MemberResponse updateByAdmin(String memberId, UpdateMemberRequest request) {
        validationService.validate(request);

        // Ambil member berdasarkan ID
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        // Update fakultas
        Faculty faculty = facultyRepository.findById(request.getFacultyCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));


        // Update TelegramUser (jika ada)
        TelegramUser telegramUser = null;
        if (request.getTelegramId() != null) {
            telegramUser = telegramUserRepository.findById(request.getTelegramId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telegram User not found"));
        }

        // Update data member
        member.setNisNip(request.getNisNip());
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setTelegramUser(telegramUser);
        member.setFaculty(faculty);
        member.setType(request.getType());
        member.setStatus(request.getStatus());

        memberRepository.save(member);

        // Ambil user yang terhubung dengan member
        User user = userRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User linked to member not found"));

        userRepository.save(user);
        return toMemberResponse(member);
    }
    @Transactional(readOnly = true)
    @Override
    public MemberResponse getMemberById(String memberId) {
        // Ambil data member dari database
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        // Ambil data user yang terhubung dengan member ini
        User user = userRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User for this member not found"));

        return toMemberResponse(member);
    }

    @Transactional
    @Override
    public MemberResponse uploadPhotoByMember(String memberId, MultipartFile file) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String photoUrl = (String) uploadResult.get("secure_url");
            member.setPhoto(photoUrl);
            memberRepository.save(member);

            return toMemberResponse(member);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MemberResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Member> memberPage = memberRepository.findAll(pageable);

        List<MemberResponse> responseList = memberPage.getContent().stream()
                .map(this::toMemberResponse)
                .toList();
        return new PageImpl<>(responseList, pageable, memberPage.getTotalElements());
    }
    @Transactional
    @Override
    public String deleteById(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        String kode ="Member "+ member.getName()+" dengan Username "+member.getUser().getUsername()+" berhasil dihapus !";
        // 2. Delete associated TelegramUser if exists
        if (member.getTelegramUser() != null) {
            telegramUserRepository.delete(member.getTelegramUser());
        }

        // 3. Delete the associated User
        User user = member.getUser();
        if (user != null) {
            userRepository.delete(user);
        }

        // 4. Finally delete the member
        memberRepository.delete(member);
        return kode;
    }

    @Override
    public Member getByUser(User currentUser) {
        return memberRepository.findByUser(currentUser)
                .orElseThrow(() -> new NotFoundException("Member not found for user: " + currentUser.getEmail()));

    }

}
