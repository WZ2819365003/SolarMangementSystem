INSERT INTO dashboard_station (id, name, resource_unit_id, resource_unit_name, region, longitude, latitude, address, capacity_kwp) VALUES
('PV-001', '深圳港科园区光伏电站', 'RU-001', '华南园区虚拟电厂', '华南园区', 113.9489, 22.5408, '深圳港科生态园 8 栋屋面', 2500.00),
('PV-002', '松山湖智造园光伏电站', 'RU-001', '华南园区虚拟电厂', '华南园区', 113.8854, 22.9192, '松山湖智造园 A 区厂房屋顶', 6200.00),
('PV-003', '武汉物流基地光伏电站', 'RU-002', '华中物流虚拟电厂', '华中园区', 114.3066, 30.5928, '武汉东西湖物流园 2 号仓屋顶', 4800.00),
('PV-004', '合肥研发中心光伏电站', 'RU-003', '华东研发虚拟电厂', '华东园区', 117.2272, 31.8206, '合肥研发中心 C 区车棚', 1800.00),
('PV-005', '天津港储能园光伏电站', 'RU-004', '华北储能虚拟电厂', '华北园区', 117.3616, 39.3434, '天津港储能园区堆场车棚', 3200.00),
('PV-006', '成都西部基地光伏电站', 'RU-005', '西南基地虚拟电厂', '西南园区', 104.0668, 30.5728, '成都西部基地生产楼及连廊', 5400.00);

INSERT INTO dashboard_station_status_snapshot (station_id, status, realtime_power_kw, today_energy_kwh, today_revenue_cny, availability, health_grade, snapshot_time) VALUES
('PV-001', 'normal', 1823.00, 8432.00, 3372.80, 99.20, 'A', '2026-03-30 09:30:00'),
('PV-002', 'warning', 3986.00, 20840.00, 8336.00, 98.10, 'A-', '2026-03-30 09:30:00'),
('PV-003', 'fault', 2156.00, 13542.00, 5416.80, 95.70, 'B', '2026-03-30 09:30:00'),
('PV-004', 'maintenance', 684.00, 2958.00, 1183.20, 92.40, 'B+', '2026-03-30 09:30:00'),
('PV-005', 'offline', 0.00, 1180.00, 472.00, 81.10, 'C', '2026-03-30 09:30:00'),
('PV-006', 'normal', 3618.00, 17924.00, 7169.60, 98.70, 'A', '2026-03-30 09:30:00');

INSERT INTO dashboard_alarm_snapshot (id, station_id, event_time, level, level_label, device_name, alarm_type, description, status, owner, suggestion) VALUES
('ALM-20260330-001', 'PV-003', '2026-03-30 14:32:00', 'critical', '紧急', '逆变器 INV-03', 'dc-over-voltage', '直流侧过压告警，需立即复核组串电压与断路器状态。', '未处理', '华中运维一组', '建议 15 分钟内派单复核，并同步检查同列逆变器。'),
('ALM-20260330-002', 'PV-002', '2026-03-30 14:18:00', 'major', '重要', '箱变 T1', 'transformer-high-temp', '箱变高压侧温升超过基线 8.6℃。', '处理中', '华南运维中心', '核查散热风道和负载分配，必要时切换低谷窗口检修。'),
('ALM-20260330-003', 'PV-001', '2026-03-30 13:57:00', 'minor', '一般', '汇流箱 HB-07', 'low-current', '支路电流偏低，疑似积灰或遮挡。', '待确认', '华南运维中心', '安排现场清扫，并对该支路做 IV 曲线复测。'),
('ALM-20260330-004', 'PV-006', '2026-03-30 13:40:00', 'hint', '提示', '气象站 WS-02', 'data-delay', '天气采样上报延迟 90 秒，已自动补采。', '未处理', '平台值班', '观察网络抖动是否持续，无需立即派单。'),
('ALM-20260330-005', 'PV-005', '2026-03-30 13:12:00', 'major', '重要', '采集器 DTU-11', 'communication-lost', '采集器离线超过 15 分钟，关联设备数据中断。', '未处理', '华北运维组', '优先检查交换机和 4G 备链路，恢复后补传离线数据。');

