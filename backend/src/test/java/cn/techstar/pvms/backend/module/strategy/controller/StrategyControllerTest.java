package cn.techstar.pvms.backend.module.strategy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
class StrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnStrategyMeta() throws Exception {
        mockMvc.perform(get("/api/pvms/strategy/meta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.types.length()").value(4))
            .andExpect(jsonPath("$.data.statuses.length()").value(5))
            .andExpect(jsonPath("$.data.stations.length()").value(16))
            .andExpect(jsonPath("$.data.companies.length()").value(5))
            .andExpect(jsonPath("$.data.pricePeriods.length()").value(9));
    }

    @Test
    void shouldReturnStrategyTreeAndKpi() throws Exception {
        mockMvc.perform(get("/api/pvms/strategy/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.tree.length()").value(5))
            .andExpect(jsonPath("$.data.tree[0].children.length()").isNumber());

        mockMvc.perform(get("/api/pvms/strategy/kpi"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.activeCount").isNumber())
            .andExpect(jsonPath("$.data.todayRevenue").isNumber())
            .andExpect(jsonPath("$.data.successRate").isNumber())
            .andExpect(jsonPath("$.data.pendingCount").isNumber());
    }

    @Test
    void shouldReturnStrategyListAndDetail() throws Exception {
        mockMvc.perform(get("/api/pvms/strategy/list").param("status", "executing"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.items.length()").isNumber())
            .andExpect(jsonPath("$.data.items[0].id").isString())
            .andExpect(jsonPath("$.data.items[0].stationId").isString());

        mockMvc.perform(get("/api/pvms/strategy/detail").param("id", "SG-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value("SG-001"))
            .andExpect(jsonPath("$.data.periods.length()").isNumber())
            .andExpect(jsonPath("$.data.pricePeriods.length()").value(9));
    }

    @Test
    void shouldReturnElectricityPriceRevenueAndCompare() throws Exception {
        mockMvc.perform(get("/api/pvms/strategy/electricity-price").param("stationId", "SZ-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.stationId").value("SZ-001"))
            .andExpect(jsonPath("$.data.periods.length()").value(9));

        mockMvc.perform(get("/api/pvms/strategy/revenue/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.kpi.todayRevenue").isNumber())
            .andExpect(jsonPath("$.data.trend.dates.length()").isNumber())
            .andExpect(jsonPath("$.data.typeBreakdown.length()").value(4));

        mockMvc.perform(get("/api/pvms/strategy/revenue/detail"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.items.length()").isNumber())
            .andExpect(jsonPath("$.data.items[0].strategyId").isString());

        mockMvc.perform(get("/api/pvms/strategy/compare").param("ids", "SG-001,SG-002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.items.length()").value(2))
            .andExpect(jsonPath("$.data.totalDelta").isNumber());
    }

    @Test
    void shouldCreateAndBatchCreateStrategies() throws Exception {
        mockMvc.perform(post("/api/pvms/strategy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stationId": "SZ-001",
                      "type": "peak-shaving",
                      "name": "深圳湾削峰策略",
                      "targetPowerKw": 1200,
                      "startTime": "2026-03-30T08:00:00",
                      "endTime": "2026-03-30T18:00:00",
                      "remark": "test create"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").isString())
            .andExpect(jsonPath("$.data.status").value("draft"));

        mockMvc.perform(post("/api/pvms/strategy/batch-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "strategies": [
                        {
                          "stationId": "SZ-002",
                          "type": "peak-shaving",
                          "name": "南山削峰策略",
                          "targetPowerKw": 900,
                          "startTime": "2026-03-30T07:00:00",
                          "endTime": "2026-03-30T19:00:00"
                        },
                        {
                          "stationId": "DG-001",
                          "type": "demand-response",
                          "name": "松山湖需求响应策略",
                          "targetPowerKw": 1500,
                          "startTime": "2026-03-30T09:00:00",
                          "endTime": "2026-03-30T17:00:00"
                        }
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.createdCount").value(2))
            .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    @Test
    void shouldSubmitTerminateAndBatchOperateStrategies() throws Exception {
        mockMvc.perform(post("/api/pvms/strategy/submit").param("id", "SG-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value("SG-001"))
            .andExpect(jsonPath("$.data.status").value("pending"));

        mockMvc.perform(post("/api/pvms/strategy/terminate").param("id", "SG-002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value("SG-002"))
            .andExpect(jsonPath("$.data.status").value("terminated"));

        mockMvc.perform(post("/api/pvms/strategy/batch-submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "ids": ["SG-003", "SG-004"] }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.updatedCount").value(2));

        mockMvc.perform(post("/api/pvms/strategy/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "ids": ["SG-005", "SG-006"] }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.deletedCount").value(2));
    }

    @Test
    void shouldSimulateAndBatchSimulateStrategies() throws Exception {
        mockMvc.perform(post("/api/pvms/strategy/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stationId": "SZ-001",
                      "type": "peak-shaving",
                      "targetPowerKw": 1200,
                      "startTime": "2026-03-30T08:00:00",
                      "endTime": "2026-03-30T18:00:00"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.estimatedRevenue").isNumber())
            .andExpect(jsonPath("$.data.confidenceRange.low").isNumber())
            .andExpect(jsonPath("$.data.confidenceRange.high").isNumber())
            .andExpect(jsonPath("$.data.breakdown.peakSaving").isNumber());

        mockMvc.perform(post("/api/pvms/strategy/batch-simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "strategies": [
                        {
                          "stationId": "SZ-001",
                          "type": "peak-shaving",
                          "targetPowerKw": 1200,
                          "startTime": "2026-03-30T08:00:00",
                          "endTime": "2026-03-30T18:00:00"
                        },
                        {
                          "stationId": "DG-001",
                          "type": "demand-response",
                          "targetPowerKw": 1500,
                          "startTime": "2026-03-30T09:00:00",
                          "endTime": "2026-03-30T17:00:00"
                        }
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.results.length()").value(2))
            .andExpect(jsonPath("$.data.totalRevenue").isNumber());
    }
}
