package cn.techstar.pvms.backend.module.adjustable.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.adjustable.service.AdjustableCapacityMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pvms/adjustable-capacity")
public class AdjustableCapacityController {

    private final AdjustableCapacityMockService service;

    public AdjustableCapacityController(AdjustableCapacityMockService service) {
        this.service = service;
    }

    @GetMapping("/realtime")
    public ApiResponse<Map<String, Object>> getRealtime() {
        return ApiResponse.success(service.getRealtime());
    }
}