INSERT INTO dashboard_vpp_node_snapshot (node_id, total_capacity_mw, adjustable_min_mw, adjustable_max_mw, last_heartbeat) VALUES
('VPP-NODE-001', 23.90, -5.20, 8.60, '2026-03-30 09:30:00');

INSERT INTO pm_resource_unit (
    id, name, region, city, status, cluster_radius_km, dispatch_mode, strategy_label,
    latest_alarm_title, latest_alarm_time, alarm_total, alarm_critical, alarm_major, alarm_minor
) VALUES
('RU-001', '深圳湾科创园聚合单元', '华南区域', '深圳', 'normal', 8, '日前基线 + 实时修正', '深圳南山与前海相邻工商业屋顶光伏共享同城天气剖面，仅按权重分摊出力。', '前海冷站屋顶 C 站采集延迟，连续两个时段偏差超阈。', '2026-03-23 14:18:00', 2, 0, 1, 1),
('RU-002', '松山湖智造聚合单元', '华南区域', '东莞', 'warning', 10, '滚动预测 + 偏差压缩', '松山湖制造园区资源共享同一片天气趋势，按容量权重分摊实际出力。', '松山湖制造园 B 站在 13:45 后出现预测偏差持续放大。', '2026-03-23 14:06:00', 4, 0, 2, 2),
('RU-003', '武汉物流基地聚合单元', '华中区域', '武汉', 'fault', 14, '偏差压降', '武汉物流园与仓储园站点共享天气剖面，但故障站拖低聚合单元可调能力。', '武汉物流园 A 站逆变器簇故障，导致资源单元响应不完整。', '2026-03-23 14:02:00', 6, 2, 2, 2),
('RU-004', '合肥研发协同聚合单元', '华东区域', '合肥', 'maintenance', 9, '检修窗口 + 保守基线', '合肥研发中心邻近站群共享天气剖面，检修期以保守调度为主。', '合肥研发中心 A 站处于检修窗口，可调空间暂时下调。', '2026-03-23 13:32:00', 2, 0, 1, 1),
('RU-005', '天津港储能园聚合单元', '华北区域', '天津', 'offline', 11, '退出调度 + 边界保电', '天津港储能园区部分站点因通信中断脱网，当前按离线资源单元独立管理。', '天津港储能园 A 站通信中断超过 15 分钟，当前已切换至边界保电模式。', '2026-03-23 14:12:00', 4, 1, 2, 1),
('RU-006', '成都西部基地聚合单元', '西南区域', '成都', 'normal', 13, '日前基线 + 实时优化', '成都西部基地相邻园区光伏站群共享天气建模，当前可稳定提供上下调能力。', '成都仓储园 C 站传感器上报偶发延迟，已自动补采。', '2026-03-23 13:26:00', 1, 0, 0, 1);

