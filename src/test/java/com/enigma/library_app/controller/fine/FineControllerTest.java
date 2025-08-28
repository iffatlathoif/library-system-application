package com.enigma.library_app.controller.fine;

import com.enigma.library_app.auth.security.JwtService;
import com.enigma.library_app.controller.constants.Constants;
import com.enigma.library_app.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FineControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtService jwtService;
	private static String token;

	@BeforeAll
	static void beforeAll() {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
	}

	@BeforeEach
	void setUp() {
		token = "Bearer " + jwtService.generateToken(Constants.USER_STAFF);
	}

	@Test
	void testGetFineAllRevenue() throws Exception {
		mockMvc.perform(get("/api/reports/fines")
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertNotNull(data);
				});
	}

	@Test
	void testGetFineAllRevenueByYearAndMonth() throws Exception {
		mockMvc.perform(get("/api/reports/fines")
						.header(AUTHORIZATION, token)
						.param("year", "2025")
						.param("month", "1")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertNotNull(data);
				});
	}

	@Test
	void testGetAllFinePaidByDateRange() throws Exception {
		mockMvc.perform(get("/api/fines")
						.header(AUTHORIZATION, token)
						.param("startDate", "02-08-2025")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					List<Object> objects = TestUtil.extractDataArray(result, objectMapper);
					assertFalse(objects.isEmpty());
				});
	}
}