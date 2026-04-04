package cn.techstar.pvms.backend.module.forecast.controller;

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
class ForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnForecastMeta() throws Exception {
        mockMvc.perform(get("/api/pvms/forecast/meta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.regions.length()").value(5))
            .andExpect(jsonPath("$.data.stations.length()").value(16))
            .andExpect(jsonPath("$.data.stations[0].id").value("SZ-001"));
    }

    @Test
    void shouldReturnForecastOverview() throws Exception {
        mockMvc.perform(get("/api/pvms/forecast/overview").param("stationId", "SZ-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.kpis.dayAheadAccuracy").isNumber())
            .andExpect(jsonPath("$.data.kpis.ultraShortAccuracy").isNumber())
            .andExpect(jsonPath("$.data.stationTable.length()").value(1))
            .andExpect(jsonPath("$.data.stationTable[0].stationId").value("SZ-001"));
    }

    @Test
    void shouldReturnForecastComparison() throws Exception {
        mockMvc.perform(get("/api/pvms/forecast/comparison").param("stationId", "SZ-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.timeLabels.length()").value(96))
            .andExpect(jsonPath("$.data.series.dayAhead.length()").value(96))
            .andExpect(jsonPath("$.data.series.ultraShort.length()").value(96))
            .andExpect(jsonPath("$.data.series.actual.length()").value(96));
    }

    @Test
    void shouldReturnForecastDeviationHeatmap() throws Exception {
        mockMvc.perform(get("/api/pvms/forecast/deviation-heatmap"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.hours.length()").value(24))
            .andExpect(jsonPath("$.data.stations.length()").value(16))
            .andExpect(jsonPath("$.data.data.length()").value(16))
            .andExpect(jsonPath("$.data.data[0].length()").value(24));
    }

    @Test
    void shouldReturnForecastAdjustable() throws Exception {
        mockMvc.perform(get("/api/pvms/forecast/adjustable").param("region", "华南"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.kpis.totalAdjustable").isNumber())
            .andExpect(jsonPath("$.data.capacityCurve.timeLabels.length()").value(96))
            .andExpect(jsonPath("$.data.timeline.length()").isNumber())
            .andExpect(jsonPath("$.data.stationTable.length()").value(6));
    }

    @Test
    void shouldReturnForecastAccuracy() throws Exception {
        mockMvc.perform(get("/api/pvms/forecast/accuracy"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.kpis.mae").isNumber())
            .andExpect(jsonPath("$.data.kpis.rmse").isNumber())
            .andExpect(jsonPath("$.data.trend.dates.length()").value(30))
            .andExpect(jsonPath("$.data.distribution.bins.length()").value(10))
            .andExpect(jsonPath("$.data.stationRanking.length()").value(16))
            .andExpect(jsonPath("$.data.monthlyTable.length()").value(6));
    }
}
