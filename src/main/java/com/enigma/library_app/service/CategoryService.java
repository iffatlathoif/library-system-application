package com.enigma.library_app.service;

import com.enigma.library_app.model.Category;

public interface CategoryService {
	Category findByName(String name);
	Category findById(Long id);
}
