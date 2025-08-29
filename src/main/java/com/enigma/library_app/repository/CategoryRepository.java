package com.enigma.library_app.repository;

import com.enigma.library_app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByName(String name);
}
