package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ProductionMonitorCurveMapper {

    List<CurveRow> findByResourceUnitIdAndDate(
        @Param("resourceUnitId") String resourceUnitId,
        @Param("bizDate") LocalDate bizDate
    );

    List<CurveRow> findLoadVisibleByDate(@Param("bizDate") LocalDate bizDate);

    record CurveRow(
        String stationId,
        String resourceUnitId,
        String stationName,
        double capacityMw,
        double weight,
        String status,
        int sortIndex,
        boolean loadVisible,
        LocalDate bizDate,
        int timeSlot,
        double loadKw,
        double pvPowerKw,
        double forecastPowerKw,
        double baselinePowerKw,
        int irradiance,
        double temperature
    ) {
    }
}
