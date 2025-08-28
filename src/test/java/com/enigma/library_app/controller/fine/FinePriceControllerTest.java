package com.enigma.library_app.controller.fine;

import com.enigma.library_app.auth.security.JwtService;
import com.enigma.library_app.controller.constants.Constants;
import com.enigma.library_app.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinePriceControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtService jwtService;
	private static String token;
	private static String finePriceId;

	@BeforeAll
	static void beforeAll() {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
	}

	@BeforeEach
	void setUp() {
		token = "Bearer " + jwtService.generateToken(Constants.USER_STAFF);
	}

	@Order(1)
	@Test
	void testCreateFinePrice() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("price", "10000");
		mockMvc.perform(post("/api/fine-prices")
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andExpectAll(status().isCreated())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					finePriceId = data.get("id").toString();
				});
	}

	@Order(2)
	@Test
	void testGetFineById() throws Exception {
		mockMvc.perform(get("/api/fine-prices/{finePriceId}", finePriceId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				).andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(finePriceId, data.get("id").toString());
				});
	}

	@Order(3)
	@Test
	void testUpdatePriceById() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("price", "20000");
		mockMvc.perform(put("/api/fine-prices/{finePriceId}", finePriceId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(finePriceId, data.get("id").toString());
					assertEquals("20000", data.get("price").toString());
				});
	}

	@Order(4)
	@Test
	void testActivateFinePriceById() throws Exception {
		mockMvc.perform(put("/api/fine-prices/{finePriceId}/activate", finePriceId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				).andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertTrue((Boolean) data.get("active"));
					assertEquals(finePriceId, data.get("id").toString());
				});
	}

	@Order(5)
	@Test
	void testGetActiveFinePrice() throws Exception {
		mockMvc.perform(get("/api/fine-prices/active")
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				).andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertTrue((Boolean) data.get("active"));
				});
	}

	@Order(6)
	@Test
	void testUpdateActiveById() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("isActive", "false");
		mockMvc.perform(put("/api/fine-prices/{finePriceId}", finePriceId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(finePriceId, data.get("id").toString());
					assertEquals(false, data.get("active"));
				});
	}

	@Order(7)
	@Test
	void testDeleteByIdNotActive() throws Exception {
		mockMvc.perform(delete("/api/fine-prices/{finePriceId}", finePriceId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				).andExpectAll(status().isOk())
				.andDo(result -> {
					String jsonResponse = result.getResponse().getContentAsString();
					Map<String, Object> mapResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
					});
					String message = (String) mapResponse.get("data");
					assertEquals("Fine Price deleted successfully.", message);
				});
	}
}