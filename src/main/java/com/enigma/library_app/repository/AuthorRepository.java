package com.enigma.library_app.repository;

import com.enigma.library_app.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
	Optional<Author> findByName(String name);
}
