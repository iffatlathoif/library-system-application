package com.enigma.library_app.repository;

import com.enigma.library_app.model.transaction.fine.entity.FinePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FinePriceRepository extends JpaRepository<FinePrice, Long> {
	@Modifying
	@Query("UPDATE FinePrice f SET f.isActive = false WHERE f.isActive = true")
	void deactivateAllActivePrices();

	@Query("SELECT f FROM FinePrice f WHERE f.isActive = true")
	Optional<FinePrice> findActiveFinePrices();

	@Query("select fp from FinePrice fp where fp.isActive = ?1")
	FinePrice findByActive(boolean isActive);
}
