package com.enigma.library_app.service.impl.faculty;

import com.enigma.library_app.common.ValidationService;
import com.enigma.library_app.dto.faculty.request.CreateFacultyRequest;
import com.enigma.library_app.dto.faculty.request.UpdateFacultyRequest;
import com.enigma.library_app.dto.faculty.response.FacultyResponse;
import com.enigma.library_app.model.master.location.entity.Faculty;
import com.enigma.library_app.repository.FacultyRepository;
import com.enigma.library_app.service.contract.faculty.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FacultyServiceImpl implements FacultyService {
    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    @Override
    public FacultyResponse create(CreateFacultyRequest request) {
        validationService.validate(request);
        Faculty faculty = new Faculty();
        faculty.setFacultyCode(request.getFacultyCode());
        faculty.setName(request.getName());

        facultyRepository.save(faculty);

        return toFacultyResponse(faculty);
    }
    @Transactional
    @Override
    public FacultyResponse update(String facultyId, UpdateFacultyRequest request) {
        validationService.validate(request);
        Faculty faculty = facultyRepository.findById(request.getFacultyCode())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Faculty Not Found!"));

        faculty.setFacultyCode(request.getFacultyCode());
        faculty.setName(request.getName());

        facultyRepository.save(faculty);

        return toFacultyResponse(faculty);
    }

    @Override
    public FacultyResponse getById(String id) {
        validationService.validate(id);
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Faculty Not Found!"));
        return toFacultyResponse(faculty);
    }

    @Override
    public String deleteById(String id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Faculty Not Found!"));
        String kode = faculty.getFacultyCode();
        facultyRepository.delete(faculty);
        return kode;
    }

    @Override
    public Page<FacultyResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Faculty> facultyPage = facultyRepository.findAll(pageable);

        List<FacultyResponse> responseList = facultyPage.getContent().stream()
                .map(this::toFacultyResponse)
                .toList();
        return new PageImpl<>(responseList, pageable, facultyPage.getTotalElements());

    }

    private FacultyResponse toFacultyResponse(Faculty faculty){
        return FacultyResponse.builder()
                .facultyCode(faculty.getFacultyCode())
                .name(faculty.getName())
                .build();
    }
}
