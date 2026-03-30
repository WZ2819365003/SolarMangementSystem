package cn.techstar.pvms.backend.module.devicealarm.service;

import cn.techstar.pvms.backend.module.devicealarm.repository.DeviceAlarmRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DeviceAlarmDataService {

    private final DeviceAlarmRepository repository;

    public DeviceAlarmDataService(DeviceAlarmRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> getDeviceMonitor(String keyword, String status) {
        List<DeviceViewRow> allRows = repository.findDeviceFacts(DeviceAlarmSupport.DEFAULT_BIZ_DATE, DeviceAlarmSupport.CURRENT_SLOT)
            .stream()
            .map(this::toDeviceViewRow)
            .toList();

        List<DeviceViewRow> filteredRows = allRows.stream()
            .filter(keywordFilter(keyword))
            .filter(row -> status == null || status.isBlank() || Objects.equals(row.status(), status))
            .toList();

        List<Map<String, Object>> summaryCards = buildDeviceSummaryCards(filteredRows);
        List<Map<String, Object>> deviceGroups = buildDeviceGroups(filteredRows);
        List<String> maintenanceTips = buildMaintenanceTips(filteredRows.isEmpty() ? allRows : filteredRows);

        return DeviceAlarmSupport.orderedMap(
            "summaryCards", summaryCards,
            "deviceGroups", deviceGroups,
            "maintenanceTips", maintenanceTips,
            "rows", filteredRows.stream().map(this::mapDeviceRow).toList()
        );
    }

    public Map<String, Object> getAlarmCenter(String keyword, String level, String status) {
        List<AlarmViewRow> allRows = repository.findAlarmFacts().stream()
            .map(this::toAlarmViewRow)
            .sorted(Comparator.comparing(AlarmViewRow::happenedAtValue).reversed())
            .toList();

        List<AlarmViewRow> filteredRows = allRows.stream()
            .filter(alarmKeywordFilter(keyword))
            .filter(row -> level == null || level.isBlank() || Objects.equals(row.level(), level))
            .filter(row -> status == null || status.isBlank() || Objects.equals(row.status(), status))
            .toList();

        return DeviceAlarmSupport.orderedMap(
            "summaryCards", buildAlarmSummaryCards(allRows),
            "processBoard", buildProcessBoard(allRows),
            "rows", filteredRows.stream().map(this::mapAlarmRow).toList(),
            "total", filteredRows.size()
        );
    }

    private DeviceViewRow toDeviceViewRow(DeviceAlarmRepository.DeviceFactRow row) {
        String type = DeviceAlarmSupport.resolveDeviceType(row.model(), row.ratedPowerKw());
        String status = DeviceAlarmSupport.deviceStatusLabel(row.rawStatus());
        double loadRate = DeviceAlarmSupport.computeLoadRate(
            row.ratedPowerKw(),
            row.stationPvOutputKw(),
            row.inverterCount(),
            row.rawStatus()
        );
        double temperature = DeviceAlarmSupport.computeDeviceTemperature(row.moduleTemperatureC(), row.rawStatus(), row.sortIndex());
        LocalDateTime lastReportAt = DeviceAlarmSupport.computeLastReportAt(row.rawStatus(), row.sortIndex());
        return new DeviceViewRow(
            row.deviceId(),
            row.deviceName(),
            row.stationName(),
            type,
            status,
            loadRate,
            temperature,
            DeviceAlarmSupport.format(lastReportAt),
            row.strategyName(),
            row.strategyType()
        );
    }

    private AlarmViewRow toAlarmViewRow(DeviceAlarmRepository.AlarmFactRow row) {
        String level = DeviceAlarmSupport.alarmLevelLabel(row.rawLevel(), row.type());
        String status = DeviceAlarmSupport.alarmStatusLabel(row.eventTime(), row.rawStatus());
        return new AlarmViewRow(
            row.alarmId(),
            row.stationName(),
            row.deviceName(),
            DeviceAlarmSupport.categoryLabel(row.type()),
            level,
            status,
            DeviceAlarmSupport.ownerLabel(row.region(), level),
            DeviceAlarmSupport.format(row.eventTime()),
            row.eventTime(),
            row.description()
        );
    }

    private List<Map<String, Object>> buildDeviceSummaryCards(List<DeviceViewRow> rows) {
        long onlineCount = rows.stream().filter(row -> Objects.equals(row.status(), "在线")).count();
        long offlineCount = rows.stream().filter(row -> Objects.equals(row.status(), "离线")).count();
        long alarmCount = rows.stream().filter(row -> Objects.equals(row.status(), "告警")).count();
        return List.of(
            DeviceAlarmSupport.orderedMap("label", "设备总数", "value", rows.size(), "unit", "台"),
            DeviceAlarmSupport.orderedMap("label", "在线设备", "value", onlineCount, "unit", "台"),
            DeviceAlarmSupport.orderedMap("label", "离线设备", "value", offlineCount, "unit", "台"),
            DeviceAlarmSupport.orderedMap("label", "告警设备", "value", alarmCount, "unit", "台")
        );
    }

    private List<Map<String, Object>> buildDeviceGroups(List<DeviceViewRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<String, Long> counts = rows.stream()
            .collect(Collectors.groupingBy(DeviceViewRow::type, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(entry -> DeviceAlarmSupport.orderedMap(
                "label", entry.getKey(),
                "ratio", (int) Math.round(entry.getValue() * 100.0 / rows.size())
            ))
            .toList();
    }

    private List<String> buildMaintenanceTips(List<DeviceViewRow> rows) {
        List<String> tips = new ArrayList<>();
        rows.stream()
            .filter(row -> Objects.equals(row.status(), "告警"))
            .sorted(Comparator.comparing(DeviceViewRow::temperature).reversed())
            .limit(2)
            .forEach(row -> tips.add(row.stationName() + " / " + row.deviceName() + " 温度 " + row.temperature() + "℃，建议优先巡检。"));

        rows.stream()
            .filter(row -> row.loadRate() >= 85 && Objects.equals(row.status(), "在线"))
            .limit(1)
            .forEach(row -> tips.add(row.stationName() + " / " + row.deviceName() + " 负荷率 " + row.loadRate() + "%，建议关注晚高峰窗口。"));

        rows.stream()
            .filter(row -> row.strategyName() != null && !row.strategyName().isBlank())
            .filter(row -> Objects.equals(row.status(), "离线") || Objects.equals(row.status(), "告警"))
            .limit(1)
            .forEach(row -> tips.add(row.stationName() + " 当前策略“" + row.strategyName() + "”受设备状态影响，建议核对执行窗口。"));

        if (tips.isEmpty()) {
            tips.add("当前设备运行平稳，建议继续按日常巡检节奏检查通信与温度趋势。");
        }
        return tips.stream().distinct().limit(4).toList();
    }

    private List<Map<String, Object>> buildAlarmSummaryCards(List<AlarmViewRow> rows) {
        long pendingCount = rows.stream().filter(row -> Objects.equals(row.status(), "未处理") || Objects.equals(row.status(), "待确认")).count();
        long processingCount = rows.stream().filter(row -> Objects.equals(row.status(), "处理中")).count();
        long closedCount = rows.stream().filter(row -> Objects.equals(row.status(), "已关闭")).count();
        double closeLoopRate = rows.isEmpty() ? 100 : DeviceAlarmSupport.round(closedCount * 100.0 / rows.size(), 1);
        return List.of(
            DeviceAlarmSupport.orderedMap("label", "今日新增", "value", rows.size(), "unit", "条"),
            DeviceAlarmSupport.orderedMap("label", "待处理", "value", pendingCount, "unit", "条"),
            DeviceAlarmSupport.orderedMap("label", "处理中", "value", processingCount, "unit", "条"),
            DeviceAlarmSupport.orderedMap("label", "闭环率", "value", closeLoopRate, "unit", "%")
        );
    }

    private List<String> buildProcessBoard(List<AlarmViewRow> rows) {
        long severeCount = rows.stream().filter(row -> Objects.equals(row.level(), "严重")).count();
        long communicationCount = rows.stream().filter(row -> Objects.equals(row.category(), "通讯")).count();
        long pendingCount = rows.stream().filter(row -> Objects.equals(row.status(), "未处理") || Objects.equals(row.status(), "待确认")).count();
        return List.of(
            severeCount > 0
                ? "严重告警当前有 " + severeCount + " 条，建议 15 分钟内派单并确认现场责任人。"
                : "当前无严重告警，值班侧重点转向一般告警收敛。",
            communicationCount > 0
                ? "通讯类告警有 " + communicationCount + " 条，优先核查采集链路与 DTU 在线状态。"
                : "通讯链路当前平稳，可优先处理温度和绝缘类风险。",
            pendingCount > 0
                ? "待处理与待确认共 " + pendingCount + " 条，建议同站重复告警合并闭环。"
                : "当前待处理堆积较低，可安排历史告警复盘与阈值校准。"
        );
    }

    private Map<String, Object> mapDeviceRow(DeviceViewRow row) {
        return DeviceAlarmSupport.orderedMap(
            "deviceId", row.deviceId(),
            "deviceName", row.deviceName(),
            "stationName", row.stationName(),
            "type", row.type(),
            "status", row.status(),
            "loadRate", DeviceAlarmSupport.round(row.loadRate(), 1) + "%",
            "temperature", DeviceAlarmSupport.round(row.temperature(), 1) + "℃",
            "lastReportAt", row.lastReportAt()
        );
    }

    private Map<String, Object> mapAlarmRow(AlarmViewRow row) {
        return DeviceAlarmSupport.orderedMap(
            "alarmId", row.alarmId(),
            "stationName", row.stationName(),
            "deviceName", row.deviceName(),
            "category", row.category(),
            "level", row.level(),
            "status", row.status(),
            "owner", row.owner(),
            "happenedAt", row.happenedAt()
        );
    }

    private Predicate<DeviceViewRow> keywordFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return row -> true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return row -> contains(row.deviceId(), normalized)
            || contains(row.deviceName(), normalized)
            || contains(row.stationName(), normalized)
            || contains(row.type(), normalized);
    }

    private Predicate<AlarmViewRow> alarmKeywordFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return row -> true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return row -> contains(row.alarmId(), normalized)
            || contains(row.stationName(), normalized)
            || contains(row.deviceName(), normalized)
            || contains(row.category(), normalized)
            || contains(row.description(), normalized);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private record DeviceViewRow(
        String deviceId,
        String deviceName,
        String stationName,
        String type,
        String status,
        double loadRate,
        double temperature,
        String lastReportAt,
        String strategyName,
        String strategyType
    ) {
    }

    private record AlarmViewRow(
        String alarmId,
        String stationName,
        String deviceName,
        String category,
        String level,
        String status,
        String owner,
        String happenedAt,
        LocalDateTime happenedAtValue,
        String description
    ) {
    }
}
