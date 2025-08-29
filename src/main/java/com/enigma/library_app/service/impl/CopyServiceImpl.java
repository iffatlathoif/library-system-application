package com.enigma.library_app.service.impl;

import com.enigma.library_app.dto.copy.request.CopyRequest;
import com.enigma.library_app.dto.copy.request.UpdateCopyRequest;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.enumeration.StatusCopies;
import com.enigma.library_app.exception.ApiException;
import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.mapper.CopyMapper;
import com.enigma.library_app.model.Book;
import com.enigma.library_app.model.Copy;
import com.enigma.library_app.model.Location;
import com.enigma.library_app.repository.CopyRepository;
import com.enigma.library_app.service.CopyService;
import com.enigma.library_app.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CopyServiceImpl implements CopyService {

	private final CopyRepository copyRepository;
	private final LocationService locationService;

	@Override
	public List<Copy> saveAll(List<Copy> copies) {
		return copyRepository.saveAll(copies);
	}

	@Override
	public List<CopyResponse> getAvailabilityLocationFaculty(String locationName, String facultyName) {
		List<Copy> copies = copyRepository.findByLocationNameAndFacultyName(locationName, facultyName);
		return CopyMapper.toDtoList(copies);
	}

	@Override
	public List<CopyResponse> createCopies(Book book, Location location, CopyRequest copyRequest) {
		List<CopyRequest> copyRequests = Collections.nCopies(copyRequest.getQuantity(), copyRequest);
		List<Copy> copies = new ArrayList<>();

		copyRequests.forEach(request -> {
			Copy copy = new Copy();
			copy.setBook(book);
			copy.setLocation(location);
			copy.setStatus(StatusCopies.AVAILABLE);
			copy.setAcquisitionDate(LocalDate.now());
			copy.setRackCode(request.getRackCode());
			if (location == null) {
				Location locationDB = book.getCopies().get(0).getLocation();
				copy.setLocation(locationDB);
			} else {
				copy.setLocation(location);
			}
			copies.add(copy);
		});
		List<Copy> savedCopies = saveAll(copies);
		return CopyMapper.toDtoList(savedCopies);
	}

	@Override
	public CopyResponse updateById(Long id, UpdateCopyRequest request) {
		Copy copy = getEntityById(id);

		if (request.getLocationId() != null) {
			Location location = locationService.getEntityById(request.getLocationId());
			copy.setLocation(location);
		}

		if (request.getStatus() != null) {
			copy.setStatus(request.getStatus());
		}

		if (request.getRackCode() != null) {
			copy.setRackCode(request.getRackCode());
		}
		Copy savedCopy = copyRepository.save(copy);
		return CopyMapper.toDto(savedCopy);
	}

	@Override
	public Copy getEntityById(Long id) {
		return copyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Copy not found with id " + id));
	}

	@Override
	public CopyResponse getById(Long id) {
		Copy copy = getEntityById(id);
		return CopyMapper.toDto(copy);
	}

	@Override
	public void deleteById(Long id) {
		Copy copy = getEntityById(id);
		if (copy.getStatus() == StatusCopies.ON_LOAN) {
			throw new ApiException("Cannot delete copy with id " + id + " . Copy is currently ON_LOAN.");
		}
		copyRepository.deleteById(id);
	}
}
