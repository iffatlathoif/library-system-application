package com.enigma.library_app.service.contract.book;

import com.enigma.library_app.dto.copy.request.CopyRequest;
import com.enigma.library_app.dto.copy.request.UpdateCopyRequest;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.Copy;
import com.enigma.library_app.model.master.location.entity.Location;

import java.util.List;

public interface CopyService {
	List<Copy> saveAll(List<Copy> copies);
	List<CopyResponse> getAvailabilityLocationFaculty(String locationName, String facultyName);
	List<CopyResponse> createCopies(Book book, Location location, CopyRequest copyRequest);
	CopyResponse updateById(Long id, UpdateCopyRequest request);
	Copy getEntityById(Long id);
	CopyResponse getById(Long id);
	void deleteById(Long id);
}
