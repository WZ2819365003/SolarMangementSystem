package cn.techstar.pvms.backend.module.strategy.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StrategyRevenueMapper {

    record RevenueDailyRow(
        String strategyId,
        LocalDate bizDate,
        double estimatedRevenueCny,
        double actualRevenueCny,
        double peakSavingCny,
        double responseRewardCny,
        double penaltyCny,
        String strategyName,
        String type,
        String status,
        String stationId,
        String stationName,
        String resourceUnitId,
        String companyId,
        String companyName,
        String region
    ) {
    }

    record RevenueDailyMutationRow(
        String strategyId,
        LocalDate bizDate,
        double estimatedRevenueCny,
        double actualRevenueCny,
        double peakSavingCny,
        double responseRewardCny,
        double penaltyCny
    ) {
    }

    List<RevenueDailyRow> findAll();

    List<RevenueDailyRow> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    int insertRevenueDaily(@Param("row") RevenueDailyMutationRow row);
}
