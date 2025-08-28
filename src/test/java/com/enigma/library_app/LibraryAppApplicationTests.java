package com.enigma.library_app;

import com.enigma.library_app.handlers.LibraryBot;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.TelegramBotsApi;


@SpringBootTest
class LibraryAppApplicationTests {

	@MockitoBean
	private LibraryBot mockedLibraryBot;

	@MockitoBean
	private TelegramBotsApi mockedTelegramBotsApi;

	@Test
	void contextLoads() {
	}

}
