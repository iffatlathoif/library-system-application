package com.enigma.library_app.repository;

import com.enigma.library_app.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, String> {
	Optional<Publisher> findByName(String name);
}
