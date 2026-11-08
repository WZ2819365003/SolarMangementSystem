package cn.techstar.pvms.backend.module.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单账号登录与内存令牌。后端为单实例部署，重启后令牌失效，用户重新登录即可。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, FailedLogin> failedLogins = new ConcurrentHashMap<>();
    private final AuthProperties properties;
    private final Clock clock;
    private final byte[] usernameDigest;
    private final byte[] passwordDigest;

    @Autowired
    public AuthService(AuthProperties properties) {
        this(properties, Clock.systemUTC());
    }

    AuthService(AuthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.usernameDigest = digest(properties.username());
        this.passwordDigest = digest(resolvePassword(properties.password()));
    }

    public LoginResult login(String username, String password, String clientIp) {
        Instant now = clock.instant();
        FailedLogin failed = failedLogins.get(clientIp);
        if (failed != null && failed.locksOut(now, properties.maxFailedAttempts())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后再试");
        }
        // 用非短路的 & 保证用户名错误和密码错误的耗时一致
        if (!(MessageDigest.isEqual(usernameDigest, digest(username))
            & MessageDigest.isEqual(passwordDigest, digest(password)))) {
            recordFailure(clientIp, now);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        failedLogins.remove(clientIp);
        sessions.values().removeIf(session -> session.isExpired(now));
        String token = newToken();
        Instant expiresAt = now.plus(properties.tokenTtl());
        sessions.put(token, new Session(properties.username(), expiresAt));
        return new LoginResult(token, properties.username(), expiresAt);
    }

    public boolean isValidToken(String token) {
        Session session = sessions.get(token);
        if (session == null) {
            return false;
        }
        if (session.isExpired(clock.instant())) {
            sessions.remove(token);
            return false;
        }
        return true;
    }

    public void logout(String token) {
        sessions.remove(token);
    }

    private void recordFailure(String clientIp, Instant now) {
        failedLogins.values().removeIf(failed -> failed.isExpired(now));
        failedLogins.merge(
            clientIp,
            new FailedLogin(1, now.plus(properties.lockDuration())),
            (current, first) -> new FailedLogin(current.count() + 1, first.expiresAt())
        );
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String resolvePassword(String configured) {
        if (configured == null || configured.isBlank()) {
            String generated = newToken();
            log.warn("未配置 PVMS_AUTH_PASSWORD，本次启动使用随机口令（账号 {}）：{}", properties.username(), generated);
            return generated;
        }
        if (configured.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("PVMS_AUTH_PASSWORD 长度不能少于 " + MIN_PASSWORD_LENGTH + " 位");
        }
        return configured;
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record LoginResult(String token, String username, Instant expiresAt) {
    }

    private record Session(String username, Instant expiresAt) {
        boolean isExpired(Instant now) {
            return !now.isBefore(expiresAt);
        }
    }

    private record FailedLogin(int count, Instant expiresAt) {
        boolean isExpired(Instant now) {
            return !now.isBefore(expiresAt);
        }

        boolean locksOut(Instant now, int maxAttempts) {
            return !isExpired(now) && count >= maxAttempts;
        }
    }
}
