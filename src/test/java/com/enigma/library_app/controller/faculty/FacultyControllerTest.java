package com.enigma.library_app.controller.faculty;
import com.enigma.library_app.auth.security.JwtService;
import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.model.master.location.entity.Faculty;
import com.enigma.library_app.repository.FacultyRepository;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FacultyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private FacultyRepository facultyRepository;


    private static String facultyCode;
    private static String adminToken;

    @BeforeAll
    static void beforeAll() {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        facultyCode = "EE"; // ganti kode agar tidak bentrok dengan data CS yang sudah ada
    }

    @BeforeEach
    void setUp() {
        adminToken = "Bearer " + jwtService.generateToken("sugihprtm");

        if (!facultyRepository.existsById(facultyCode)) {
            Faculty faculty = Faculty.builder()
                    .facultyCode(facultyCode)
                    .name("Electrical Engineering")
                    .build();
            facultyRepository.save(faculty);
        }
    }

    private Map<String, Object> getFacultyRequest(String code, String name) {
        Map<String, Object> request = new HashMap<>();
        request.put("facultyCode", code);
        request.put("name", name);
        return request;
    }

    @Order(1)
    @Test
    void createFaculty() throws Exception {
        Map<String, Object> request = getFacultyRequest("ME", "Mechanical Engineering");

        mockMvc.perform(post("/api/faculty")
                        .header(AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals("ME", data.get("facultyCode"));
                    assertEquals("Mechanical Engineering", data.get("name"));
                });
    }

    @Test
    @Order(2)
    void getFacultyById() throws Exception {
        mockMvc.perform(get("/api/faculty/{code}", facultyCode)
                        .header(AUTHORIZATION, adminToken))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals(facultyCode, data.get("facultyCode"));
                    assertEquals("Electrical Engineering", data.get("name"));
                });
    }

    @Test
    @Order(3)
    void updateFaculty() throws Exception {
        Map<String, Object> request = getFacultyRequest(facultyCode, "Electrical & Electronics Engineering");

        mockMvc.perform(put("/api/faculty/{code}", facultyCode)
                        .header(AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals("Electrical & Electronics Engineering", data.get("name"));
                });
    }

    @Test
    @Order(4)
    void getAllFaculties() throws Exception {
        mockMvc.perform(get("/api/faculty")
                        .header(AUTHORIZATION, adminToken))
                .andExpectAll(status().isOk())
                .andExpect(result -> {
                    List<Object> list = TestUtil.extractDataArray(result, objectMapper);
                    assertFalse(list.isEmpty());
                });
    }

    @Test
    @Order(5)
    void deleteFaculty() throws Exception {
        mockMvc.perform(delete("/api/faculty/{code}", facultyCode)
                        .header(AUTHORIZATION, adminToken))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    String json = result.getResponse().getContentAsString();
                    BaseResponse<String> resp = objectMapper.readValue(json, new TypeReference<>(){

                    });
                    assertEquals("Faculty "+facultyCode+" Telah Dihapus!", resp.getData());
                });
    }

    @Test
    @Order(6)
    void deleteFaculty_notFound() throws Exception {
        mockMvc.perform(delete("/api/faculty/{code}", "ZZ")
                        .header(AUTHORIZATION, adminToken))
                .andExpectAll(status().isNotFound());
    }

    @Test
    @Order(7)
    void deleteFaculty_unauthorized() throws Exception {
        String memberToken = "Bearer " + jwtService.generateToken("tamasugih");

        mockMvc.perform(delete("/api/faculty/{code}", facultyCode)
                        .header(AUTHORIZATION, memberToken))
                .andExpect(status().isForbidden());
    }
}