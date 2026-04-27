package com.dormrepair.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM evaluation");
        jdbcTemplate.update("DELETE FROM repair_record");
        jdbcTemplate.update("DELETE FROM repair_order");
        jdbcTemplate.update("DELETE FROM `user`");
        jdbcTemplate.update(
            """
                INSERT INTO `user` (username, password, real_name, role, phone, dorm_building, dorm_room, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            "admin",
            passwordEncoder.encode("123456"),
            "系统管理员",
            "ADMIN",
            "13800000001",
            null,
            null,
            1
        );
    }

    @Test
    void shouldLoginAndAccessProtectedEndpoint() throws Exception {
        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType("application/json")
                    .content("""
                        {"username":"admin","password":"123456"}
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").isString())
            .andExpect(jsonPath("$.data.userInfo.role").value("ADMIN"))
            .andReturn();

        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = body.path("data").path("token").asText();
        assertThat(token).isNotBlank();

        mockMvc.perform(
                get("/api/user/me")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void shouldRejectWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/user/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
    }
}
