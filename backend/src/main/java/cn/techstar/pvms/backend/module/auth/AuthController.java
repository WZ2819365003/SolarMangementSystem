package cn.techstar.pvms.backend.module.auth;

import cn.techstar.pvms.backend.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.LoginResult> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return ApiResponse.success(authService.login(request.username(), request.password(), http.getRemoteAddr()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest http) {
        authService.logout(AuthFilter.bearerToken(http));
        return ApiResponse.success(null);
    }

    public record LoginRequest(
        @NotBlank @Size(max = 128) String username,
        @NotBlank @Size(max = 256) String password
    ) {
    }
}
