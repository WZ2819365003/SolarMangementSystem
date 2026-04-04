package cn.techstar.pvms.backend.module.forecast.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ForecastErrorSampleMapper {

    record ErrorSampleRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        LocalDate bizDate,
        int hourSlot,
        String forecastType,
        double errorKw,
        boolean qualified
    ) {
    }

    List<ErrorSampleRow> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
