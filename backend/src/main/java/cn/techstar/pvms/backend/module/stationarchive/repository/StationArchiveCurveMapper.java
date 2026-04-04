package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StationArchiveCurveMapper {

    List<CurveRow> findByDate(@Param("bizDate") LocalDate bizDate);

    List<CurveRow> findByStationIdAndDate(@Param("stationId") String stationId, @Param("bizDate") LocalDate bizDate);

    record CurveRow(
        String stationId,
        String companyId,
        String stationName,
        String status,
        double loadBaseKw,
        double capacityKwp,
        int sortIndex,
        LocalDate bizDate,
        int timeSlot,
        double loadKw,
        double pvOutputKw,
        double forecastDayAheadKw,
        double forecastUltraShortKw
    ) {
    }
}
