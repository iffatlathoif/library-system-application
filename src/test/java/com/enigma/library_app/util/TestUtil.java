package com.enigma.library_app.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtil {
	public static Map<String, Object> extractData(MvcResult result, ObjectMapper objectMapper) throws Exception {
		String json = result.getResponse().getContentAsString();
		Map<String, Object> response = objectMapper.readValue(json, new TypeReference<>() {
		});
		assertNull(response.get("errors"));
		return (Map<String, Object>) response.get("data");
	}

	public static List<Object> extractDataArray(MvcResult result, ObjectMapper objectMapper) throws Exception {
		String json = result.getResponse().getContentAsString();
		Map<String, Object> response = objectMapper.readValue(json, new TypeReference<>() {
		});
		assertNull(response.get("errors"));
		return (List<Object>) response.get("data");
	}

	public static String extractError(MvcResult result, ObjectMapper objectMapper) throws Exception {
		String json = result.getResponse().getContentAsString();
		Map<String, Object> response = objectMapper.readValue(json, new TypeReference<>() {
		});
		assertNotNull(response.get("errors"));
		assertNull(response.get("data"));
		return (String) response.get("errors");
	}
}