INSERT INTO pm_station (
    id, resource_unit_id, name, capacity_mw, weight, status, online_rate, alarm_count,
    sort_index, load_visible, load_base_kw
) VALUES
('SZ-001', 'RU-001', '深圳湾科创园 A 站', 1.8, 0.34, 'normal', 99.1, 0, 1, TRUE, 2100),
('SZ-002', 'RU-001', '南山智造中心 B 站', 1.4, 0.26, 'normal', 98.7, 0, 2, TRUE, 1600),
('SZ-003', 'RU-001', '前海冷站屋顶 C 站', 1.2, 0.22, 'warning', 96.8, 1, 3, TRUE, 1500),
('SZ-004', 'RU-001', '生态园 D 站', 1.0, 0.18, 'normal', 98.9, 0, 4, TRUE, 1200),
('DG-001', 'RU-002', '松山湖智造园 A 站', 1.9, 0.31, 'normal', 97.5, 0, 5, TRUE, 2200),
('DG-002', 'RU-002', '松山湖制造园 B 站', 1.6, 0.27, 'warning', 94.2, 1, 6, TRUE, 1800),
('DG-003', 'RU-002', '松山湖配套园 C 站', 1.4, 0.23, 'warning', 93.6, 1, 7, FALSE, 1650),
('DG-004', 'RU-002', '松山湖仓储园 D 站', 1.2, 0.19, 'normal', 95.8, 0, 8, FALSE, 1500),
('WH-001', 'RU-003', '武汉物流园 A 站', 1.6, 0.41, 'fault', 82.4, 3, 9, TRUE, 1900),
('WH-002', 'RU-003', '武汉仓储园 B 站', 1.4, 0.34, 'warning', 88.9, 1, 10, TRUE, 1700),
('WH-003', 'RU-003', '鄂州协同园 C 站', 1.2, 0.25, 'normal', 91.6, 1, 11, TRUE, 1400),
('HF-001', 'RU-004', '合肥研发中心 A 站', 1.0, 0.39, 'maintenance', 90.8, 1, 12, TRUE, 1100),
('HF-002', 'RU-004', '合肥实证基地 B 站', 0.9, 0.34, 'normal', 93.1, 0, 13, TRUE, 1000),
('HF-003', 'RU-004', '合肥车棚屋顶 C 站', 0.7, 0.27, 'maintenance', 90.6, 1, 14, FALSE, 850),
('TJ-001', 'RU-005', '天津港储能园 A 站', 1.2, 0.38, 'offline', 75.2, 2, 15, TRUE, 1400),
('TJ-002', 'RU-005', '天津港仓储园 B 站', 1.0, 0.34, 'offline', 79.1, 1, 16, TRUE, 1200),
('TJ-003', 'RU-005', '天津港堆场园 C 站', 0.9, 0.28, 'warning', 82.6, 1, 17, FALSE, 1100),
('CD-001', 'RU-006', '成都西部基地 A 站', 1.7, 0.31, 'normal', 98.2, 0, 18, TRUE, 2000),
('CD-002', 'RU-006', '成都连廊产区 B 站', 1.5, 0.28, 'normal', 97.5, 0, 19, TRUE, 1700),
('CD-003', 'RU-006', '成都仓储园 C 站', 1.3, 0.23, 'warning', 95.9, 1, 20, TRUE, 1500),
('CD-004', 'RU-006', '成都配套园 D 站', 1.1, 0.18, 'normal', 98.4, 0, 21, FALSE, 1300);

INSERT INTO pm_weather_snapshot (
    resource_unit_id, snapshot_time, weather, cloudiness, temperature, irradiance, humidity, wind_speed, conclusion
) VALUES
('RU-001', '2026-03-30 14:00:00', '多云', '中云', 29, 746, 61, 3.1, '午后辐照条件稳定，适合维持上调空间。'),
('RU-002', '2026-03-30 14:00:00', '晴转多云', '低云', 28, 714, 57, 2.8, '16:00 后云量抬升，预计实际出力略低于预测。'),
('RU-003', '2026-03-30 14:00:00', '阴', '厚云', 23, 402, 72, 2.1, '云层持续偏厚，建议限制上调并优先压缩偏差风险。'),
('RU-004', '2026-03-30 14:00:00', '小雨', '中云', 21, 318, 76, 3.5, '检修时段叠加阵雨，建议降低上调预期并保持基线稳定。'),
('RU-005', '2026-03-30 14:00:00', '大风', '少云', 16, 458, 48, 7.3, '大风和通信中断叠加，当前不建议承接新增调度指令。'),
('RU-006', '2026-03-30 14:00:00', '晴', '少云', 24, 648, 57, 2.4, '辐照度和温度均处于稳定区间，当前具备较好调度弹性。');

