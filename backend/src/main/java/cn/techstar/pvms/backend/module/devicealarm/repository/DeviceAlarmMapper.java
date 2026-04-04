package cn.techstar.pvms.backend.module.devicealarm.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeviceAlarmMapper {

    record DeviceFactRow(
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

    record AlarmFactRow(
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

    List<DeviceFactRow> findDeviceFacts(@Param("bizDate") LocalDate bizDate, @Param("timeSlot") int timeSlot);

    List<AlarmFactRow> findAlarmFacts();
}
