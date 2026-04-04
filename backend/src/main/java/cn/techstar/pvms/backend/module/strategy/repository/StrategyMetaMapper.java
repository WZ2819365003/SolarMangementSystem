package cn.techstar.pvms.backend.module.strategy.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyMetaMapper {

    record CompanyRow(
        String id,
        String name,
        String region,
        int sortIndex
    ) {
    }

    record StationRow(
        String id,
        String companyId,
        String companyName,
        String region,
        String name,
        double capacityKwp,
        String status,
        String dataQuality,
        int sortIndex
    ) {
    }

    List<CompanyRow> findCompanies();

    List<StationRow> findStations();
}
