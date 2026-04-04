package cn.techstar.pvms.backend.module.dashboard.repository;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface DashboardVppNodeSnapshotMapper {

    record VppNodeSnapshotRow(
        String nodeId,
        double totalCapacityMw,
        double adjustableMinMw,
        double adjustableMaxMw,
        LocalDateTime lastHeartbeat
    ) {
    }

    VppNodeSnapshotRow findDefault();
}
