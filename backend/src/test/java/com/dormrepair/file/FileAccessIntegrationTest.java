package com.dormrepair.file;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;
import com.dormrepair.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileAccessIntegrationTest {

    private static final byte[] PNG_BYTES = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p94AAAAASUVORK5CYII="
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM file_storage");
    }

    @Test
    void shouldStoreUploadedImageInDatabaseAndReadWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demo.png",
            "image/png",
            PNG_BYTES
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .header("Authorization", "Bearer " + adminToken())
                .param("type", "repair"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.matchesPattern("/api/files/\\d+")))
            .andReturn();

        String imageUrl = com.jayway.jsonpath.JsonPath.read(uploadResult.getResponse().getContentAsString(), "$.data");
        Integer storedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM file_storage WHERE file_type = 'repair' AND content_type = 'image/png' AND file_data IS NOT NULL",
            Integer.class
        );
        org.assertj.core.api.Assertions.assertThat(storedCount).isEqualTo(1);

        mockMvc.perform(get(imageUrl))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andExpect(content().bytes(PNG_BYTES));
    }

    @Test
    void shouldRejectNonImageContent() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "fake.png",
            "image/png",
            "not an image".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .header("Authorization", "Bearer " + adminToken())
                .param("type", "repair"))
            .andExpect(status().isBadRequest());
    }

    private String adminToken() {
        return jwtTokenProvider.generateToken(1L, "admin", "ADMIN");
    }
}