INSERT INTO pm_station_curve_15m (
    station_id, biz_date, time_slot, load_kw, pv_power_kw, forecast_power_kw,
    baseline_power_kw, irradiance, temperature
)
SELECT
    s.id,
    DATE '2026-03-30',
    slot_range.X,
    ROUND(
        s.load_base_kw * (
            0.40
            + 0.35 * EXP(-0.5 * POWER((((slot_range.X * 15.0) / 60.0) - 10.0) / 1.5, 2))
            + 0.30 * EXP(-0.5 * POWER((((slot_range.X * 15.0) / 60.0) - 15.5) / 2.0, 2))
            + CASE
                WHEN ((slot_range.X * 15.0) / 60.0) < 6 OR ((slot_range.X * 15.0) / 60.0) > 22 THEN -0.15
                ELSE 0
            END
            + SIN((s.sort_index * 13.0 + slot_range.X) / 9.0) * 0.03
        ),
        1
    ) AS load_kw,
    ROUND(
        GREATEST(
            0,
            s.capacity_mw * 1000.0
            * GREATEST(0, SIN((((slot_range.X * 15.0) / 60.0) - 6.0) / 12.0 * PI()))
            * CASE s.status
                WHEN 'normal' THEN 0.94
                WHEN 'warning' THEN 0.82
                WHEN 'maintenance' THEN 0.68
                WHEN 'fault' THEN 0.46
                WHEN 'offline' THEN 0.12
                ELSE 0.90
            END
            * (0.86 + COS((s.sort_index * 7.0 + slot_range.X) / 8.0) * 0.04)
        ),
        1
    ) AS pv_power_kw,
    ROUND(
        GREATEST(
            0,
            s.capacity_mw * 1000.0
            * GREATEST(0, SIN((((slot_range.X * 15.0) / 60.0) - 6.0) / 12.0 * PI()))
            * CASE w.cloudiness
                WHEN '厚云' THEN 0.55
                WHEN '中云' THEN 0.82
                WHEN '低云' THEN 0.90
                WHEN '少云' THEN 0.94
                ELSE 0.88
            END
            * (0.96 + SIN((s.sort_index * 5.0 + slot_range.X) / 10.0) * 0.02)
        ),
        1
    ) AS forecast_power_kw,
    ROUND(
        GREATEST(
            0,
            s.capacity_mw * 1000.0
            * GREATEST(0, SIN((((slot_range.X * 15.0) / 60.0) - 6.0) / 12.0 * PI()))
            * CASE w.cloudiness
                WHEN '厚云' THEN 0.58
                WHEN '中云' THEN 0.84
                WHEN '低云' THEN 0.92
                WHEN '少云' THEN 0.96
                ELSE 0.90
            END
            * 0.98
        )
        + 12,
        1
    ) AS baseline_power_kw,
    CAST(
        ROUND(
            GREATEST(
                0,
                w.irradiance * GREATEST(0, SIN((((slot_range.X * 15.0) / 60.0) - 6.0) / 12.0 * PI()))
                + COS((s.sort_index + slot_range.X) / 6.0) * 20
            ),
            0
        ) AS INT
    ) AS irradiance,
    ROUND(
        w.temperature + SIN(slot_range.X / 6.0) * 2.1,
        1
    ) AS temperature
FROM pm_station s
JOIN pm_weather_snapshot w ON w.resource_unit_id = s.resource_unit_id
CROSS JOIN SYSTEM_RANGE(0, 95) AS slot_range;

