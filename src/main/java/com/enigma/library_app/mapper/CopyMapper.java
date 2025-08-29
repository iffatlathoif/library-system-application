package com.enigma.library_app.mapper;

import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.model.Copy;

import java.util.List;

public class CopyMapper {
	public static CopyResponse toDto(Copy copy) {
		return CopyResponse.builder()
				.copyId(copy.getCopyId())
				.book(BookMapper.toDto(copy.getBook()))
				.location(LocationMapper.toDto(copy.getLocation()))
				.rackCode(copy.getRackCode())
				.status(copy.getStatus())
				.build();
	}

	public static List<CopyResponse> toDtoList(List<Copy> copies) {
		return copies.stream()
				.map(CopyMapper::toDto)
				.toList();
	}
}
