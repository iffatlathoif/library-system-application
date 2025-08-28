package com.enigma.library_app.controller.loan;

import com.enigma.library_app.auth.security.JwtService;
import com.enigma.library_app.controller.constants.Constants;
import com.enigma.library_app.util.TestUtil;
import com.enigma.library_app.dto.loan.request.*;
import com.enigma.library_app.dto.loan.response.LoanResponse;
import com.enigma.library_app.model.transaction.loan.constant.LoanStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;

    private static String staffToken;
    private static String memberToken;
    private static String adminToken;
    private static String loanIdByStaff;
    private static String loanIdByMember;
    private static String loanId;
    private static String memberId;
    private static String bookId1;
    private static Long copyId1;
    private static String bookId2;
    private static Long copyId2;
    private static Long locationId;

    @BeforeAll
    static void beforeAll() {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    @BeforeEach
    void setUp() {
        staffToken = "Bearer " + jwtService.generateToken("sugihpratama");
        memberToken = "Bearer " + jwtService.generateToken("tamasugih");
        adminToken = "Bearer " + jwtService.generateToken("sugihprtm");


        memberId = "2fc19bcf-e662-4f5c-a9fb-e820076148d1";
        bookId1 = "d6c6f435-a460-4ca5-bd25-6ba885ae8e87";
        bookId2 = "736523c6-bc8c-4cd3-82de-db5fcc738c4a";

        copyId2 = 162L;
        locationId = 2L;
    }

    @Order(1)
    @Test
    void testCreateLoanByStaff() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", memberId);

        Map<String, Object> item = new HashMap<>();
        item.put("bookId", bookId1);
        item.put("quantity", 1);

        request.put("items", List.of(item));

        mockMvc.perform(post("/api/loan")
                        .header(AUTHORIZATION, staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals(memberId, data.get("memberId"));
                    assertNotNull(data.get("loanId"));
                    assertEquals(LoanStatus.ONGOING.toString(), data.get("status"));
                    loanIdByStaff = data.get("loanId").toString();
                    log.info(loanIdByStaff);
                });
    }

    @Order(2)
    @Test
    void testCreateLoanByMember() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("bookId", bookId2);
        request.put("copyId", copyId2);

        mockMvc.perform(post("/api/loan/member")
                        .header(AUTHORIZATION, memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals("Sugih Pratama", data.get("memberName"));
                    assertNotNull(data.get("loanId"));
                    assertEquals(LoanStatus.REQUESTED.toString(), data.get("status"));

                    loanIdByMember = data.get("loanId").toString();
                });
    }

    @Order(3)
    @Test
    void testGetLoanById() throws Exception {
        mockMvc.perform(get("/api/loan/{loanId}", loanIdByStaff)
                        .header(AUTHORIZATION, staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals(loanIdByStaff, data.get("loanId"));
                });
    }

    @Order(4)
    @Test
    void testGetAllLoans() throws Exception {
        mockMvc.perform(get("/api/loan")
                        .header(AUTHORIZATION, staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    List<Object> loans = TestUtil.extractDataArray(result, objectMapper);
                    assertFalse(loans.isEmpty());
                });
    }

    @Order(5)
    @Test
    void testUpdateLoan() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("status", LoanStatus.ONGOING.toString());

        Map<String, Object> item = new HashMap<>();
        item.put("bookId", bookId1);
        item.put("quantity", 2);

        request.put("items", List.of(item));

        mockMvc.perform(put("/api/loan/{loanId}", loanIdByStaff)
                        .header(AUTHORIZATION, staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals(loanIdByStaff, data.get("loanId"));
                    assertEquals(LoanStatus.ONGOING.toString(), data.get("status"));
                });
    }

    @Order(6)
    @Test
    void testVerifyLoanRequest() throws Exception {
        mockMvc.perform(post("/api/loan/{loanId}/verify", loanIdByMember)
                        .header(AUTHORIZATION, staffToken)
                        .param("approve", "true")
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals(LoanStatus.ONGOING.toString(), data.get("status"));
                });
    }

    @Order(7)
    @Test
    void testReturnLoan() throws Exception {
        mockMvc.perform(post("/api/loan/{loanId}/return", loanIdByMember)
                        .header(AUTHORIZATION, staffToken)
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    Map<String, Object> data = TestUtil.extractData(result, objectMapper);
                    assertEquals(LoanStatus.RETURNED.toString(), data.get("status"));
                });
    }

    @Order(8)
    @Test
    void testGetLoanReport() throws Exception {
        mockMvc.perform(get("/api/loan/report")
                        .header(AUTHORIZATION, staffToken)
                        .param("page", "0")
                        .param("size", "10")
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    String json = result.getResponse().getContentAsString();
                    Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                    List<Map<String, Object>> reports = (List<Map<String, Object>>) data.get("content");

                    assertNotNull(reports, "Loan report content must not be null");
                    assertFalse(reports.isEmpty(), "Loan report content must not be empty");

                    for (Map<String, Object> report : reports) {
                        assertNotNull(report.get("loanId"), "Loan ID must not be null");
                        assertNotNull(report.get("memberName"), "Member name must not be null");
                        assertNotNull(report.get("bookTitle"), "Book title must not be null");
                        assertNotNull(report.get("locationName"), "Location name must not be null");
                        assertNotNull(report.get("status"), "Loan status must not be null");
                    }
                });
    }

    @Order(9)
    @Test
    void testGetLoanHistoryByMember() throws Exception {
        mockMvc.perform(get("/api/loan/history/member/{memberId}", memberId)
                        .header(AUTHORIZATION, adminToken)
                        .param("page", "0")
                        .param("size", "10")
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    List<Object> history = TestUtil.extractDataArray(result, objectMapper);
                    assertFalse(history.isEmpty());
                });
    }

    @Order(10)
    @Test
    void testGetLoanHistoryByBook() throws Exception {
        mockMvc.perform(get("/api/loan/history/book/{bookId}", bookId1)
                        .header(AUTHORIZATION, adminToken)
                        .param("page", "0")
                        .param("size", "10")
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    List<Object> history = TestUtil.extractDataArray(result, objectMapper);
                    assertFalse(history.isEmpty());
                });
    }

    @Order(11)
    @Test
    void testGetLoanHistoryByStatus() throws Exception {
        mockMvc.perform(get("/api/loan/history/status")
                        .header(AUTHORIZATION, adminToken)
                        .param("status", "ONGOING")
                        .param("page", "0")
                        .param("size", "10")
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    List<Object> history = TestUtil.extractDataArray(result, objectMapper);
                    assertFalse(history.isEmpty());
                });
    }

    @Order(12)
    @Test
    void testGetLoanHistoryByDueDate() throws Exception {
        mockMvc.perform(get("/api/loan/history/duedate")
                        .header(AUTHORIZATION, adminToken)
                        .param("dueDate", "2025-08-11")
                        .param("page", "0")
                        .param("size", "10")
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    List<Object> history = TestUtil.extractDataArray(result, objectMapper);
                    assertNotNull(history); // Pastikan response tidak null
                    // Optional logging
                    log.info("Loan history by dueDate contains {} items", history.size());
                });
    }

    @Order(13)
    @Test
    void testGetLoanHistoryByLocation() throws Exception {
        mockMvc.perform(get("/api/loan/history/location/{locationId}", locationId)
                        .header(AUTHORIZATION, adminToken)
                        .param("page", "0")
                        .param("size", "10")
                ).andExpectAll(status().isOk())
                .andDo(result -> {
                    String json = result.getResponse().getContentAsString();
                    Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
                    List<Object> history = (List<Object>) responseMap.get("data");
                    assertNotNull(history);
                    // Optional: log ukuran data
                    log.info("Loan history size: {}", history.size());
                });
    }
}