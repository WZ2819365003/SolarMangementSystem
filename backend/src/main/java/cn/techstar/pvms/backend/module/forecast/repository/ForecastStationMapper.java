package cn.techstar.pvms.backend.module.forecast.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ForecastStationMapper {

    record StationRow(
        String id,
        String name,
        String companyId,
        String companyName,
        String resourceUnitId,
        String region,
        double capacityKwp,
        String status,
        String dataQuality,
        int sortIndex
    ) {
    }

    List<StationRow> findAll();
}
