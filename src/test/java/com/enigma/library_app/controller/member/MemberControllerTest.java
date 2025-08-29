package com.enigma.library_app.controller.member;

import com.enigma.library_app.service.JwtService;
import com.enigma.library_app.model.Faculty;
import com.enigma.library_app.repository.FacultyRepository;
import com.enigma.library_app.repository.MemberRepository;
import com.enigma.library_app.repository.UserRepository;
import com.enigma.library_app.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private static String adminToken;
    private static String memberToken;
    private static String memberId;
    private static String facultyCode = "CS";
    private static byte[] imageBytes;


    @BeforeAll
    static void beforeAll() throws IOException {
        imageBytes = Files.readAllBytes(Paths.get("src/test/resources/Foto.png"));
    }

    @BeforeEach
    void setUp() {
        if (!facultyRepository.existsById(facultyCode)) {
            Faculty faculty = Faculty.builder()
                    .facultyCode(facultyCode)
                    .name("Computer Science")
                    .build();
            facultyRepository.save(faculty);
        }
        adminToken = "Bearer " + jwtService.generateToken("sugihprtm");
        memberToken = "Bearer " + jwtService.generateToken("tamasugih");
    }
    private Map<String, Object> getMemberRequest(String username, String name, String nisNip) {
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", "password123");
        request.put("name", name);
        request.put("nisNip", nisNip);
        request.put("email", username + "@example.com");
        request.put("phone", "08123456789");
        request.put("facultyCode", facultyCode);
        request.put("role", "MEMBER");
        request.put("type", "MAHASISWA");
        return request;
    }

    private Map<String, Object> getUpdateRequest(String name, String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("username", "test_user");
        request.put("password", "newpassword123");
        request.put("name", name);
        request.put("nisNip", "987654");
        request.put("email", email);
        request.put("phone", "08987654321");
        request.put("facultyCode", "CS");
        request.put("role", "MEMBER");
        request.put("type", "MAHASISWA");
        request.put("status", "ACTIVE");
        return request;
    }

    @Order(1)
    @Test
    void createMemberByAdmin() throws Exception {
        Map<String, Object> request = getMemberRequest("test_member", "Test Member", "123456");

        mockMvc.perform(post("/api/member")
                        .header(AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals("Test Member", data.get("name"));
                    assertEquals("test_member", data.get("username"));
                    memberId = data.get("id").toString();
                });
    }

    @Test
    @Order(2)
    void getMemberById() throws Exception {
        mockMvc.perform(get("/api/member/{memberId}", memberId)
                        .header(AUTHORIZATION, adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals(memberId, data.get("id"));
                    assertEquals("Test Member", data.get("name"));
                });
    }

    @Test
    @Order(3)
    void updateMember() throws Exception {
        Map<String, Object> request = getUpdateRequest("Updated Member", "updated@example.com");

        mockMvc.perform(put("/api/member/{memberId}", memberId)
                        .header(AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals("Updated Member", data.get("name"));
                    assertEquals("updated@example.com", data.get("email"));
                });
    }

    @Test
    @Order(4)
    void getAllMembers() throws Exception {
        mockMvc.perform(get("/api/member")
                        .header(AUTHORIZATION, adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk())
                .andExpect(result -> {
                    List<Object> objects = TestUtil.extractDataArray(result, objectMapper);
                    assertFalse(objects.isEmpty());
                });
    }

    @Test
    @Order(5)
    void uploadPhoto() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "Foto.png",
                MediaType.IMAGE_PNG_VALUE,
                imageBytes
        );// For file upload test you might need to use MockMultipartFile
        // This is a simplified version
        mockMvc.perform(multipart("/api/member/{memberId}/upload-photo", memberId)
                        .file(mockFile)
                        .header(AUTHORIZATION, memberToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertNotNull(data.get("photo"));
                });
    }
    @Order(6)
    @Test
    void deleteMember() throws Exception {
        mockMvc.perform(delete("/api/member/{memberId}", memberId)
                        .header(AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    String jsonResponse = result.getResponse().getContentAsString();
                    Map<String, Object> mapResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
                    });
                    String message = (String) mapResponse.get("data");
                    assertEquals("Member Updated Member dengan Username test_member berhasil dihapus !", message);
                });
    }

    @Order(7)
    @Test
    void deleteMember_notFound() throws Exception {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(delete("/api/member/{memberId}", nonExistentId)
                        .header(AUTHORIZATION, adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors").exists()
                );
    }

    @Order(8)
    @Test
    void deleteMember_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/member/{memberId}", memberId)
                        .header(AUTHORIZATION, memberToken) // Using member token instead of admin
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}