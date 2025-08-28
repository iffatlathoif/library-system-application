package com.enigma.library_app.service.contract.book;

import com.enigma.library_app.model.master.book.entity.Publisher;

public interface PublisherService {
	Publisher findByName(String name);
}
