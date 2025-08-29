package com.enigma.library_app.service.impl;

import com.enigma.library_app.model.Author;
import com.enigma.library_app.repository.AuthorRepository;
import com.enigma.library_app.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

	private final AuthorRepository authorRepository;

	@Override
	public Author findByName(String name) {
		return authorRepository.findByName(name)
				.orElseGet(() -> {
					Author author = new Author();
					author.setName(name);
					return author;
				});
	}
}
