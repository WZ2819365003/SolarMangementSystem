package cn.techstar.pvms.backend.module.dashboard.controller;

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
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDashboardStationsGeo() throws Exception {
        mockMvc.perform(get("/api/pvms/dashboard/stations-geo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summary.length()").value(5))
            .andExpect(jsonPath("$.data.stations.length()").value(6))
            .andExpect(jsonPath("$.data.stations[0].id").value("PV-001"))
            .andExpect(jsonPath("$.data.stations[0].status").value("normal"))
            .andExpect(jsonPath("$.data.stations[0].resourceUnitId").value("RU-001"))
            .andExpect(jsonPath("$.data.stations[0].resourceUnitName").value("华南园区虚拟电厂"))
            .andExpect(jsonPath("$.data.filters.regionOptions.length()").value(6));
    }

    @Test
    void shouldFilterStationsByStatus() throws Exception {
        mockMvc.perform(get("/api/pvms/dashboard/stations-geo").param("status", "warning"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.stations.length()").value(1))
            .andExpect(jsonPath("$.data.stations[0].status").value("warning"));
    }

    @Test
    void shouldReturnDashboardKpiSummary() throws Exception {
        mockMvc.perform(get("/api/pvms/dashboard/kpi-summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.focusLabel").value("全系统"))
            .andExpect(jsonPath("$.data.items.length()").value(8))
            .andExpect(jsonPath("$.data.items[0].key").value("capacity"));
    }

    @Test
    void shouldReturnPowerCurveForStationScope() throws Exception {
        mockMvc.perform(
                get("/api/pvms/dashboard/power-curve")
                    .param("date", "2026-03-22")
                    .param("stationId", "PV-001")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentDate").value("2026-03-22"))
            .andExpect(jsonPath("$.data.stationName").value("深圳港科园区光伏电站"))
            .andExpect(jsonPath("$.data.actual.length()").value(96))
            .andExpect(jsonPath("$.data.plan.length()").value(96));
    }

    @Test
    void shouldReturnStationRankingByMetric() throws Exception {
        mockMvc.perform(get("/api/pvms/dashboard/station-ranking").param("metric", "pr"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentMetric").value("pr"))
            .andExpect(jsonPath("$.data.rankings[0].value").isNumber())
            .andExpect(jsonPath("$.data.metricOptions.length()").value(3));
    }

    @Test
    void shouldReturnRecentAlarmFeed() throws Exception {
        mockMvc.perform(get("/api/pvms/alarms/recent").param("level", "critical"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.summary.critical").value(1))
            .andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.items[0].level").value("critical"));
    }

    @Test
    void shouldReturnCurrentWeatherForStation() throws Exception {
        mockMvc.perform(get("/api/pvms/weather/current").param("stationId", "PV-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.stationName").value("深圳港科园区光伏电站"))
            .andExpect(jsonPath("$.data.current.temperature").value(28))
            .andExpect(jsonPath("$.data.forecast.length()").value(3));
    }

    @Test
    void shouldReturnDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/pvms/dashboard/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summaryCards").isArray())
            .andExpect(jsonPath("$.data.summaryCards.length()").value(5))
            .andExpect(jsonPath("$.data.trends.dates").isArray())
            .andExpect(jsonPath("$.data.focusAlarms").isArray())
            .andExpect(jsonPath("$.data.stationRows").isArray())
            .andExpect(jsonPath("$.data.stationRows.length()").value(6));
    }

    @Test
    void shouldReturnVppNodeStatus() throws Exception {
        mockMvc.perform(get("/api/pvms/dashboard/vpp-node-status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.nodeId").value("VPP-NODE-001"))
            .andExpect(jsonPath("$.data.totalCapacityMw").isNumber())
            .andExpect(jsonPath("$.data.availableCapacityMw").isNumber())
            .andExpect(jsonPath("$.data.onlineStations").isNumber())
            .andExpect(jsonPath("$.data.totalStations").value(6))
            .andExpect(jsonPath("$.data.adjustableRangeMw.min").isNumber());
    }
}
