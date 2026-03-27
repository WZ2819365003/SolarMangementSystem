package cn.techstar.pvms.backend.module.stations.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnResourceUnitList() throws Exception {
        mockMvc.perform(get("/api/pvms/resource-units/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.statistics.length()").value(4))
            .andExpect(jsonPath("$.data.total").value(6))
            .andExpect(jsonPath("$.data.list.length()").value(6))
            .andExpect(jsonPath("$.data.list[0].id").value("RU-001"))
            .andExpect(jsonPath("$.data.list[0].stationCount").value(6))
            .andExpect(jsonPath("$.data.list[0].dispatchableCapacityMw").value(5.2));
    }

    @Test
    void shouldFilterResourceUnitListByKeywordAndStatus() throws Exception {
        mockMvc.perform(
                get("/api/pvms/resource-units/list")
                    .param("keyword", "华东")
                    .param("status", "warning")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list.length()").value(1))
            .andExpect(jsonPath("$.data.list[0].id").value("RU-004"));
    }

    @Test
    void shouldReturnResourceUnitOverview() throws Exception {
        mockMvc.perform(get("/api/pvms/resource-units/RU-001/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.info.name").value("华南工商业聚合单元 A"))
            .andExpect(jsonPath("$.data.kpi.realtimePowerMw").value(3.86))
            .andExpect(jsonPath("$.data.dispatchSummary.successRate").value(92.9))
            .andExpect(jsonPath("$.data.alarmSummary.total").value(4))
            .andExpect(jsonPath("$.data.memberStations.length()").value(3))
            .andExpect(jsonPath("$.data.weather.temperature").value(28));
    }

    @Test
    void shouldReturnResourceUnitPowerCurve() throws Exception {
        mockMvc.perform(get("/api/pvms/resource-units/RU-001/power-curve").param("date", "2026-03-23"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.resourceUnitId").value("RU-001"))
            .andExpect(jsonPath("$.data.currentDate").value("2026-03-23"))
            .andExpect(jsonPath("$.data.actual.length()").value(96))
            .andExpect(jsonPath("$.data.forecast.length()").value(96))
            .andExpect(jsonPath("$.data.baseline.length()").value(96))
            .andExpect(jsonPath("$.data.irradiance.length()").value(96));
    }
}
