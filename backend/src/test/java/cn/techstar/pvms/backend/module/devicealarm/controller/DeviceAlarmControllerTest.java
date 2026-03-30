package cn.techstar.pvms.backend.module.devicealarm.controller;

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
class DeviceAlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDeviceMonitorContract() throws Exception {
        mockMvc.perform(get("/api/pvms/devices/monitor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summaryCards.length()").value(4))
            .andExpect(jsonPath("$.data.summaryCards[0].label").isString())
            .andExpect(jsonPath("$.data.deviceGroups.length()").isNumber())
            .andExpect(jsonPath("$.data.maintenanceTips").isArray())
            .andExpect(jsonPath("$.data.rows.length()").isNumber())
            .andExpect(jsonPath("$.data.rows[0].deviceId").isString())
            .andExpect(jsonPath("$.data.rows[0].deviceName").isString())
            .andExpect(jsonPath("$.data.rows[0].stationName").isString())
            .andExpect(jsonPath("$.data.rows[0].status").isString())
            .andExpect(jsonPath("$.data.rows[0].lastReportAt").isString());
    }

    @Test
    void shouldFilterDeviceMonitorByChineseStatus() throws Exception {
        mockMvc.perform(get("/api/pvms/devices/monitor").param("status", "告警"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.rows.length()").isNumber())
            .andExpect(jsonPath("$.data.rows[0].status").value("告警"));
    }

    @Test
    void shouldReturnAlarmCenterContract() throws Exception {
        mockMvc.perform(get("/api/pvms/alarms/center"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.summaryCards.length()").value(4))
            .andExpect(jsonPath("$.data.processBoard.length()").value(3))
            .andExpect(jsonPath("$.data.rows.length()").isNumber())
            .andExpect(jsonPath("$.data.rows[0].alarmId").isString())
            .andExpect(jsonPath("$.data.rows[0].stationName").isString())
            .andExpect(jsonPath("$.data.rows[0].deviceName").isString())
            .andExpect(jsonPath("$.data.rows[0].category").isString())
            .andExpect(jsonPath("$.data.rows[0].level").isString())
            .andExpect(jsonPath("$.data.rows[0].status").isString())
            .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void shouldFilterAlarmCenterByChineseLevelAndStatus() throws Exception {
        mockMvc.perform(get("/api/pvms/alarms/center")
                .param("level", "严重")
                .param("status", "未处理"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.rows.length()").isNumber())
            .andExpect(jsonPath("$.data.rows[0].level").value("严重"))
            .andExpect(jsonPath("$.data.rows[0].status").value("未处理"));
    }
}
