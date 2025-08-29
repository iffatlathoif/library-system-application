package com.enigma.library_app.mapper;

import com.enigma.library_app.dto.location.request.CreateLocationRequest;
import com.enigma.library_app.dto.location.response.LocationResponse;
import com.enigma.library_app.model.Location;

public class LocationMapper {
	public static LocationResponse toDto(Location location) {
		return LocationResponse.builder()
				.locationId(location.getLocationId())
				.name(location.getName())
				.address(location.getAddress())
				.description(location.getDescription())
				.build();
	}

	public static Location toEntity(CreateLocationRequest request) {
		return Location.builder()
				.name(request.getName())
				.address(request.getAddress())
				.description(request.getDescription())
				.build();
	}
}
