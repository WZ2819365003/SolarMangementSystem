package cn.techstar.pvms.backend.module.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private static final String PASSWORD = "unit-test-password";
    private static final String IP = "203.0.113.20";

    private final MutableClock clock = new MutableClock();

    private AuthService service(String password) {
        return new AuthService(
            new AuthProperties("admin", password, Duration.ofHours(8), 3, Duration.ofMinutes(15)),
            clock
        );
    }

    private static void assertStatus(Runnable action, HttpStatus status) {
        assertThatThrownBy(action::run)
            .isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode().value()).isEqualTo(status.value()));
    }

    @Test
    void shouldExpireTokensAfterTtl() {
        AuthService service = service(PASSWORD);
        String token = service.login("admin", PASSWORD, IP).token();
        assertThat(service.isValidToken(token)).isTrue();

        clock.advance(Duration.ofHours(8));

        assertThat(service.isValidToken(token)).isFalse();
    }

    @Test
    void shouldIssueUniqueTokens() {
        AuthService service = service(PASSWORD);
        assertThat(service.login("admin", PASSWORD, IP).token())
            .isNotEqualTo(service.login("admin", PASSWORD, IP).token());
    }

    @Test
    void shouldLockOutClientAfterRepeatedFailuresEvenWithCorrectPassword() {
        AuthService service = service(PASSWORD);
        for (int i = 0; i < 3; i++) {
            assertStatus(() -> service.login("admin", "wrong-password", IP), HttpStatus.UNAUTHORIZED);
        }

        assertStatus(() -> service.login("admin", PASSWORD, IP), HttpStatus.TOO_MANY_REQUESTS);
        assertThat(service.login("admin", PASSWORD, "203.0.113.21").token()).isNotBlank();

        clock.advance(Duration.ofMinutes(15));
        assertThat(service.login("admin", PASSWORD, IP).token()).isNotBlank();
    }

    @Test
    void shouldClearFailuresAfterSuccessfulLogin() {
        AuthService service = service(PASSWORD);
        for (int round = 0; round < 3; round++) {
            assertStatus(() -> service.login("admin", "wrong-password", IP), HttpStatus.UNAUTHORIZED);
            assertStatus(() -> service.login("admin", "wrong-password", IP), HttpStatus.UNAUTHORIZED);
            assertThat(service.login("admin", PASSWORD, IP).token()).isNotBlank();
        }
    }

    @Test
    void shouldRejectWrongUsername() {
        assertStatus(() -> service(PASSWORD).login("root", PASSWORD, IP), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRefuseToStartWithShortPassword() {
        assertThatThrownBy(() -> service("short")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNeverAcceptBlankPasswordWhenUnconfigured() {
        AuthService service = service(null);
        assertStatus(() -> service.login("admin", "", IP), HttpStatus.UNAUTHORIZED);
        assertStatus(() -> service.login("admin", "null", IP), HttpStatus.UNAUTHORIZED);
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-08T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
