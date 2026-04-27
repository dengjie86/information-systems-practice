package com.dormrepair.order;

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
class StudentRepairOrderIntegrationTest {

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
            "student1",
            passwordEncoder.encode("123456"),
            "张三",
            "STUDENT",
            "13800000011",
            "1A",
            "101",
            1
        );
        jdbcTemplate.update(
            """
                INSERT INTO `user` (id, username, password, real_name, role, phone, dorm_building, dorm_room, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            2L,
            "student2",
            passwordEncoder.encode("123456"),
            "李四",
            "STUDENT",
            "13800000012",
            "2B",
            "202",
            1
        );
        jdbcTemplate.update(
            """
                INSERT INTO `user` (id, username, password, real_name, role, phone, dorm_building, dorm_room, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            3L,
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
            "门窗维修",
            "窗户损坏",
            2,
            0
        );

        jdbcTemplate.update(
            """
                INSERT INTO repair_order
                (id, order_no, user_id, title, category_id, description, image_url, dorm_building, dorm_room,
                 contact_phone, status, priority, reject_reason, admin_remark, submit_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
            11L,
            "WO202604250001",
            1L,
            "宿舍灯损坏",
            1L,
            "宿舍灯不亮了",
            "/img/deng.png",
            "1A",
            "101",
            "13800000011",
            "REJECTED",
            "HIGH",
            "信息不完整",
            "请补充更清晰的故障图片"
        );
        jdbcTemplate.update(
            """
                INSERT INTO repair_order
                (id, order_no, user_id, title, category_id, description, image_url, dorm_building, dorm_room,
                 contact_phone, status, priority, submit_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
            12L,
            "WO202604250002",
            2L,
            "窗户损坏",
            1L,
            "宿舍窗户开裂",
            "/img/chuanghu.png",
            "2B",
            "202",
            "13800000012",
            "PENDING_AUDIT",
            "NORMAL"
        );
    }

    @Test
    void 学生可以提交报修工单() throws Exception {
        String token = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                post("/api/orders")
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .content("""
                        {
                          "title":"空调漏水",
                          "categoryId":1,
                          "description":"床边一直滴水",
                          "imageUrl":"/img/kongtiao.png",
                          "contactPhone":"13800000999",
                          "priority":"URGENT"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").isNumber())
            .andExpect(jsonPath("$.data.orderNo").value(org.hamcrest.Matchers.startsWith("WO")))
            .andExpect(jsonPath("$.data.status").value("PENDING_AUDIT"));
    }

    @Test
    void 学生可以查看自己的工单列表和详情() throws Exception {
        String token = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                get("/api/orders/my")
                    .header("Authorization", "Bearer " + token)
                    .param("pageNum", "1")
                    .param("pageSize", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].orderNo").value("WO202604250001"))
            .andExpect(jsonPath("$.data.records[0].categoryName").value("水电维修"))
            .andExpect(jsonPath("$.data.records[0].rejectReason").value("信息不完整"));

        mockMvc.perform(
                get("/api/orders/11")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.orderNo").value("WO202604250001"))
            .andExpect(jsonPath("$.data.categoryName").value("水电维修"))
            .andExpect(jsonPath("$.data.dormBuilding").value("1A"))
            .andExpect(jsonPath("$.data.imageUrl").value("/img/deng.png"))
            .andExpect(jsonPath("$.data.status").value("REJECTED"))
            .andExpect(jsonPath("$.data.rejectReason").value("信息不完整"));
    }

    @Test
    void 停用分类不能提交且不能查看他人工单() throws Exception {
        String token = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                post("/api/orders")
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .content("""
                        {
                          "title":"窗户损坏",
                          "categoryId":2,
                          "description":"需要尽快处理"
                        }
                        """)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(409));

        mockMvc.perform(
                get("/api/orders/12")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void 管理员不能调用学生工单接口() throws Exception {
        String token = loginAndGetToken("admin", "123456");

        mockMvc.perform(
                get("/api/orders/my")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
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
