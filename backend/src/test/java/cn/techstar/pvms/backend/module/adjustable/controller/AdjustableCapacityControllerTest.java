package cn.techstar.pvms.backend.module.adjustable.controller;

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
class AdjustableCapacityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnRealtimeAdjustableCapacity() throws Exception {
        mockMvc.perform(get("/api/pvms/adjustable-capacity/realtime"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summary.totalCapacityKw").isNumber())
            .andExpect(jsonPath("$.data.summary.currentPowerKw").isNumber())
            .andExpect(jsonPath("$.data.summary.maxAdjustableUpKw").isNumber())
            .andExpect(jsonPath("$.data.summary.maxAdjustableDownKw").isNumber())
            .andExpect(jsonPath("$.data.summary.deferrableLoadKw").isNumber())
            .andExpect(jsonPath("$.data.summary.utilizationRate").isNumber())
            .andExpect(jsonPath("$.data.stations").isArray())
            .andExpect(jsonPath("$.data.stations[0].adjustableUpKw").isNumber())
            .andExpect(jsonPath("$.data.timeline").isArray())
            .andExpect(jsonPath("$.data.timeline.length()").value(96));
    }
}
