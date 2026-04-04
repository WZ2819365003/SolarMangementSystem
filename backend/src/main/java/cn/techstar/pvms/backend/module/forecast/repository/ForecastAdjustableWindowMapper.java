package cn.techstar.pvms.backend.module.forecast.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ForecastAdjustableWindowMapper {

    record AdjustableWindowRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        LocalDate bizDate,
        int windowOrder,
        int startSlot,
        int endSlot,
        String windowStatus
    ) {
    }

    List<AdjustableWindowRow> findByDate(@Param("bizDate") LocalDate bizDate);
}
