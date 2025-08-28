package com.enigma.library_app.service.contract.member;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.dto.member.request.CreateMemberRequest;
import com.enigma.library_app.dto.member.request.UpdateMemberRequest;
import com.enigma.library_app.dto.member.response.MemberResponse;
import com.enigma.library_app.model.master.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    MemberResponse createByAdmin(CreateMemberRequest request);
    MemberResponse updateByAdmin(String memberId, UpdateMemberRequest request);
    MemberResponse getMemberById(String memberId);
    MemberResponse uploadPhotoByMember(String memberId, MultipartFile file);
    Page<MemberResponse> getAll(int page, int size);
    String deleteById(String memberId);

    Member getByUser(User currentUser);
}
