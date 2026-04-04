package cn.techstar.pvms.backend.module.strategy.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyPriceMapper {

    record PricePeriodRow(
        String stationId,
        int periodOrder,
        int startSlot,
        int endSlot,
        String priceType,
        double priceCnyPerKwh
    ) {
    }

    List<PricePeriodRow> findByStationId(@Param("stationId") String stationId);

    List<PricePeriodRow> findTemplate();
}
