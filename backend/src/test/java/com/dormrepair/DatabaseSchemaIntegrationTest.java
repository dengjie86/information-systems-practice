package com.dormrepair;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseSchemaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void h2SchemaShouldIncludeProductionTablesAndColumnsUsedByCurrentCode() {
        List<String> tables = jdbcTemplate.queryForList(
            """
                SELECT UPPER(TABLE_NAME)
                FROM INFORMATION_SCHEMA.TABLES
                """,
            String.class
        );

        assertThat(tables).contains("USER", "REPAIR_CATEGORY", "REPAIR_ORDER", "REPAIR_RECORD", "EVALUATION");

        List<String> repairOrderColumns = columnsOf("REPAIR_ORDER");
        assertThat(repairOrderColumns).contains("DISPATCH_REMARK");

        List<String> repairRecordColumns = columnsOf("REPAIR_RECORD");
        assertThat(repairRecordColumns).contains("ORDER_ID", "WORKER_ID", "ACTION_TYPE", "STATUS_BEFORE", "STATUS_AFTER");

        List<String> evaluationColumns = columnsOf("EVALUATION");
        assertThat(evaluationColumns).contains("ORDER_ID", "USER_ID", "SCORE", "CONTENT");
    }

    private List<String> columnsOf(String tableName) {
        return jdbcTemplate.queryForList(
            """
                SELECT UPPER(COLUMN_NAME)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE UPPER(TABLE_NAME) = ?
                """,
            String.class,
            tableName
        );
    }
}
