package cn.techstar.pvms.backend.module.dashboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class DashboardVppNodeSnapshotRepository {

    private final JdbcClient jdbcClient;

    public DashboardVppNodeSnapshotRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public VppNodeSnapshotRow findDefault() {
        return jdbcClient.sql("""
            SELECT
                node_id,
                total_capacity_mw,
                adjustable_min_mw,
                adjustable_max_mw,
                last_heartbeat
            FROM dashboard_vpp_node_snapshot
            ORDER BY node_id
            LIMIT 1
            """)
            .query((resultSet, rowNum) -> new VppNodeSnapshotRow(
                resultSet.getString("node_id"),
                resultSet.getDouble("total_capacity_mw"),
                resultSet.getDouble("adjustable_min_mw"),
                resultSet.getDouble("adjustable_max_mw"),
                toLocalDateTime(resultSet.getTimestamp("last_heartbeat"))
            ))
            .single();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record VppNodeSnapshotRow(
        String nodeId,
        double totalCapacityMw,
        double adjustableMinMw,
        double adjustableMaxMw,
        LocalDateTime lastHeartbeat
    ) {
    }
}
