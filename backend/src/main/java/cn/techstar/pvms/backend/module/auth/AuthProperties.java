package cn.techstar.pvms.backend.module.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * 登录账号与令牌配置，部署时通过 PVMS_AUTH_USERNAME / PVMS_AUTH_PASSWORD 等环境变量注入。
 */
@ConfigurationProperties("pvms.auth")
public record AuthProperties(
    @DefaultValue("admin") String username,
    String password,
    @DefaultValue("8h") Duration tokenTtl,
    @DefaultValue("5") int maxFailedAttempts,
    @DefaultValue("15m") Duration lockDuration
) {
}
