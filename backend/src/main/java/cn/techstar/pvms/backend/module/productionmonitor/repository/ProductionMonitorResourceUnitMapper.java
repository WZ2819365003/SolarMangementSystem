package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProductionMonitorResourceUnitMapper {

    List<ResourceUnitRow> findAll();

    record ResourceUnitRow(
        String id,
        String name,
        String region,
        String city,
        String status,
        int clusterRadiusKm,
        String dispatchMode,
        String strategyLabel,
        String latestAlarmTitle,
        LocalDateTime latestAlarmTime,
        int alarmTotal,
        int alarmCritical,
        int alarmMajor,
        int alarmMinor
    ) {
    }
}
