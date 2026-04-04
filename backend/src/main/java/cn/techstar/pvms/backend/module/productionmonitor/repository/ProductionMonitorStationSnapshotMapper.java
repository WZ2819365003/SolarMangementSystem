package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProductionMonitorStationSnapshotMapper {

    List<StationSnapshotRow> findAll();

    record StationSnapshotRow(
        String id,
        String resourceUnitId,
        String name,
        double capacityMw,
        double weight,
        String status,
        double onlineRate,
        int alarmCount,
        int sortIndex,
        boolean loadVisible,
        double loadBaseKw,
        LocalDateTime snapshotTime,
        double realtimePowerKw,
        double loadKw,
        double availability,
        String healthGrade
    ) {
    }
}
