package cn.techstar.pvms.backend.module.strategy.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StrategyRevenueRepository {

    private final JdbcClient jdbcClient;

    public StrategyRevenueRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<RevenueDailyRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                revenue.strategy_id,
                revenue.biz_date,
                revenue.estimated_revenue_cny,
                revenue.actual_revenue_cny,
                revenue.peak_saving_cny,
                revenue.response_reward_cny,
                revenue.penalty_cny,
                strategy.name AS strategy_name,
                strategy.type,
                strategy.status,
                strategy.station_id,
                station.name AS station_name,
                strategy.company_id,
                company.name AS company_name,
                company.region
            FROM sg_revenue_daily revenue
            JOIN sg_strategy strategy ON strategy.id = revenue.strategy_id
            JOIN sa_station station ON station.id = strategy.station_id
            JOIN sa_company company ON company.id = strategy.company_id
            ORDER BY revenue.biz_date DESC, strategy.updated_at DESC
            """)
            .query((resultSet, rowNum) -> new RevenueDailyRow(
                resultSet.getString("strategy_id"),
                resultSet.getObject("biz_date", LocalDate.class),
                resultSet.getDouble("estimated_revenue_cny"),
                resultSet.getDouble("actual_revenue_cny"),
                resultSet.getDouble("peak_saving_cny"),
                resultSet.getDouble("response_reward_cny"),
                resultSet.getDouble("penalty_cny"),
                resultSet.getString("strategy_name"),
                resultSet.getString("type"),
                resultSet.getString("status"),
                resultSet.getString("station_id"),
                resultSet.getString("station_name"),
                resultSet.getString("company_id"),
                resultSet.getString("company_name"),
                resultSet.getString("region")
            ))
            .list();
    }

    public List<RevenueDailyRow> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return findAll().stream()
            .filter(item -> !item.bizDate().isBefore(startDate) && !item.bizDate().isAfter(endDate))
            .toList();
    }

    public int insertRevenueDaily(RevenueDailyMutationRow row) {
        return jdbcClient.sql("""
            INSERT INTO sg_revenue_daily (
                strategy_id, biz_date, estimated_revenue_cny, actual_revenue_cny,
                peak_saving_cny, response_reward_cny, penalty_cny
            ) VALUES (
                :strategyId, :bizDate, :estimatedRevenueCny, :actualRevenueCny,
                :peakSavingCny, :responseRewardCny, :penaltyCny
            )
            """)
            .param("strategyId", row.strategyId())
            .param("bizDate", row.bizDate())
            .param("estimatedRevenueCny", row.estimatedRevenueCny())
            .param("actualRevenueCny", row.actualRevenueCny())
            .param("peakSavingCny", row.peakSavingCny())
            .param("responseRewardCny", row.responseRewardCny())
            .param("penaltyCny", row.penaltyCny())
            .update();
    }

    public record RevenueDailyRow(
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
        String companyId,
        String companyName,
        String region
    ) {
    }

    public record RevenueDailyMutationRow(
        String strategyId,
        LocalDate bizDate,
        double estimatedRevenueCny,
        double actualRevenueCny,
        double peakSavingCny,
        double responseRewardCny,
        double penaltyCny
    ) {
    }
}
