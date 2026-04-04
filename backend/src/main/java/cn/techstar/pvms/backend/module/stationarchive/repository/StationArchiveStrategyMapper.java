package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StationArchiveStrategyMapper {

    List<StrategyRow> findAll();

    StrategyRow findByStationId(@Param("stationId") String stationId);

    record StrategyRow(
        String stationId,
        String stationName,
        String name,
        String type,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        double targetPowerKw,
        double estimatedRevenueCny
    ) {
    }
}
