package cn.techstar.pvms.backend.module.stations.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnResourceUnitList() throws Exception {
        mockMvc.perform(get("/api/pvms/resource-units/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(6))
            .andExpect(jsonPath("$.data.items.length()").value(6))
            .andExpect(jsonPath("$.data.items[0].id").value("RU-001"))
            .andExpect(jsonPath("$.data.items[0].stationCount").value(4));
    }

    @Test
    void shouldFilterResourceUnitListByKeywordAndStatus() throws Exception {
        mockMvc.perform(
                get("/api/pvms/resource-units/list")
                    .param("keyword", "合肥")
                    .param("status", "maintenance")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.items[0].id").value("RU-004"));
    }

    @Test
    void shouldReturnResourceUnitOverview() throws Exception {
        mockMvc.perform(get("/api/pvms/resource-units/RU-001/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.resourceUnitId").value("RU-001"))
            .andExpect(jsonPath("$.data.name").exists())
            .andExpect(jsonPath("$.data.stationCount").value(4))
            .andExpect(jsonPath("$.data.stations.length()").value(4));
    }

    @Test
    void shouldReturnResourceUnitPowerCurve() throws Exception {
        mockMvc.perform(get("/api/pvms/resource-units/RU-001/power-curve").param("date", "2026-03-30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.resourceUnitId").value("RU-001"))
            .andExpect(jsonPath("$.data.date").value("2026-03-30"))
            .andExpect(jsonPath("$.data.pvPower.length()").value(96))
            .andExpect(jsonPath("$.data.forecastPower.length()").value(96));
    }
}