INSERT INTO pm_station_realtime_snapshot (
    station_id, snapshot_time, realtime_power_kw, load_kw, availability, health_grade
)
SELECT
    s.id,
    TIMESTAMP '2026-03-30 14:00:00',
    c.pv_power_kw,
    c.load_kw,
    ROUND(
        CASE s.status
            WHEN 'normal' THEN LEAST(99.9, s.online_rate + 0.5)
            WHEN 'warning' THEN GREATEST(80.0, s.online_rate - 0.4)
            WHEN 'maintenance' THEN GREATEST(78.0, s.online_rate - 1.8)
            WHEN 'fault' THEN GREATEST(70.0, s.online_rate - 4.6)
            WHEN 'offline' THEN GREATEST(60.0, s.online_rate - 8.2)
            ELSE s.online_rate
        END,
        1
    ) AS availability,
    CASE s.status
        WHEN 'normal' THEN 'A'
        WHEN 'warning' THEN 'B+'
        WHEN 'maintenance' THEN 'B'
        WHEN 'fault' THEN 'C'
        WHEN 'offline' THEN 'D'
        ELSE 'B'
    END AS health_grade
FROM pm_station s
JOIN pm_station_curve_15m c
    ON c.station_id = s.id
   AND c.biz_date = DATE '2026-03-30'
   AND c.time_slot = 56;

INSERT INTO pm_dispatch_record (
    id, resource_unit_id, issued_at, command_type, target_power_mw, actual_power_mw,
    response_seconds, status, deviation_reason
)
SELECT
    CONCAT('PMD-', u.id, '-', slot_range.X + 1),
    u.id,
    TIMESTAMPADD(HOUR, slot_range.X + 9, TIMESTAMP '2026-03-30 00:00:00'),
    CASE WHEN MOD(slot_range.X, 2) = 0 THEN '上调' ELSE '下调' END,
    ROUND(
        (
            SELECT COALESCE(SUM(rs.realtime_power_kw), 0) / 1000.0
            FROM pm_station_realtime_snapshot rs
            JOIN pm_station s ON s.id = rs.station_id
            WHERE s.resource_unit_id = u.id
        )
        + CASE WHEN MOD(slot_range.X, 2) = 0 THEN 0.22 ELSE -0.18 END
        + slot_range.X * 0.03,
        2
    ) AS target_power_mw,
    ROUND(
        (
            SELECT COALESCE(SUM(rs.realtime_power_kw), 0) / 1000.0
            FROM pm_station_realtime_snapshot rs
            JOIN pm_station s ON s.id = rs.station_id
            WHERE s.resource_unit_id = u.id
        )
        + CASE
            WHEN u.status IN ('fault', 'offline') AND slot_range.X >= 4 THEN -0.24
            WHEN u.status IN ('fault', 'offline') THEN -0.08
            WHEN u.status = 'maintenance' AND slot_range.X >= 4 THEN -0.10
            ELSE 0.06
        END
        + CASE WHEN MOD(slot_range.X, 2) = 0 THEN 0.15 ELSE -0.14 END,
        2
    ) AS actual_power_mw,
    CASE
        WHEN u.status IN ('fault', 'offline') THEN 148 + slot_range.X * 12
        WHEN u.status = 'maintenance' THEN 112 + slot_range.X * 8
        ELSE 86 + slot_range.X * 4
    END AS response_seconds,
    CASE
        WHEN u.status IN ('fault', 'offline') AND slot_range.X >= 4 THEN '偏差超阈'
        WHEN u.status IN ('fault', 'offline') THEN '处理中'
        WHEN u.status = 'maintenance' AND slot_range.X = 5 THEN '处理中'
        ELSE '已完成'
    END AS status,
    CASE
        WHEN u.status IN ('fault', 'offline') AND slot_range.X >= 4 THEN '天气扰动与设备异常叠加，执行偏差仍在放大'
        WHEN u.status IN ('fault', 'offline') THEN '通信与设备状态波动导致回执滞后'
        WHEN u.status = 'maintenance' AND slot_range.X = 5 THEN '检修窗口内保守响应，保持安全裕度'
        ELSE '执行闭环正常'
    END AS deviation_reason
FROM pm_resource_unit u
CROSS JOIN SYSTEM_RANGE(0, 5) AS slot_range;
