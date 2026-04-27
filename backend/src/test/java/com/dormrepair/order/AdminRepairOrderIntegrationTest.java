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
class AdminRepairOrderIntegrationTest {

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
                INSERT INTO `user` (id, username, password, real_name, role, phone, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
            2L,
            "admin",
            passwordEncoder.encode("123456"),
            "系统管理员",
            "ADMIN",
            "13800000001",
            1
        );
        jdbcTemplate.update(
            """
                INSERT INTO `user` (id, username, password, real_name, role, phone, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
            3L,
            "worker1",
            passwordEncoder.encode("123456"),
            "王师傅",
            "WORKER",
            "13800000021",
            1
        );
        jdbcTemplate.update(
            """
                INSERT INTO `user` (id, username, password, real_name, role, phone, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
            4L,
            "worker2",
            passwordEncoder.encode("123456"),
            "赵师傅",
            "WORKER",
            "13800000022",
            0
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
                INSERT INTO repair_order
                (id, order_no, user_id, title, category_id, description, image_url, dorm_building, dorm_room,
                 contact_phone, status, priority, submit_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
            11L,
            "WO202604250011",
            1L,
            "宿舍灯损坏",
            1L,
            "宿舍灯不亮了",
            "/img/deng.png",
            "1A",
            "101",
            "13800000011",
            "PENDING_AUDIT",
            "HIGH"
        );
        jdbcTemplate.update(
            """
                INSERT INTO repair_order
                (id, order_no, user_id, title, category_id, description, image_url, dorm_building, dorm_room,
                 contact_phone, status, priority, admin_remark, submit_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
            12L,
            "WO202604250012",
            1L,
            "水管漏水",
            1L,
            "洗手池下方漏水",
            "/img/shuiguan.png",
            "1A",
            "101",
            "13800000011",
            "PENDING_ASSIGN",
            "URGENT",
            "尽快安排维修"
        );
        jdbcTemplate.update(
            """
                INSERT INTO repair_order
                (id, order_no, user_id, title, category_id, description, image_url, dorm_building, dorm_room,
                 contact_phone, status, priority, reject_reason, submit_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
            13L,
            "WO202604250013",
            1L,
            "门锁损坏",
            1L,
            "门锁卡住了",
            "/img/mensuo.png",
            "1A",
            "101",
            "13800000011",
            "REJECTED",
            "NORMAL",
            "描述不够清楚"
        );
    }

    @Test
    void 管理员可以查看工单列表和详情() throws Exception {
        String token = loginAndGetToken("admin", "123456");

        mockMvc.perform(
                get("/api/orders/admin")
                    .header("Authorization", "Bearer " + token)
                    .param("status", "PENDING_AUDIT")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].orderNo").value("WO202604250011"))
            .andExpect(jsonPath("$.data.records[0].studentName").value("张三"));

        mockMvc.perform(
                get("/api/orders/admin/12")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.orderNo").value("WO202604250012"))
            .andExpect(jsonPath("$.data.studentName").value("张三"))
            .andExpect(jsonPath("$.data.adminRemark").value("尽快安排维修"));
    }

    @Test
    void 管理员可以审核通过并分派维修人员() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String studentToken = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                post("/api/orders/admin/11/approve")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .content("""
                        {
                          "adminRemark":"信息已核实"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(
                get("/api/orders/admin/11")
                    .header("Authorization", "Bearer " + adminToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING_ASSIGN"))
            .andExpect(jsonPath("$.data.adminRemark").value("信息已核实"));

        mockMvc.perform(
                post("/api/orders/admin/11/assign")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .content("""
                        {
                          "workerId":3,
                          "dispatchRemark":"今晚前上门处理"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(
                get("/api/orders/admin/11")
                    .header("Authorization", "Bearer " + adminToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING_ACCEPT"))
            .andExpect(jsonPath("$.data.assignedWorkerId").value(3))
            .andExpect(jsonPath("$.data.assignedWorkerName").value("王师傅"))
            .andExpect(jsonPath("$.data.adminRemark").value("信息已核实"))
            .andExpect(jsonPath("$.data.dispatchRemark").value("今晚前上门处理"));

        mockMvc.perform(
                get("/api/orders/11")
                    .header("Authorization", "Bearer " + studentToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING_ACCEPT"))
            .andExpect(jsonPath("$.data.adminRemark").value("信息已核实"))
            .andExpect(jsonPath("$.data.dispatchRemark").doesNotExist());
    }

    @Test
    void 管理员可以驳回工单且学生能看到驳回原因() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String studentToken = loginAndGetToken("student1", "123456");

        mockMvc.perform(
                post("/api/orders/admin/11/reject")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .content("""
                        {
                          "rejectReason":"宿舍号和故障描述不完整",
                          "adminRemark":"这条旧字段不应该再保存"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(
                get("/api/orders/11")
                    .header("Authorization", "Bearer " + studentToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("REJECTED"))
            .andExpect(jsonPath("$.data.rejectReason").value("宿舍号和故障描述不完整"))
            .andExpect(jsonPath("$.data.adminRemark").doesNotExist());
    }

    @Test
    void 非管理员不能审核且非法状态不能直接分派() throws Exception {
        String studentToken = loginAndGetToken("student1", "123456");
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(
                post("/api/orders/admin/11/approve")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType("application/json")
                    .content("{}")
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(
                post("/api/orders/admin/13/assign")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .content("""
                        {
                          "workerId":3
                        }
                        """)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(409));

        mockMvc.perform(
                post("/api/orders/admin/12/assign")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .content("""
                        {
                          "workerId":4
                        }
                        """)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(409));
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
