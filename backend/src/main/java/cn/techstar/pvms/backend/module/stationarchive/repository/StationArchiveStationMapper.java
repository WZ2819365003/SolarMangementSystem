package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StationArchiveStationMapper {

    List<StationRow> findAll();

    record StationRow(
        String id,
        String companyId,
        String companyName,
        String name,
        double capacityKwp,
        String status,
        String dataQuality,
        String gridStatus,
        String gridStatusLabel,
        LocalDate commissionDate,
        String address,
        double loadBaseKw,
        int sortIndex
    ) {
    }
}
