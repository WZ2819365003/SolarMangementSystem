package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StationArchiveCompanyMapper {

    List<CompanyRow> findAll();

    record CompanyRow(
        String id,
        String name,
        String region,
        int sortIndex
    ) {
    }
}
