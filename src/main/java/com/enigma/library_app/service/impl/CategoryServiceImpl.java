package com.enigma.library_app.service.impl;

import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.model.Category;
import com.enigma.library_app.repository.CategoryRepository;
import com.enigma.library_app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	@Override
	public Category findByName(String name) {
		return categoryRepository.findByName(name)
				.orElseGet(() -> {
					Category category = new Category();
					category.setName(name);
					return category;
				});
	}

	@Override
	public Category findById(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
	}
}
