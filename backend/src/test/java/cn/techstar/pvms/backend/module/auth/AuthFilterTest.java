package cn.techstar.pvms.backend.module.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AuthFilterTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private AuthFilter authFilter;
    @Autowired
    private AuthProperties properties;
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc anonymous;

    @BeforeEach
    void setUp() {
        anonymous = MockMvcBuilders.webAppContextSetup(context).addFilters(authFilter).build();
    }

    @Test
    void shouldRejectAnonymousReadsAndWrites() throws Exception {
        for (String path : new String[] {
            "/api/pvms/station-tree",
            "/api/pvms/strategy/list",
            "/api/pvms/devices/monitor",
            "/api/pvms/adjustable-capacity/realtime",
            "/api/pvms/dashboard/stations-geo"
        }) {
            anonymous.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.code").value(401));
        }
        for (String path : new String[] {
            "/api/pvms/strategy/create",
            "/api/pvms/strategy/batch-create",
            "/api/pvms/strategy/submit",
            "/api/pvms/strategy/terminate",
            "/api/pvms/strategy/batch-submit",
            "/api/pvms/strategy/batch-delete",
            "/api/pvms/strategy/simulate",
            "/api/pvms/strategy/batch-simulate"
        }) {
            anonymous.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void shouldKeepOnlyExactPublicPathsOpen() throws Exception {
        anonymous.perform(get("/api/system/health")).andExpect(status().isOk());

        anonymous.perform(get("/api/system/health/")).andExpect(status().isUnauthorized());
        anonymous.perform(get("/api/system/health;x=1")).andExpect(status().isUnauthorized());
        anonymous.perform(get("/api/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidTokens() throws Exception {
        for (String value : new String[] {"Bearer not-a-token", "Bearer ", "Basic abc"}) {
            anonymous.perform(get("/api/pvms/station-tree").header(HttpHeaders.AUTHORIZATION, value))
                .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void shouldLoginAccessAndLogout() throws Exception {
        String token = login(properties.username(), properties.password());
        String bearer = "Bearer " + token;

        anonymous.perform(get("/api/pvms/station-tree").header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        anonymous.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk());

        anonymous.perform(get("/api/pvms/station-tree").header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectWrongCredentialsAndMalformedLogin() throws Exception {
        anonymous.perform(post("/api/auth/login")
                .with(request -> {
                    request.setRemoteAddr("203.0.113.10");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(properties.username(), "wrong-password")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.data").doesNotExist());

        anonymous.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    private String login(String username, String password) throws Exception {
        String body = anonymous.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(username, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value(username))
            .andExpect(jsonPath("$.data.expiresAt").isString())
            .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.at("/data/token").asText();
    }

    private String loginBody(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(new AuthController.LoginRequest(username, password));
    }
}
