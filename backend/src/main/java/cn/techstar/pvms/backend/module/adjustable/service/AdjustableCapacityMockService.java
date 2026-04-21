package cn.techstar.pvms.backend.module.adjustable.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdjustableCapacityMockService {

    private static final List<StationRef> STATIONS = List.of(
        new StationRef("ST-001", "深圳港科园区光伏电站", 2500, 1823, "normal"),
        new StationRef("ST-002", "松山湖智造园光伏电站", 6200, 3986, "warning"),
        new StationRef("ST-003", "南山科技园分布式", 1800, 1245, "normal"),
        new StationRef("ST-004", "前海综合能源站", 3200, 2156, "normal"),
        new StationRef("ST-006", "光明产业园光伏", 2800, 1920, "normal"),
        new StationRef("ST-007", "武汉物流基地光伏电站", 4800, 2156, "fault"),
        new StationRef("ST-008", "武汉光谷园区光伏", 3600, 2480, "normal"),
        new StationRef("ST-009", "黄陂仓储光伏", 2200, 1510, "normal"),
        new StationRef("ST-011", "肥西产业园光伏", 3000, 2060, "normal"),
        new StationRef("ST-013", "滨海新区光伏", 2600, 1780, "normal"),
        new StationRef("ST-014", "成都西部基地光伏电站", 5400, 3618, "normal"),
        new StationRef("ST-015", "双流工业园光伏", 2400, 1650, "normal"),
        new StationRef("ST-016", "新都物流园光伏", 1600, 1100, "normal")
    );

    public Map<String, Object> getRealtime() {
        double totalCapacity = STATIONS.stream().mapToDouble(StationRef::capacityKw).sum();
        double currentPower = STATIONS.stream().mapToDouble(StationRef::currentPowerKw).sum();

        List<Map<String, Object>> stationList = STATIONS.stream().map(s -> {
            double upFactor = "fault".equals(s.status()) ? 0.1 : 0.88;
            double downFactor = "fault".equals(s.status()) ? 0.1 : 0.56;
            double adjustUp = Math.round(s.capacityKw() * upFactor - s.currentPowerKw());
            double adjustDown = Math.round(s.currentPowerKw() - s.capacityKw() * downFactor);
            return orderedMap(
                "id", s.id(), "name", s.name(), "capacityKw", s.capacityKw(),
                "currentPowerKw", s.currentPowerKw(),
                "adjustableUpKw", Math.max(0, adjustUp),
                "adjustableDownKw", Math.max(0, adjustDown)
            );
        }).toList();

        double totalUp = stationList.stream().mapToDouble(m -> ((Number) m.get("adjustableUpKw")).doubleValue()).sum();
        double totalDown = stationList.stream().mapToDouble(m -> ((Number) m.get("adjustableDownKw")).doubleValue()).sum();
        double deferrableLoad = Math.round(totalCapacity * 0.05);

        List<Map<String, Object>> timeline = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            double hour = i / 4.0;
            double solar = Math.max(0, Math.sin(((hour - 6) / 12) * Math.PI));
            double power = Math.round(totalCapacity * solar * 0.72);
            double up = Math.round(totalCapacity * solar * 0.16);
            double down = Math.round(power * 0.35);
            timeline.add(orderedMap(
                "time", String.format("%02d:%02d", (int) hour, (i % 4) * 15),
                "totalPowerKw", power,
                "adjustableUpKw", up,
                "adjustableDownKw", down
            ));
        }

        return orderedMap(
            "summary", orderedMap(
                "totalCapacityKw", Math.round(totalCapacity),
                "currentPowerKw", Math.round(currentPower),
                "maxAdjustableUpKw", Math.round(totalUp),
                "maxAdjustableDownKw", Math.round(totalDown),
                "deferrableLoadKw", deferrableLoad,
                "utilizationRate", Math.round(currentPower * 1000.0 / totalCapacity) / 10.0
            ),
            "stations", stationList,
            "timeline", timeline
        );
    }

    private static Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((String) entries[i], entries[i + 1]);
        }
        return result;
    }

    private record StationRef(String id, String name, double capacityKw, double currentPowerKw, String status) {}
}
