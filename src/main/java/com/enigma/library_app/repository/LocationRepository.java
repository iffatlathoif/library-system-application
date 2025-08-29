package com.enigma.library_app.repository;

import com.enigma.library_app.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
	Optional<Location> findByName(String name);
}
