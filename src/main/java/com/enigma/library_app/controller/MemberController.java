package com.enigma.library_app.controller;


import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.PagingResponse;
import com.enigma.library_app.dto.member.request.CreateMemberRequest;
import com.enigma.library_app.dto.member.request.UpdateMemberRequest;
import com.enigma.library_app.dto.member.response.MemberResponse;
import com.enigma.library_app.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(
            path = "/api/member",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<MemberResponse> createMemberByAdmin(@RequestBody CreateMemberRequest request){
        MemberResponse memberResponse = memberService.createByAdmin(request);
        return BaseResponse.<MemberResponse>builder().data(memberResponse).build();
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(
            path = "/api/member/{memberId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<MemberResponse> update(@PathVariable String memberId,
                                               @RequestBody UpdateMemberRequest request){
        MemberResponse memberResponse = memberService.updateByAdmin(memberId, request);
        return BaseResponse.<MemberResponse>builder().data(memberResponse).build();
    }
    @GetMapping(
            path = "/api/member/{memberId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<MemberResponse> getById(@PathVariable("memberId") String memberId){
        MemberResponse memberResponse = memberService.getMemberById(memberId);
        return BaseResponse.<MemberResponse>builder().data(memberResponse).build();
    }
    @GetMapping("/api/member")
    public BaseResponse<List<MemberResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<MemberResponse> responsePage = memberService.getAll(page, size);
        PagingResponse paging = PagingResponse.builder()
                .currentPage(responsePage.getNumber())
                .totalPage(responsePage.getTotalPages())
                .size(responsePage.getSize())
                .build();

        return BaseResponse.<List<MemberResponse>>builder()
                .data(responsePage.getContent())
                .paging(paging)
                .build();
    }
    @PostMapping("api/member/{memberId}/upload-photo")
    @PreAuthorize("hasAuthority('MEMBER')")
    public BaseResponse<MemberResponse> uploadPhoto(
            @PathVariable String memberId,
            @RequestParam("file") MultipartFile file) {
        return BaseResponse.success(memberService.uploadPhotoByMember(memberId, file));
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping(
            path = "/api/member/{memberId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<String> delete(@PathVariable("memberId") String memberId){
        String kode = memberService.deleteById(memberId);
        return BaseResponse.<String>builder()
                .data(kode).build();
    }

}
