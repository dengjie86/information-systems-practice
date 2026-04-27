package com.dormrepair.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class UserCategoryIntegrationTest {

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
        jdbcTemplate.update("DELETE FROM repair_category");
        jdbcTemplate.update("DELETE FROM `user`");

        jdbcTemplate.update(
            """
                INSERT INTO `user` (id, username, password, real_name, role, phone, dorm_building, dorm_room, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            1L,
            "admin",
            passwordEncoder.encode("123456"),
            "系统管理员",
            "ADMIN",
            "13800000001",
            null,
            null,
            1
        );
        jdbcTemplate.update(
            """
                INSERT INTO `user` (id, username, password, real_name, role, phone, dorm_building, dorm_room, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            2L,
            "student1",
            passwordEncoder.encode("123456"),
            "张三",
            "STUDENT",
            "13800000011",
            "1号楼",
            "101",
            1
        );
        jdbcTemplate.update(
            """
                INSERT INTO `user` (id, username, password, real_name, role, phone, dorm_building, dorm_room, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            3L,
            "worker1",
            passwordEncoder.encode("123456"),
            "赵师傅",
            "WORKER",
            "13800000021",
            null,
            null,
            1
        );

        jdbcTemplate.update(
            """
                INSERT INTO repair_category (id, category_name, description, sort_order, status)
                VALUES (?, ?, ?, ?, ?)
                """,
            1L,
            "水电维修",
            "水管漏水、断电",
            1,
            1
        );
        jdbcTemplate.update(
            """
                INSERT INTO repair_category (id, category_name, description, sort_order, status)
                VALUES (?, ?, ?, ?, ?)
                """,
            2L,
            "窗户维修",
            "窗户损坏",
            2,
            0
        );
    }

    @Test
    void shouldAllowStudentUpdateDormInfo() throws Exception {
        String token = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                put("/api/user/dorm")
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .content("""
                        {"dormBuilding":"2号楼","dormRoom":"305","phone":"13800000099"}
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(
                get("/api/user/me")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.dormBuilding").value("2号楼"))
            .andExpect(jsonPath("$.data.dormRoom").value("305"))
            .andExpect(jsonPath("$.data.phone").value("13800000099"));
    }

    @Test
    void shouldReturnEnabledCategoriesToStudent() throws Exception {
        String token = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                get("/api/category/list")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].categoryName").value("水电维修"));
    }

    @Test
    void shouldRejectStudentAccessToAdminApis() throws Exception {
        String token = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                get("/api/user/list")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(
                get("/api/category/all")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldAllowAdminManageCategories() throws Exception {
        String token = loginAndGetToken("admin", "123456");

        mockMvc.perform(
                post("/api/category/add")
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .content("""
                        {"categoryName":"网络故障","description":"网络断连","sortOrder":3}
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(
                put("/api/category/2/status")
                    .header("Authorization", "Bearer " + token)
                    .param("status", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(
                get("/api/category/all")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void shouldAllowAdminDeleteOnlyUnusedCategories() throws Exception {
        String token = loginAndGetToken("admin", "123456");

        jdbcTemplate.update(
            """
                INSERT INTO repair_order
                (id, order_no, user_id, title, category_id, dorm_building, dorm_room, status, priority)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            21L,
            "WO202604280021",
            2L,
            "水管漏水",
            1L,
            "1号楼",
            "101",
            "PENDING_AUDIT",
            "NORMAL"
        );

        mockMvc.perform(
                delete("/api/category/2")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(
                delete("/api/category/1")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.msg").value("删除失败：该分类已被工单使用"));

        mockMvc.perform(
                get("/api/category/all")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].id").value(1));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType("application/json")
                    .content("""
                        {"username":"%s","password":"%s"}
                        """.formatted(username, password))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return body.path("data").path("token").asText();
    }
}
