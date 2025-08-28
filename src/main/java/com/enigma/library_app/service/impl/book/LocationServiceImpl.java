package com.enigma.library_app.service.impl.book;

import com.enigma.library_app.dto.location.request.CreateLocationRequest;
import com.enigma.library_app.dto.location.request.UpdateLocationRequest;
import com.enigma.library_app.dto.location.response.LocationResponse;
import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.mapper.LocationMapper;
import com.enigma.library_app.repository.LocationRepository;
import com.enigma.library_app.service.contract.book.LocationService;
import com.enigma.library_app.model.master.location.entity.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

	private final LocationRepository locationRepository;

	@Override
	public Location findByName(String name) {
		return locationRepository.findByName(name)
				.orElseGet(() -> {
					Location location = new Location();
					location.setName(name);
					return location;
				});
	}

	@Override
	public Location getEntityById(Long id) {
		return locationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Location not found with id " + id));
	}

	@Override
	public LocationResponse getById(Long id) {
		Location location = getEntityById(id);
		return LocationMapper.toDto(location);
	}

	@Override
	public LocationResponse create(CreateLocationRequest request) {
		Location location = LocationMapper.toEntity(request);
		Location savedLocation = locationRepository.save(location);
		return LocationMapper.toDto(savedLocation);
	}

	@Override
	public Page<LocationResponse> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Location> pages = locationRepository.findAll(pageable);
		List<LocationResponse> dtoList = pages.getContent().stream()
				.map(LocationMapper::toDto).toList();
		return new PageImpl<>(dtoList, pageable, pages.getTotalElements());
	}

	@Override
	public LocationResponse updateById(Long id, UpdateLocationRequest request) {
		Location location = getEntityById(id);
		if (request.getAddress() != null) {
			location.setAddress(request.getAddress());
		}

		if (request.getName() != null) {
			location.setName(request.getName());
		}

		if (request.getDescription() != null) {
			location.setDescription(request.getDescription());
		}
		Location savedLocation = locationRepository.save(location);
		return LocationMapper.toDto(savedLocation);
	}

	@Override
	public void deleteById(Long id) {
		Location location = getEntityById(id);
		locationRepository.delete(location);
	}
}
