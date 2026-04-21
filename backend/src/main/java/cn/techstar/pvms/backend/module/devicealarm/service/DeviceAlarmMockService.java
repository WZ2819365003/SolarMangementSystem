package cn.techstar.pvms.backend.module.devicealarm.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DeviceAlarmMockService {

    private static final List<DeviceRecord> DEVICES = buildDevices();
    private static final List<AlarmRecord> ALARMS = buildAlarms();

    public Map<String, Object> getDeviceMonitor(String keyword, String status) {
        List<DeviceRecord> filtered = DEVICES.stream()
            .filter(d -> keyword == null || keyword.isBlank() || d.name().contains(keyword) || d.stationName().contains(keyword))
            .filter(d -> status == null || status.isBlank() || Objects.equals(d.status(), status))
            .toList();

        long total = filtered.size();
        long normal = filtered.stream().filter(d -> "normal".equals(d.status())).count();
        long warning = filtered.stream().filter(d -> "warning".equals(d.status())).count();
        long fault = filtered.stream().filter(d -> "fault".equals(d.status())).count();
        long offline = filtered.stream().filter(d -> "offline".equals(d.status())).count();

        // Group by station
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (DeviceRecord d : filtered) {
            grouped.computeIfAbsent(d.stationName(), k -> new ArrayList<>()).add(orderedMap(
                "id", d.id(), "name", d.name(), "type", d.type(),
                "status", d.status(), "powerKw", d.powerKw()
            ));
        }
        List<Map<String, Object>> groups = grouped.entrySet().stream()
            .map(e -> orderedMap("stationName", e.getKey(), "devices", e.getValue()))
            .toList();

        List<Map<String, Object>> tips = DEVICES.stream()
            .filter(d -> "warning".equals(d.status()))
            .map(d -> orderedMap("deviceId", d.id(), "message", "距下次保养还剩 12 天"))
            .toList();

        return orderedMap(
            "summary", orderedMap("total", total, "normal", normal, "warning", warning, "fault", fault, "offline", offline),
            "groups", groups,
            "maintenanceTips", tips
        );
    }

    public Map<String, Object> getAlarmCenter(String level, String status, String keyword) {
        List<AlarmRecord> filtered = ALARMS.stream()
            .filter(a -> level == null || level.isBlank() || Objects.equals(a.level(), level))
            .filter(a -> status == null || status.isBlank() || Objects.equals(a.status(), status))
            .filter(a -> keyword == null || keyword.isBlank() || a.description().contains(keyword) || a.stationName().contains(keyword))
            .toList();

        long pending = ALARMS.stream().filter(a -> "pending".equals(a.status())).count();
        long processing = ALARMS.stream().filter(a -> "processing".equals(a.status())).count();
        long resolved = ALARMS.stream().filter(a -> "resolved".equals(a.status())).count();
        long closed = ALARMS.stream().filter(a -> "closed".equals(a.status())).count();

        return orderedMap(
            "processBoard", orderedMap("pending", pending, "processing", processing, "resolved", resolved, "closed", closed),
            "summary", orderedMap(
                "critical", ALARMS.stream().filter(a -> "critical".equals(a.level())).count(),
                "major", ALARMS.stream().filter(a -> "major".equals(a.level())).count(),
                "minor", ALARMS.stream().filter(a -> "minor".equals(a.level())).count(),
                "hint", ALARMS.stream().filter(a -> "hint".equals(a.level())).count()
            ),
            "items", filtered.stream().map(a -> orderedMap(
                "id", a.id(), "level", a.level(), "levelLabel", a.levelLabel(),
                "deviceName", a.deviceName(), "stationName", a.stationName(),
                "description", a.description(), "status", a.status(),
                "createdAt", a.createdAt()
            )).toList()
        );
    }

    private static List<DeviceRecord> buildDevices() {
        String[][] stations = {
            {"ST-001", "深圳港科园区光伏电站", "8"},
            {"ST-002", "松山湖智造园光伏电站", "16"},
            {"ST-007", "武汉物流基地光伏电站", "14"},
            {"ST-010", "合肥研发中心光伏电站", "6"},
            {"ST-014", "成都西部基地光伏电站", "16"}
        };
        String[] statusPool = {"normal", "normal", "normal", "normal", "warning", "normal", "normal", "fault", "normal", "normal", "normal", "offline"};
        List<DeviceRecord> result = new ArrayList<>();
        for (String[] station : stations) {
            int count = Integer.parseInt(station[2]);
            for (int i = 1; i <= count; i++) {
                String status = statusPool[(i - 1) % statusPool.length];
                double power = status.equals("fault") || status.equals("offline") ? 0 : 180 + Math.random() * 140;
                result.add(new DeviceRecord(
                    station[0].replace("ST-", "INV-") + String.format("-%02d", i),
                    i + "#逆变器", "inverter",
                    station[0], station[1],
                    status, Math.round(power)
                ));
            }
        }
        return result;
    }

    private static List<AlarmRecord> buildAlarms() {
        return List.of(
            new AlarmRecord("ALM-001", "critical", "紧急", "逆变器 INV-03", "武汉物流基地光伏电站", "直流侧过压告警，需立即复核组串电压与断路器状态。", "pending", "2026-03-29 14:32"),
            new AlarmRecord("ALM-002", "major", "重要", "箱变 T1", "松山湖智造园光伏电站", "箱变高压侧过温预警，温升超过基线 8.6°C。", "processing", "2026-03-29 14:18"),
            new AlarmRecord("ALM-003", "minor", "一般", "汇流箱 HB-07", "深圳港科园区光伏电站", "支路电流偏低，疑似单支路积灰或遮挡。", "pending", "2026-03-29 13:57"),
            new AlarmRecord("ALM-004", "hint", "提示", "气象站 WS-02", "成都西部基地光伏电站", "天气采样上报延迟 90 秒，已自动补采。", "resolved", "2026-03-29 13:40"),
            new AlarmRecord("ALM-005", "major", "重要", "采集器 DTU-11", "天津港储能园光伏电站", "采集器离线超过 15 分钟，关联设备数据中断。", "pending", "2026-03-29 13:12"),
            new AlarmRecord("ALM-006", "minor", "一般", "逆变器 INV-08", "深圳港科园区光伏电站", "MPPT 跟踪异常，输出功率波动超过 15%。", "processing", "2026-03-29 12:45"),
            new AlarmRecord("ALM-007", "hint", "提示", "环境监测仪", "合肥研发中心光伏电站", "湿度传感器数据漂移，建议校准。", "closed", "2026-03-29 11:20"),
            new AlarmRecord("ALM-008", "major", "重要", "逆变器 INV-12", "松山湖智造园光伏电站", "绝缘阻抗下降，疑似线缆老化。", "resolved", "2026-03-28 16:30"),
            new AlarmRecord("ALM-009", "critical", "紧急", "防雷器 SPD-01", "武汉物流基地光伏电站", "防雷器动作计数器满，需更换。", "resolved", "2026-03-28 10:15"),
            new AlarmRecord("ALM-010", "minor", "一般", "逆变器 INV-05", "成都西部基地光伏电站", "风扇转速异常，散热效率降低。", "closed", "2026-03-27 09:30")
        );
    }

    private static Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((String) entries[i], entries[i + 1]);
        }
        return result;
    }

    private record DeviceRecord(String id, String name, String type, String stationId, String stationName, String status, double powerKw) {}
    private record AlarmRecord(String id, String level, String levelLabel, String deviceName, String stationName, String description, String status, String createdAt) {}
}
