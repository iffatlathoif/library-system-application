package com.enigma.library_app.repository;

import com.enigma.library_app.model.master.location.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, String> {
    Object getFacultiesByFacultyCode(String facultyCode);
}
