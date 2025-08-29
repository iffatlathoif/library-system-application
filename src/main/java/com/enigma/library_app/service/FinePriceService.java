package com.enigma.library_app.service;

import com.enigma.library_app.dto.fine.request.CreateFinePriceRequest;
import com.enigma.library_app.dto.fine.response.FinePriceResponse;
import com.enigma.library_app.model.FinePrice;

public interface FinePriceService {
	FinePriceResponse create(CreateFinePriceRequest request);
	FinePriceResponse updateById(Long id, CreateFinePriceRequest request);
	FinePrice getEntityById(Long id);
	FinePriceResponse getById(Long id);
	FinePriceResponse activateFinePrice(Long id);
	void deleteById(Long id);
	FinePriceResponse getByActive();
}
