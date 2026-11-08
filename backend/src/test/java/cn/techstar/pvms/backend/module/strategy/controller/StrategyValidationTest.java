package cn.techstar.pvms.backend.module.strategy.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StrategyValidationTest {

    @Autowired
    private MockMvc mockMvc;

    private void json(String path, String body) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/pvms/strategy/create", "/api/pvms/strategy/simulate"})
    void shouldRejectInvalidStrategyPayloads(String path) throws Exception {
        json(path, "{}");
        json(path, "not-json");
        json(path, """
            {"stationId":"SZ-001","type":"unknown","targetPowerKw":100,
             "startTime":"2026-03-30T08:00:00","endTime":"2026-03-30T18:00:00"}""");
        json(path, """
            {"stationId":"SZ-001","type":"peak-shaving","targetPowerKw":-1,
             "startTime":"2026-03-30T08:00:00","endTime":"2026-03-30T18:00:00"}""");
        json(path, """
            {"stationId":"SZ-001","type":"peak-shaving","targetPowerKw":100,
             "startTime":"2026-03-30T18:00:00","endTime":"2026-03-30T08:00:00"}""");
        json(path, """
            {"stationId":"NOT-A-STATION","type":"peak-shaving","targetPowerKw":100,
             "startTime":"2026-03-30T08:00:00","endTime":"2026-03-30T18:00:00"}""");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/pvms/strategy/batch-create", "/api/pvms/strategy/batch-simulate"})
    void shouldRejectInvalidBatchPayloads(String path) throws Exception {
        json(path, "{}");
        json(path, "{\"strategies\":[]}");
        json(path, "{\"strategies\":[{}]}");
        json(path, "{\"strategies\":[null]}");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/pvms/strategy/batch-submit", "/api/pvms/strategy/batch-delete"})
    void shouldRejectInvalidIdLists(String path) throws Exception {
        json(path, "{}");
        json(path, "{\"ids\":[]}");
        json(path, "{\"ids\":[\"\"]}");
    }

    @Test
    void shouldRejectBlankIdParameter() throws Exception {
        mockMvc.perform(post("/api/pvms/strategy/submit").param("id", " "))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldNotFallBackToAnotherStrategyWhenIdIsUnknown() throws Exception {
        String before = mockMvc.perform(get("/api/pvms/strategy/list"))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/pvms/strategy/submit").param("id", "SG-NOT-EXIST"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
        mockMvc.perform(post("/api/pvms/strategy/terminate").param("id", "SG-NOT-EXIST"))
            .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/pvms/strategy/batch-submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[\"SG-NOT-EXIST\"]}"))
            .andExpect(status().isNotFound());

        org.assertj.core.api.Assertions.assertThat(
            mockMvc.perform(get("/api/pvms/strategy/list")).andReturn().getResponse().getContentAsString()
        ).isEqualTo(before);
    }
}
