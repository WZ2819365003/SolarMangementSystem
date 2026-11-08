package cn.techstar.pvms.backend.module.auth;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 业务接口测试不关心鉴权，这里让自动配置的 MockMvc 默认带上有效令牌。
 * 鉴权行为本身由 AuthFilterTest 用未带令牌的 MockMvc 覆盖。
 */
@Configuration
@Profile("test")
class AuthenticatedMockMvcConfig {

    @Bean
    MockMvcBuilderCustomizer authenticatedMockMvc(AuthService authService, AuthProperties properties) {
        return builder -> builder.defaultRequest(get("/").header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + authService.login(properties.username(), properties.password(), "test-config").token()
        ));
    }
}
