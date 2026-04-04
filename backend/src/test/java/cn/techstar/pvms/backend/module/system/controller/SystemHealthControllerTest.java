package cn.techstar.pvms.backend.module.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
class SystemHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBackendHealthStatus() throws Exception {
        mockMvc.perform(get("/api/system/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.service").value("pvms-backend"))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }
}

