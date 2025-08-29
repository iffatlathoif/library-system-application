package com.enigma.library_app.controller.fine;

import com.enigma.library_app.controller.constants.Constants;
import com.enigma.library_app.service.JwtService;
import com.enigma.library_app.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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