package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProductionMonitorWeatherSnapshotMapper {

    List<WeatherSnapshotRow> findAll();

    record WeatherSnapshotRow(
        String resourceUnitId,
        LocalDateTime snapshotTime,
        String weather,
        String cloudiness,
        int temperature,
        int irradiance,
        int humidity,
        double windSpeed,
        String conclusion
    ) {
    }
}
