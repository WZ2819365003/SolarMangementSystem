package cn.techstar.pvms.backend.module.forecast.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ForecastPredictionMapper {

    record PredictionRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        LocalDate bizDate,
        int timeSlot,
        String forecastType,
        double predictedPowerKw,
        double upperBoundKw,
        double lowerBoundKw,
        String scenarioTag
    ) {
    }

    List<PredictionRow> findByDate(@Param("bizDate") LocalDate bizDate);
}
