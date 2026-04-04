package cn.techstar.pvms.backend.module.forecast.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ForecastCurveMapper {

    record CurveRow(
        String stationId,
        String stationName,
        String region,
        String status,
        String dataQuality,
        int sortIndex,
        LocalDate bizDate,
        int timeSlot,
        double loadKw,
        double pvOutputKw,
        double forecastDayAheadKw,
        double forecastUltraShortKw
    ) {
    }

    List<CurveRow> findByDate(@Param("bizDate") LocalDate bizDate);
}
