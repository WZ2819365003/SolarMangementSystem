package cn.techstar.pvms.backend.module.forecast.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ForecastMonthlyAccuracyMapper {

    record MonthlyAccuracyRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        String monthKey,
        String forecastType,
        double maeKw,
        double rmseKw,
        double accuracyPct
    ) {
    }

    List<MonthlyAccuracyRow> findAll();
}
