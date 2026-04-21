package cn.techstar.pvms.backend.module.productionmonitor.controller;

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
class ProductionMonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnProductionMonitorMeta() throws Exception {
        mockMvc.perform(get("/api/pvms/production-monitor/meta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.defaultResourceUnitId").value("RU-001"))
            .andExpect(jsonPath("$.data.resourceUnits.length()").value(6))
            .andExpect(jsonPath("$.data.resourceUnits[0].clusterRadiusKm").value(8))
            .andExpect(jsonPath("$.data.resourceUnits[0].stationIds.length()").value(4))
            .andExpect(jsonPath("$.data.stations.length()").value(21))
            .andExpect(jsonPath("$.data.stations[0].id").value("SZ-001"))
            .andExpect(jsonPath("$.data.stations[0].resourceUnitId").value("RU-001"));
    }

    @Test
    void shouldReturnOverviewForResourceUnit() throws Exception {
        mockMvc.perform(get("/api/pvms/production-monitor/overview").param("resourceUnitId", "RU-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.info.name").value("深圳湾科创园聚合单元"))
            .andExpect(jsonPath("$.data.kpis.length()").value(6))
            .andExpect(jsonPath("$.data.memberStations.length()").value(4))
            .andExpect(jsonPath("$.data.weatherBrief.irradiance").value(746))
            .andExpect(jsonPath("$.data.alarmBrief.total").value(2));
    }

    @Test
    void shouldReturnOutputViewForResourceUnit() throws Exception {
        mockMvc.perform(
                get("/api/pvms/production-monitor/output")
                    .param("resourceUnitId", "RU-001")
                    .param("granularity", "15m")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summary.length()").value(4))
            .andExpect(jsonPath("$.data.curve.axis.length()").value(96))
            .andExpect(jsonPath("$.data.contributionRanking.length()").value(4))
            .andExpect(jsonPath("$.data.table.length()").value(8));
    }

    @Test
    void shouldAggregateOutputViewToThirtyMinutes() throws Exception {
        mockMvc.perform(
                get("/api/pvms/production-monitor/output")
                    .param("resourceUnitId", "RU-001")
                    .param("granularity", "30m")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summary[3].value").value("30"))
            .andExpect(jsonPath("$.data.curve.axis.length()").value(48))
            .andExpect(jsonPath("$.data.curve.actual.length()").value(48));
    }

    @Test
    void shouldReturnDispatchViewForResourceUnit() throws Exception {
        mockMvc.perform(get("/api/pvms/production-monitor/dispatch").param("resourceUnitId", "RU-004"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summary.length()").value(6))
            .andExpect(jsonPath("$.data.executionTrend.axis.length()").value(6))
            .andExpect(jsonPath("$.data.riskHints.length()").value(2))
            .andExpect(jsonPath("$.data.records.length()").value(6));
    }

    @Test
    void shouldReturnLoadViewForResourceUnit() throws Exception {
        mockMvc.perform(
                get("/api/pvms/production-monitor/load")
                    .param("resourceUnitId", "RU-001")
                    .param("date", "2026-03-30")
                    .param("granularity", "15m")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summary.totalLoadMw").isNumber())
            .andExpect(jsonPath("$.data.summary.stationCount").value(16))
            .andExpect(jsonPath("$.data.summary.onlineCount").value(14))
            .andExpect(jsonPath("$.data.stations.length()").value(16))
            .andExpect(jsonPath("$.data.stations[0].gridInteraction.times.length()").value(96));
    }

    @Test
    void shouldReturnWeatherViewForResourceUnit() throws Exception {
        mockMvc.perform(get("/api/pvms/production-monitor/weather").param("resourceUnitId", "RU-002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summary.length()").value(4))
            .andExpect(jsonPath("$.data.trend.axis.length()").value(8))
            .andExpect(jsonPath("$.data.impactTable.length()").value(2))
            .andExpect(jsonPath("$.data.impactTable[0].timeRange").value("今天 12:00-15:00"));
    }
}
