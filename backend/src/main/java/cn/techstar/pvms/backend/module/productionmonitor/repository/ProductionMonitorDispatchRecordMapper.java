package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProductionMonitorDispatchRecordMapper {

    List<DispatchRecordRow> findByResourceUnitId(@Param("resourceUnitId") String resourceUnitId);

    record DispatchRecordRow(
        String id,
        String resourceUnitId,
        LocalDateTime issuedAt,
        String commandType,
        double targetPowerMw,
        double actualPowerMw,
        int responseSeconds,
        String status,
        String deviationReason
    ) {
    }
}
