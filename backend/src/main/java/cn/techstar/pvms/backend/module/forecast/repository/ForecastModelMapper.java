package cn.techstar.pvms.backend.module.forecast.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ForecastModelMapper {

    record ModelRow(
        String id,
        String name,
        String type,
        String version,
        String provider,
        String description,
        String status
    ) {
    }

    List<ModelRow> findAll();
}
