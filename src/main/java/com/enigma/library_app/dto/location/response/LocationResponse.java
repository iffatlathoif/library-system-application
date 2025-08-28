package com.enigma.library_app.dto.location.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponse {
	private Long locationId;
	private String name;
	private String address;
	private String description;

    public LocationResponse(Long locationId, String name) {
		this.locationId = locationId;
		this.name = name;
    }
}
