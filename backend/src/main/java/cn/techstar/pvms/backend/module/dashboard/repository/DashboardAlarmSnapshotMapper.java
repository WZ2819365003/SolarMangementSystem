package cn.techstar.pvms.backend.module.dashboard.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DashboardAlarmSnapshotMapper {

    record AlarmSnapshotRow(
        String id,
        String stationId,
        String stationName,
        LocalDateTime eventTime,
        String level,
        String levelLabel,
        String deviceName,
        String alarmType,
        String description,
        String status,
        String owner,
        String suggestion
    ) {
    }

    record AlarmSummary(
        long critical,
        long major,
        long minor,
        long hint
    ) {
    }

    List<AlarmSnapshotRow> findRecent(@Param("level") String level, @Param("stationId") String stationId);

    AlarmSummary summary(@Param("level") String level, @Param("stationId") String stationId);
}
