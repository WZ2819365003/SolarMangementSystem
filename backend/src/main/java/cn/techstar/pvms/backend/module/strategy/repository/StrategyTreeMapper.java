package cn.techstar.pvms.backend.module.strategy.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyTreeMapper {

    record StationTreeRow(
        String companyId,
        String companyName,
        String region,
        int companySortIndex,
        String stationId,
        String stationName,
        double capacityKwp,
        String stationStatus,
        String dataQuality,
        int stationSortIndex,
        int strategyCount,
        int activeCount,
        int pendingCount
    ) {
    }

    List<StationTreeRow> findCompanyStationRows();
}
