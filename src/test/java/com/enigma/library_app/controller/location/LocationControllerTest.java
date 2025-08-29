package com.enigma.library_app.controller.location;

import com.enigma.library_app.controller.constants.Constants;
import com.enigma.library_app.service.JwtService;
import com.enigma.library_app.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocationControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtService jwtService;
	private static String token;
	private static String locationId;

	@BeforeEach
	void setUp() {
		token = "Bearer " + jwtService.generateToken(Constants.USER_ADMIN);
	}

	@Order(1)
	@Test
	void testCreateLocation() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("name", "Perpustakaan Pusat");
		request.put("address", "PP 2");
		request.put("description", "Perpustakaan pusat description");
		mockMvc.perform(post("/api/locations")
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andExpectAll(status().isCreated())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					locationId = data.get("locationId").toString();
				});
	}

	@Order(2)
	@Test
	void testGetLocationById() throws Exception {
		mockMvc.perform(get("/api/locations/{locationId}", locationId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				).andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(locationId, data.get("locationId").toString());
				});
	}

	@Order(3)
	@Test
	void testGetAllLocation() throws Exception {
		mockMvc.perform(get("/api/locations")
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				).andExpectAll(status().isOk())
				.andDo(result -> {
					List<Object> objects = TestUtil.extractDataArray(result, objectMapper);
					assertFalse(objects.isEmpty());
				});
	}

	@Order(4)
	@Test
	void testUpdateLocationById() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("name", "Perpustakaan Pusat Update");
		request.put("address", "PP 2 Update");
		request.put("description", "Perpustakaan pusat description Update");
		mockMvc.perform(put("/api/locations/{locationId}", locationId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(locationId, data.get("locationId").toString());
				});
	}

	@Order(5)
	@Test
	void testDeleteLocationById() throws Exception {
		mockMvc.perform(delete("/api/locations/{locationId}", locationId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				).andExpectAll(status().isOk())
				.andDo(result -> {
					String jsonResponse = result.getResponse().getContentAsString();
					Map<String, Object> mapResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
					});
					String message = (String) mapResponse.get("data");
					assertEquals("Location deleted successfully.", message);
				});
	}
}