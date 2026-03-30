package cn.techstar.pvms.backend.module.devicealarm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DeviceAlarmSupport {

    public static final LocalDate DEFAULT_BIZ_DATE = LocalDate.of(2026, 3, 30);
    public static final LocalDateTime DEFAULT_NOW = LocalDateTime.of(2026, 3, 30, 14, 30);
    public static final int CURRENT_SLOT = 58;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DeviceAlarmSupport() {
    }

    public static String deviceStatusLabel(String rawStatus) {
        return switch (rawStatus == null ? "" : rawStatus) {
            case "offline" -> "离线";
            case "warning", "fault", "maintenance" -> "告警";
            default -> "在线";
        };
    }

    public static String alarmLevelLabel(String rawLevel, String type) {
        if ("严重".equals(rawLevel)) {
            return "严重";
        }
        if ("故障".equals(rawLevel)) {
            return "重要";
        }
        if ("告警".equals(rawLevel) && type != null && (type.contains("通信") || type.contains("离线"))) {
            return "提示";
        }
        return "一般";
    }

    public static String alarmStatusLabel(LocalDateTime eventTime, String rawStatus) {
        if ("未处理".equals(rawStatus)) {
            return "未处理";
        }
        if ("待确认".equals(rawStatus)) {
            return "待确认";
        }
        if ("已确认".equals(rawStatus)) {
            if (eventTime != null && eventTime.isBefore(DEFAULT_NOW.minusHours(3))) {
                return "已关闭";
            }
            return "处理中";
        }
        return "处理中";
    }

    public static double computeLoadRate(double ratedPowerKw, Double stationPvOutputKw, int inverterCount, String rawStatus) {
        if (ratedPowerKw <= 0 || inverterCount <= 0 || stationPvOutputKw == null) {
            return 0;
        }
        double basePower = stationPvOutputKw / inverterCount;
        double factor = switch (rawStatus == null ? "" : rawStatus) {
            case "warning" -> 0.82;
            case "maintenance" -> 0.58;
            case "fault", "offline" -> 0.0;
            default -> 0.96;
        };
        return Math.max(0, Math.min(100, round(basePower * factor / ratedPowerKw * 100, 1)));
    }

    public static double computeDeviceTemperature(double moduleTemperatureC, String rawStatus, int sortIndex) {
        double adjustment = switch (rawStatus == null ? "" : rawStatus) {
            case "warning" -> 3.8;
            case "maintenance" -> 2.4;
            case "fault" -> 4.5;
            case "offline" -> -6.0;
            default -> 0.6;
        };
        return round(moduleTemperatureC + adjustment + (sortIndex % 3) * 0.4, 1);
    }

    public static LocalDateTime computeLastReportAt(String rawStatus, int sortIndex) {
        int lagMinutes = switch (rawStatus == null ? "" : rawStatus) {
            case "warning" -> 9 + sortIndex;
            case "maintenance" -> 16 + sortIndex;
            case "fault" -> 28 + sortIndex;
            case "offline" -> 65 + sortIndex * 2;
            default -> 2 + sortIndex;
        };
        return DEFAULT_NOW.minusMinutes(lagMinutes);
    }

    public static String resolveDeviceType(String model, double ratedPowerKw) {
        if (model != null && !model.isBlank()) {
            return model;
        }
        return round(ratedPowerKw, 0) + "kW逆变器";
    }

    public static String categoryLabel(String type) {
        if (type == null || type.isBlank()) {
            return "运行异常";
        }
        if (type.contains("通信") || type.contains("离线")) {
            return "通讯";
        }
        if (type.contains("温")) {
            return "温度";
        }
        if (type.contains("绝缘")) {
            return "绝缘";
        }
        if (type.contains("MPPT") || type.contains("组串")) {
            return "发电侧";
        }
        return "运行异常";
    }

    public static String ownerLabel(String region, String levelLabel) {
        String regionOwner = switch (region == null ? "" : region) {
            case "华南" -> "华南运维组";
            case "华中" -> "华中运维组";
            case "华东" -> "华东运维组";
            case "华北" -> "华北运维组";
            case "西南" -> "西南运维组";
            default -> "平台值班";
        };
        return "严重".equals(levelLabel) ? regionOwner + " / 值长" : regionOwner;
    }

    public static String format(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }

    public static double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    public static Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            map.put((String) entries[index], entries[index + 1]);
        }
        return map;
    }
}
