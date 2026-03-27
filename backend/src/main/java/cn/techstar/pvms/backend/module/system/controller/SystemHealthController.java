package cn.techstar.pvms.backend.module.system.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.system.dto.HealthStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    @GetMapping("/health")
    public ApiResponse<HealthStatusResponse> health() {
        return ApiResponse.success(new HealthStatusResponse("pvms-backend", "UP", "0.0.1-SNAPSHOT"));
    }
}

