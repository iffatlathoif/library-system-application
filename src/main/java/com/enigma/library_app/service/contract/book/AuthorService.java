package com.enigma.library_app.service.contract.book;

import com.enigma.library_app.model.master.book.entity.Author;

public interface AuthorService {
	Author findByName(String name);
}
