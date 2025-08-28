package com.enigma.library_app.controller.faculty;

import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.PagingResponse;
import com.enigma.library_app.dto.faculty.request.CreateFacultyRequest;
import com.enigma.library_app.dto.faculty.request.UpdateFacultyRequest;
import com.enigma.library_app.dto.faculty.response.FacultyResponse;
import com.enigma.library_app.service.contract.faculty.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FacultyController {
    @Autowired
    private FacultyService facultyService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(
            path = "/api/faculty",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<FacultyResponse> create(@RequestBody CreateFacultyRequest request){
        FacultyResponse facultyResponse = facultyService.create(request);
        return BaseResponse.<FacultyResponse>builder().data(facultyResponse).build();
    }
    @PutMapping(
            path = "/api/faculty/{facultyId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseResponse<FacultyResponse> update(@RequestBody UpdateFacultyRequest request,
                                               @PathVariable("facultyId") String facultyId){
        FacultyResponse facultyResponse = facultyService.update(facultyId, request);
        return BaseResponse.<FacultyResponse>builder().data(facultyResponse).build();
    }
    @GetMapping("/api/faculty")
    public BaseResponse<List<FacultyResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<FacultyResponse> responsePage = facultyService.getAll(page, size);
        PagingResponse paging = PagingResponse.builder()
                .currentPage(responsePage.getNumber())
                .totalPage(responsePage.getTotalPages())
                .size(responsePage.getSize())
                .build();

        return BaseResponse.<List<FacultyResponse>>builder()
                .data(responsePage.getContent())
                .paging(paging)
                .build();
    }
    @GetMapping(
            path = "/api/faculty/{facultyId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<FacultyResponse> getById(@PathVariable("facultyId") String facultyId){
        FacultyResponse facultyResponse = facultyService.getById(facultyId);
        return BaseResponse.<FacultyResponse>builder().data(facultyResponse).build();
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping(
            path = "/api/faculty/{facultyId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<String> delete(@PathVariable("facultyId") String facultyId){
        String kode = facultyService.deleteById(facultyId);
        return BaseResponse.<String>builder()
                .data("Faculty "+ kode + " Telah Dihapus!").build();
    }
}
