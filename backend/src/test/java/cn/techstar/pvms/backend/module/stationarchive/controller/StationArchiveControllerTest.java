package cn.techstar.pvms.backend.module.stationarchive.controller;

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
class StationArchiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnArchiveListBackedByStationFacts() throws Exception {
        mockMvc.perform(get("/api/pvms/stations/archive"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items.length()").value(16))
            .andExpect(jsonPath("$.data.items[0].id").value("SZ-001"))
            .andExpect(jsonPath("$.data.items[0].companyName").value("深圳市悦美颐华物业管理有限公司"))
            .andExpect(jsonPath("$.data.items[0].status").value("normal"));
    }

    @Test
    void shouldFilterArchiveListByKeyword() throws Exception {
        mockMvc.perform(get("/api/pvms/stations/archive").param("keyword", "武汉"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    @Test
    void shouldReturnStationTreeForProductionMonitorStationMode() throws Exception {
        mockMvc.perform(get("/api/pvms/station-tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.tree").isArray())
            .andExpect(jsonPath("$.data.tree.length()").value(5))
            .andExpect(jsonPath("$.data.tree[0].nodeType").value("company"))
            .andExpect(jsonPath("$.data.tree[0].label").value("深圳市悦美颐华物业管理有限公司"))
            .andExpect(jsonPath("$.data.tree[0].children[0].nodeType").value("station"))
            .andExpect(jsonPath("$.data.tree[0].children[0].extra.status").exists())
            .andExpect(jsonPath("$.data.tree[0].children[0].children[0].nodeType").value("inverter"));
    }

    @Test
    void shouldFilterStationTreeByStatus() throws Exception {
        mockMvc.perform(get("/api/pvms/station-tree").param("status", "offline"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tree.length()").value(1))
            .andExpect(jsonPath("$.data.tree[0].label").value("天津港储能科技有限公司"))
            .andExpect(jsonPath("$.data.tree[0].children.length()").value(2))
            .andExpect(jsonPath("$.data.tree[0].children[0].extra.status").value("offline"));
    }

    @Test
    void shouldReturnCompanyOverviewForRightPanel() throws Exception {
        mockMvc.perform(get("/api/pvms/station-archive/company-overview").param("companyId", "com-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("深圳市悦美颐华物业管理有限公司"))
            .andExpect(jsonPath("$.data.kpis").isArray())
            .andExpect(jsonPath("$.data.kpis.length()").value(9))
            .andExpect(jsonPath("$.data.stations").isArray())
            .andExpect(jsonPath("$.data.stations[0].id").value("SZ-001"))
            .andExpect(jsonPath("$.data.stations[0].adjustableKw").isNumber());
    }

    @Test
    void shouldReturnResourceOverviewForStandaloneView() throws Exception {
        mockMvc.perform(get("/api/pvms/station-archive/resource-overview").param("stationId", "SZ-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("深圳湾科创园 A 站"))
            .andExpect(jsonPath("$.data.kpis").isArray())
            .andExpect(jsonPath("$.data.powerCurve.times.length()").value(96))
            .andExpect(jsonPath("$.data.stations[0].name").value("深圳湾科创园 A 站"));
    }

    @Test
    void shouldReturnAdjustableMetricPayloadForStationRightPanel() throws Exception {
        mockMvc.perform(
                get("/api/pvms/station-archive/station-realtime")
                    .param("stationId", "SZ-001")
                    .param("metric", "adjustable")
                    .param("date", "2026-03-30")
                    .param("granularity", "15min")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.times.length()").value(96))
            .andExpect(jsonPath("$.data.series[0].name").value("负荷"))
            .andExpect(jsonPath("$.data.stationKpis.adjustableCapacityKw").isNumber())
            .andExpect(jsonPath("$.data.monthlyStats.responseCount").isNumber())
            .andExpect(jsonPath("$.data.fields.maxUpCapacity").isNumber());
    }

    @Test
    void shouldSupportOneMinuteGranularityForStationRealtime() throws Exception {
        mockMvc.perform(
                get("/api/pvms/station-archive/station-realtime")
                    .param("stationId", "SZ-001")
                    .param("metric", "forecast")
                    .param("date", "2026-03-30")
                    .param("granularity", "1min")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.times.length()").value(1440))
            .andExpect(jsonPath("$.data.series[0].data.length()").value(1440));
    }

    @Test
    void shouldReturnLegacyAdjustableSummaryEndpoint() throws Exception {
        mockMvc.perform(get("/api/pvms/station-archive/station-adjustable").param("stationId", "SZ-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.stationId").value("SZ-001"))
            .andExpect(jsonPath("$.data.currentPowerKw").isNumber())
            .andExpect(jsonPath("$.data.maxAdjustableKw").isNumber())
            .andExpect(jsonPath("$.data.periods").isArray());
    }

    @Test
    void shouldReturnStationStrategyForReadonlyPanel() throws Exception {
        mockMvc.perform(get("/api/pvms/station-archive/station-strategy").param("stationId", "SZ-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentStrategy.name").value("华南园区调峰策略"))
            .andExpect(jsonPath("$.data.currentStrategy.targetPowerKw").isNumber())
            .andExpect(jsonPath("$.data.executionLogs").isArray())
            .andExpect(jsonPath("$.data.executionLogs[0].action").exists());
    }

    @Test
    void shouldReturnInverterRealtimeWithInverterIdOnly() throws Exception {
        mockMvc.perform(get("/api/pvms/station-archive/inverter-realtime").param("inverterId", "SZ-001-INV-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.inverterId").value("SZ-001-INV-01"))
            .andExpect(jsonPath("$.data.stationId").value("SZ-001"))
            .andExpect(jsonPath("$.data.topology.strings").isArray())
            .andExpect(jsonPath("$.data.deviceInfo.model").exists())
            .andExpect(jsonPath("$.data.powerCurve.times.length()").value(96));
    }
}
