package com.dormrepair.file;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileAccessIntegrationTest {

    private static final Path UPLOAD_DIR = Path.of(
        System.getProperty("java.io.tmpdir"),
        "dorm-repair-file-access-test"
    );

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureFileStorage(DynamicPropertyRegistry registry) {
        registry.add("file.upload-dir", () -> UPLOAD_DIR.toString());
    }

    @BeforeEach
    void setUp() throws Exception {
        Path repairDir = UPLOAD_DIR.resolve("repair");
        Files.createDirectories(repairDir);
        Files.write(
            repairDir.resolve("demo.png"),
            new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
            }
        );
    }

    @Test
    void shouldAllowImageAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/files/repair/demo.png"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"));
    }
}
