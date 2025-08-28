package com.enigma.library_app.controller.book;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtService jwtService;
	private static String token;
	private static String tokenMember;
	private static String bookId;
	private static byte[] imageBytes;

	@BeforeAll
	static void beforeAll() throws IOException {
		imageBytes = Files.readAllBytes(Paths.get("src/test/resources/cover.jpg"));
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
	}

	@BeforeEach
	void setUp() {
		token = "Bearer " + jwtService.generateToken(Constants.USER_STAFF);
		tokenMember = "Bearer " + jwtService.generateToken(Constants.USER_MEMBER);
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
	void testGetAllBooks() throws Exception {
		mockMvc.perform(get("/api/books")
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpectAll(status().isOk())
				.andExpect(result -> {
					List<Object> objects = TestUtil.extractDataArray(result, objectMapper);
					assertFalse(objects.isEmpty());
				});
	}

	@Order(3)
	@Test
	void testGetBookId() throws Exception {
		mockMvc.perform(get("/api/books/{bookId}", bookId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andExpect(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(bookId, data.get("id"));
				});
	}

	@Order(4)
	@Test
	void testGetAllBooksByCategory() throws Exception {
		mockMvc.perform(get("/api/books/category/{categoryId}", 1)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					List<Object> objects = TestUtil.extractDataArray(result, objectMapper);
					Map<String, Object> book = (Map<String, Object>) objects.getFirst();
					List<Map<String, Object>> categories = (List<Map<String, Object>>) book.get("categories");
					Integer categoryId = (Integer) categories.get(0).get("categoryId");
					assertEquals(1, categoryId);

				});
	}

	@Order(5)
	@Test
	void testUpdateBookById() throws Exception {
		String title = "Jejak Langkah di Hutan Senyap 4";
		Map<String, Object> request = getRequestBook(title);
		mockMvc.perform(put("/api/books/{bookId}", bookId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					Map<String, Object> data = TestUtil.extractData(result, objectMapper);
					assertEquals(bookId, data.get("id"));
					assertEquals(title, data.get("title"));
				});
	}

	@Order(6)
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
				});
	}

	@Order(7)
	@Test
	void testAvailabilityBookByLocationFaculty() throws Exception {
		mockMvc.perform(get("/api/books/availability")
						.header(AUTHORIZATION, tokenMember)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk())
				.andDo(result -> {
					List<Object> objects = TestUtil.extractDataArray(result, objectMapper);
					assertFalse(objects.isEmpty());
				});
	}

	@Order(8)
	@Test
	void testUploadBookCover() throws Exception {
		MockMultipartFile mockFile = new MockMultipartFile(
				"file",
				"buku.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				imageBytes
		);

		mockMvc.perform(multipart("/api/books/{bookId}/cover", bookId)
						.file(mockFile)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.MULTIPART_FORM_DATA)
				)
				.andExpectAll(status().isOk())
				.andDo(result -> {
					String jsonResponse = result.getResponse().getContentAsString();
					Map<String, Object> mapResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
					});
					String message = (String) mapResponse.get("data");
					assertEquals("Upload book image successfully.", message);
				});
	}

	@Order(9)
	@Test
	void testDeleteBookById() throws Exception {
		mockMvc.perform(delete("/api/books/{bookId}", bookId)
						.header(AUTHORIZATION, token)
						.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpectAll(status().isOk())
				.andDo(result -> {
					String jsonResponse = result.getResponse().getContentAsString();
					Map<String, Object> mapResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
					});
					String message = (String) mapResponse.get("data");
					assertEquals("Book deleted successfully", message);
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