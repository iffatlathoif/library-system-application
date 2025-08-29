package com.enigma.library_app.mapper;

import com.enigma.library_app.dto.book.response.CategoryResponse;
import com.enigma.library_app.model.Book;
import com.enigma.library_app.model.Category;

import java.util.List;

public class CategoryMapper {

	public static CategoryResponse toDto(Category category) {
		return CategoryResponse.builder()
				.categoryId(category.getId())
				.name(category.getName())
				.build();
	}

	public static List<CategoryResponse> toDtoList(Book book) {
		return book.getCategory().stream()
				.map(CategoryMapper::toDto)
				.toList();
	}
}
