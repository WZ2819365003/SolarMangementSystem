package cn.techstar.pvms.backend.module.dashboard.repository;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DashboardStationGeoMapper {

    record StationGeoRow(
        String id,
        String name,
        String resourceUnitId,
        String resourceUnitName,
        String region,
        double longitude,
        double latitude,
        String address,
        double capacityKwp,
        String status,
        double realtimePowerKw,
        double todayEnergyKwh,
        double todayRevenueCny,
        double availability,
        String healthGrade,
        LocalDateTime snapshotTime
    ) {
    }

    List<StationGeoRow> findAll();
}
