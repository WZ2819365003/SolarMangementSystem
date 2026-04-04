package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StationArchiveInverterAlarmMapper {

    List<InverterAlarmRow> findByInverterId(@Param("inverterId") String inverterId);

    record InverterAlarmRow(
        String id,
        String inverterId,
        LocalDateTime eventTime,
        String type,
        String level,
        String description,
        String status
    ) {
    }
}
