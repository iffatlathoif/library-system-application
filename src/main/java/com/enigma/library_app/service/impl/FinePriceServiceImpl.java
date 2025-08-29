package com.enigma.library_app.service.impl;

import com.enigma.library_app.dto.fine.request.CreateFinePriceRequest;
import com.enigma.library_app.dto.fine.response.FinePriceResponse;
import com.enigma.library_app.exception.ApiException;
import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.model.FinePrice;
import com.enigma.library_app.repository.FinePriceRepository;
import com.enigma.library_app.service.FinePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinePriceServiceImpl implements FinePriceService {

	private final FinePriceRepository finePriceRepository;

	@Transactional
	@Override
	public FinePriceResponse create(CreateFinePriceRequest request) {
		FinePrice finePrice = FinePrice.builder()
				.price(request.getPrice())
				.isActive(false)
				.build();
		FinePrice savedPrice = finePriceRepository.save(finePrice);
		return toDto(savedPrice);
	}

	@Override
	public FinePriceResponse updateById(Long id, CreateFinePriceRequest request) {
		FinePrice finePrice = getEntityById(id);
		if (request.getPrice() != null) {
			finePrice.setPrice(request.getPrice());
		}

		if (request.getIsActive() != null) {
			if (request.getIsActive()) {
				finePriceRepository.deactivateAllActivePrices();
				finePrice.setActive(request.getIsActive());
			} else {
				finePrice.setActive(false);
			}
		}
		FinePrice savedFinePrice = finePriceRepository.save(finePrice);
		return toDto(savedFinePrice);
	}

	@Override
	public FinePrice getEntityById(Long id) {
		return finePriceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fine price not found with id " + id));
	}

	@Override
	public FinePriceResponse getById(Long id) {
		FinePrice finePrice = getEntityById(id);
		return toDto(finePrice);
	}

	@Transactional
	@Override
	public FinePriceResponse activateFinePrice(Long id) {
		finePriceRepository.deactivateAllActivePrices();
		FinePrice finePrice = getEntityById(id);
		finePrice.setActive(true);
		return toDto(finePriceRepository.save(finePrice));
	}

	@Override
	public FinePriceResponse getByActive() {
		FinePrice finePrice = finePriceRepository.findByActive(true);
		return toDto(finePrice);
	}

	@Override
	public void deleteById(Long id) {
		FinePrice finePrice = getEntityById(id);
		if (finePrice.isActive()) {
			throw new ApiException("Cannot delete an active Fine Price. Please deactivate it first.");
		}
		finePriceRepository.delete(finePrice);
	}

	private static FinePriceResponse toDto(FinePrice savedPrice) {
		return FinePriceResponse.builder()
				.id(savedPrice.getId())
				.price(savedPrice.getPrice())
				.isActive(savedPrice.isActive())
				.build();
	}
}
