package cn.techstar.pvms.backend.module.system.dto;

public record HealthStatusResponse(String service, String status, String version) {
}

