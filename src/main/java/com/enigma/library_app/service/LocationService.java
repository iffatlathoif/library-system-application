package com.enigma.library_app.service;

import com.enigma.library_app.dto.location.request.CreateLocationRequest;
import com.enigma.library_app.dto.location.request.UpdateLocationRequest;
import com.enigma.library_app.dto.location.response.LocationResponse;
import com.enigma.library_app.model.Location;
import org.springframework.data.domain.Page;

public interface LocationService {
	Location findByName(String name);
	Location getEntityById(Long id);
	LocationResponse getById(Long id);
	LocationResponse create(CreateLocationRequest request);

	Page<LocationResponse> getAll(Integer page, Integer size);

	LocationResponse updateById(Long id, UpdateLocationRequest request);

	void deleteById(Long id);
}
