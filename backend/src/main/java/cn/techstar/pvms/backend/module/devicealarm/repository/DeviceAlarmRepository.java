package cn.techstar.pvms.backend.module.devicealarm.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DeviceAlarmRepository {

    private final JdbcClient jdbcClient;

    public DeviceAlarmRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<DeviceFactRow> findDeviceFacts(LocalDate bizDate, int timeSlot) {
        return jdbcClient.sql("""
            SELECT
                inverter.id AS device_id,
                inverter.name AS device_name,
                inverter.station_id,
                station.name AS station_name,
                company.region,
                company.name AS company_name,
                inverter.sort_index,
                inverter.rated_power_kw,
                inverter.status,
                inverter.model,
                inverter.manufacturer,
                inverter.module_temperature_c,
                inverter.ambient_temperature_c,
                curve.pv_output_kw,
                counts.inverter_count,
                strategy.name AS strategy_name,
                strategy.type AS strategy_type
            FROM sa_inverter inverter
            JOIN sa_station station ON station.id = inverter.station_id
            JOIN sa_company company ON company.id = station.company_id
            LEFT JOIN sa_station_curve_15m curve
              ON curve.station_id = inverter.station_id
             AND curve.biz_date = :bizDate
             AND curve.time_slot = :timeSlot
            LEFT JOIN (
                SELECT station_id, COUNT(*) AS inverter_count
                FROM sa_inverter
                GROUP BY station_id
            ) counts ON counts.station_id = inverter.station_id
            LEFT JOIN sa_station_strategy strategy ON strategy.station_id = inverter.station_id
            ORDER BY company.sort_index, station.sort_index, inverter.sort_index
            """)
            .param("bizDate", bizDate)
            .param("timeSlot", timeSlot)
            .query((resultSet, rowNum) -> new DeviceFactRow(
                resultSet.getString("device_id"),
                resultSet.getString("device_name"),
                resultSet.getString("station_id"),
                resultSet.getString("station_name"),
                resultSet.getString("region"),
                resultSet.getString("company_name"),
                resultSet.getInt("sort_index"),
                resultSet.getDouble("rated_power_kw"),
                resultSet.getString("status"),
                resultSet.getString("model"),
                resultSet.getString("manufacturer"),
                resultSet.getDouble("module_temperature_c"),
                resultSet.getDouble("ambient_temperature_c"),
                nullableDouble(resultSet, "pv_output_kw"),
                resultSet.getInt("inverter_count"),
                resultSet.getString("strategy_name"),
                resultSet.getString("strategy_type")
            ))
            .list();
    }

    public List<AlarmFactRow> findAlarmFacts() {
        return jdbcClient.sql("""
            SELECT
                alarm.id AS alarm_id,
                alarm.inverter_id,
                alarm.event_time,
                alarm.type,
                alarm.level,
                alarm.description,
                alarm.status,
                inverter.name AS device_name,
                inverter.model,
                inverter.status AS device_status,
                station.id AS station_id,
                station.name AS station_name,
                company.region,
                company.name AS company_name
            FROM sa_inverter_alarm alarm
            JOIN sa_inverter inverter ON inverter.id = alarm.inverter_id
            JOIN sa_station station ON station.id = inverter.station_id
            JOIN sa_company company ON company.id = station.company_id
            ORDER BY alarm.event_time DESC
            """)
            .query((resultSet, rowNum) -> new AlarmFactRow(
                resultSet.getString("alarm_id"),
                resultSet.getString("inverter_id"),
                toLocalDateTime(resultSet.getTimestamp("event_time")),
                resultSet.getString("type"),
                resultSet.getString("level"),
                resultSet.getString("description"),
                resultSet.getString("status"),
                resultSet.getString("device_name"),
                resultSet.getString("model"),
                resultSet.getString("device_status"),
                resultSet.getString("station_id"),
                resultSet.getString("station_name"),
                resultSet.getString("region"),
                resultSet.getString("company_name")
            ))
            .list();
    }

    private Double nullableDouble(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record DeviceFactRow(
        String deviceId,
        String deviceName,
        String stationId,
        String stationName,
        String region,
        String companyName,
        int sortIndex,
        double ratedPowerKw,
        String rawStatus,
        String model,
        String manufacturer,
        double moduleTemperatureC,
        double ambientTemperatureC,
        Double stationPvOutputKw,
        int inverterCount,
        String strategyName,
        String strategyType
    ) {
    }

    public record AlarmFactRow(
        String alarmId,
        String inverterId,
        LocalDateTime eventTime,
        String type,
        String rawLevel,
        String description,
        String rawStatus,
        String deviceName,
        String model,
        String deviceStatus,
        String stationId,
        String stationName,
        String region,
        String companyName
    ) {
    }
}
