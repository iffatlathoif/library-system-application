package com.enigma.library_app.service;

import com.enigma.library_app.model.Publisher;

public interface PublisherService {
	Publisher findByName(String name);
}
