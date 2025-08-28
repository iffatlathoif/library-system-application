package com.enigma.library_app.service.impl.book;

import com.enigma.library_app.model.master.book.entity.Publisher;
import com.enigma.library_app.repository.PublisherRepository;
import com.enigma.library_app.service.contract.book.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

	private final PublisherRepository publisherRepository;


	@Override
	public Publisher findByName(String name) {
		return publisherRepository.findByName(name)
				.orElseGet(() -> {
					Publisher publisher = new Publisher();
					publisher.setName(name);
					return publisher;
				});
	}
}
