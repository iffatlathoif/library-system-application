package com.enigma.library_app.service;

import com.enigma.library_app.dto.faculty.request.CreateFacultyRequest;
import com.enigma.library_app.dto.faculty.request.UpdateFacultyRequest;
import com.enigma.library_app.dto.faculty.response.FacultyResponse;
import org.springframework.data.domain.Page;

public interface FacultyService {
    FacultyResponse create(CreateFacultyRequest request);
    FacultyResponse update(String id, UpdateFacultyRequest request);
    FacultyResponse getById(String id);
    String deleteById(String id);
    Page<FacultyResponse> getAll(int page, int size);
}
