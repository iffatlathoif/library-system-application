package com.enigma.library_app.controller.copy;

import com.enigma.library_app.service.JwtService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CopyControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtService jwtService;
	private static String token;
	private static String bookId;
	private static String copyId;

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
	void testCreateBook() throws Exception {
		String title = "Jejak Langkah di Hutan Senyap 5";
		Map<String, Object> request = getRequestBook(title);

		// copy quantity and rack
		Map<String, Object> copy = getRequestCopy();
		request.put("copy", copy);
		mockMvc.perform(post("/api/books")
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andExpectAll(status().isCreated())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals("Jejak Langkah di Hutan Senyap 5", data.get("title"));
					bookId = data.get("id").toString();
				});
	}

	@Order(2)
	@Test
	void testAddCopiesToBook() throws Exception {
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> requestCopy = getRequestCopy();
		request.put("copy", requestCopy);
		mockMvc.perform(post("/api/books/{bookId}/copies", bookId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andExpectAll(status().isCreated())
				.andDo(result -> {
					List<Object> objects = TestUtil.extractDataArray(result, objectMapper);
					assertFalse(objects.isEmpty());
					Map<String, Object> copy = (Map<String, Object>) objects.getFirst();
					copyId = copy.get("copyId").toString();
				});
	}

	@Order(3)
	@Test
	void testGetCopyById() throws Exception {
		mockMvc.perform(get("/api/copies/{copyId}", copyId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(copyId, data.get("copyId").toString());
				});
	}

	@Order(4)
	@Test
	void testUpdateById() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("rackCode", "R-123");
		mockMvc.perform(put("/api/copies/{copyId}", copyId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals("R-123", data.get("rackCode"));
				});
	}

	@Order(5)
	@Test
	void testDeleteCopyById() throws Exception {
		mockMvc.perform(delete("/api/copies/{copyId}", copyId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					String jsonResponse = result.getResponse().getContentAsString();
					Map<String, Object> mapResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
					});
					String message = (String) mapResponse.get("data");
					assertEquals("Copy deleted successfully.", message);
				});
	}

	private static Map<String, Object> getRequestBook(String title) {
		Map<String, Object> request = new HashMap<>();
		int random = (int) (Math.random() * 100);
		request.put("isbn", "978-1111222" + random + "" + random);
		request.put("title", title);
		request.put("publisherName", "Penerbit Aksara Lestari");

		// list authors
		List<String> authorNames = new ArrayList<>();
		authorNames.add("Dewi Arum");
		request.put("authorNames", authorNames);

		// list categories
		List<String> categoryNames = new ArrayList<>();
		categoryNames.add("Petualangan");
		categoryNames.add("Thriller");
		request.put("categoryNames", categoryNames);

		request.put("publicationYear", "2020");
		request.put("language", "Indonesia");
		request.put("pageCount", 245);
		return request;
	}

	private static Map<String, Object> getRequestCopy() {
		Map<String, Object> copy = new HashMap<>();
		copy.put("rackCode", "R-E3");
		copy.put("quantity", 2);
		return copy;
	}
}