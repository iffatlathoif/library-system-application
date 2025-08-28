package com.enigma.library_app.service.contract.book;

import com.enigma.library_app.model.master.book.entity.Category;

public interface CategoryService {
	Category findByName(String name);
	Category findById(Long id);
}
