package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StationArchiveInverterRepository {

    private final JdbcClient jdbcClient;

    public StationArchiveInverterRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<InverterRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                i.id,
                i.station_id,
                s.name AS station_name,
                i.name,
                i.sort_index,
                i.rated_power_kw,
                i.status,
                i.model,
                i.manufacturer,
                i.serial_no,
                i.firmware_version,
                i.install_date,
                i.mppt_channels,
                i.dc_input_voltage_v,
                i.ac_output_voltage_v,
                i.grid_frequency_hz,
                i.module_temperature_c,
                i.ambient_temperature_c,
                i.string_count,
                i.panels_per_string
            FROM sa_inverter i
            JOIN sa_station s ON s.id = i.station_id
            ORDER BY s.sort_index, i.sort_index
            """)
            .query(this::mapRow)
            .list();
    }

    public List<InverterRow> findByStationId(String stationId) {
        return jdbcClient.sql("""
            SELECT
                i.id,
                i.station_id,
                s.name AS station_name,
                i.name,
                i.sort_index,
                i.rated_power_kw,
                i.status,
                i.model,
                i.manufacturer,
                i.serial_no,
                i.firmware_version,
                i.install_date,
                i.mppt_channels,
                i.dc_input_voltage_v,
                i.ac_output_voltage_v,
                i.grid_frequency_hz,
                i.module_temperature_c,
                i.ambient_temperature_c,
                i.string_count,
                i.panels_per_string
            FROM sa_inverter i
            JOIN sa_station s ON s.id = i.station_id
            WHERE i.station_id = :stationId
            ORDER BY i.sort_index
            """)
            .param("stationId", stationId)
            .query(this::mapRow)
            .list();
    }

    public InverterRow findById(String inverterId) {
        return jdbcClient.sql("""
            SELECT
                i.id,
                i.station_id,
                s.name AS station_name,
                i.name,
                i.sort_index,
                i.rated_power_kw,
                i.status,
                i.model,
                i.manufacturer,
                i.serial_no,
                i.firmware_version,
                i.install_date,
                i.mppt_channels,
                i.dc_input_voltage_v,
                i.ac_output_voltage_v,
                i.grid_frequency_hz,
                i.module_temperature_c,
                i.ambient_temperature_c,
                i.string_count,
                i.panels_per_string
            FROM sa_inverter i
            JOIN sa_station s ON s.id = i.station_id
            WHERE i.id = :inverterId
            """)
            .param("inverterId", inverterId)
            .query(this::mapRow)
            .single();
    }

    private InverterRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new InverterRow(
            resultSet.getString("id"),
            resultSet.getString("station_id"),
            resultSet.getString("station_name"),
            resultSet.getString("name"),
            resultSet.getInt("sort_index"),
            resultSet.getDouble("rated_power_kw"),
            resultSet.getString("status"),
            resultSet.getString("model"),
            resultSet.getString("manufacturer"),
            resultSet.getString("serial_no"),
            resultSet.getString("firmware_version"),
            resultSet.getObject("install_date", LocalDate.class),
            resultSet.getInt("mppt_channels"),
            resultSet.getDouble("dc_input_voltage_v"),
            resultSet.getDouble("ac_output_voltage_v"),
            resultSet.getDouble("grid_frequency_hz"),
            resultSet.getDouble("module_temperature_c"),
            resultSet.getDouble("ambient_temperature_c"),
            resultSet.getInt("string_count"),
            resultSet.getInt("panels_per_string")
        );
    }

    public record InverterRow(
        String id,
        String stationId,
        String stationName,
        String name,
        int sortIndex,
        double ratedPowerKw,
        String status,
        String model,
        String manufacturer,
        String serialNo,
        String firmwareVersion,
        LocalDate installDate,
        int mpptChannels,
        double dcInputVoltageV,
        double acOutputVoltageV,
        double gridFrequencyHz,
        double moduleTemperatureC,
        double ambientTemperatureC,
        int stringCount,
        int panelsPerString
    ) {
    }
}
