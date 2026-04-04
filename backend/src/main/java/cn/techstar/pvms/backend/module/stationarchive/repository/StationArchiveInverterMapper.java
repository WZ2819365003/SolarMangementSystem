package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StationArchiveInverterMapper {

    List<InverterRow> findAll();

    List<InverterRow> findByStationId(@Param("stationId") String stationId);

    InverterRow findById(@Param("inverterId") String inverterId);

    record InverterRow(
        String id,
        String stationId,
        String stationName,
        String name,
        int sortIndex,
        double ratedPowerKw,
        String status,
        String model,
        String manufacturer,
        String serialNo,
        String firmwareVersion,
        LocalDate installDate,
        int mpptChannels,
        double dcInputVoltageV,
        double acOutputVoltageV,
        double gridFrequencyHz,
        double moduleTemperatureC,
        double ambientTemperatureC,
        int stringCount,
        int panelsPerString
    ) {
    }
}
