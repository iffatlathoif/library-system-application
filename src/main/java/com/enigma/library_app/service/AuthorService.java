package com.enigma.library_app.service;

import com.enigma.library_app.model.Author;

public interface AuthorService {
	Author findByName(String name);
}
