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
('ALM-20260330-005', 'PV-005', '2026-03-30 13:12:00', 'major', '重要', '采集器 DTU-11', 'communication-lost', '采集器离线超过 15 分钟，关联设备数据中断。', '未处理', '华北运维组', '优先检查交换机和 4G 备链路，恢复后补传离线数据。'),
('ALM-20260407-001', 'PV-003', '2026-04-07 08:12:00', 'critical', '紧急', '逆变器 INV-07', 'ground-fault', '直流侧接地故障，绝缘电阻低于 500kΩ，自动离网保护已触发。', '未处理', '华中运维一组', '立即断开直流侧，检查组串绝缘，排查接地点后方可复网。'),
('ALM-20260407-002', 'PV-002', '2026-04-07 07:55:00', 'critical', '紧急', '箱变 T2', 'overvoltage-protect', '网侧过压保护动作，电压超过额定值 10%，保护装置已动作。', '处理中', '华南运维中心', '查明电网电压异常原因，待电网恢复正常后重合闸。'),
('ALM-20260407-003', 'PV-001', '2026-04-07 07:40:00', 'major', '重要', '汇流箱 HB-03', 'string-imbalance', '支路 #4 电流与均值偏差超过 15%，疑似组件衰减或遮挡。', '待确认', '华南运维中心', '安排现场 IV 曲线测试，对比历史数据判断衰减程度。'),
('ALM-20260407-004', 'PV-006', '2026-04-07 07:22:00', 'major', '重要', '逆变器 INV-02', 'cooling-fan-fault', '散热风机故障，机箱温度已达 78℃，功率已自动降额至 70%。', '处理中', '西南运维团队', '更换散热风机，降额运行期间加强温度监控。'),
('ALM-20260407-005', 'PV-004', '2026-04-07 07:05:00', 'major', '重要', '直流汇流箱 HB-12', 'fuse-blown', '第 6 路保险丝熔断，该支路 8 块组件停止发电。', '未处理', '华东运维组', '更换同规格保险丝，并排查该支路短路原因。'),
('ALM-20260407-006', 'PV-005', '2026-04-07 06:50:00', 'minor', '一般', '气象站 WS-01', 'sensor-offset', '辐照度传感器零点漂移 +12 W/m²，影响发电预测精度。', '待确认', '平台值班', '安排定期标定，更新传感器补偿参数。'),
('ALM-20260407-007', 'PV-001', '2026-04-07 06:35:00', 'minor', '一般', '采集器 DTU-03', 'data-gap', '数据上报中断 5 分钟，已自动重连恢复，缺口数据已补采。', '已处理', '平台值班', '监控网络稳定性，如频繁出现考虑升级通信模块。'),
('ALM-20260407-008', 'PV-002', '2026-04-07 06:18:00', 'minor', '一般', '逆变器 INV-11', 'grid-frequency-deviation', '电网频率偏差告警，频率超出 49.8~50.2Hz 范围持续 30 秒。', '已处理', '华南运维中心', '记录频率异常时段，上报电网调度，暂无需手动干预。'),
('ALM-20260407-009', 'PV-003', '2026-04-07 06:00:00', 'hint', '提示', '监控平台', 'daily-report', '今日凌晨日报生成完成，昨日发电量 65.8 MWh，同比提升 3.2%。', '已处理', '平台值班', '无需处理，仅作记录。'),
('ALM-20260407-010', 'PV-006', '2026-04-07 05:45:00', 'hint', '提示', '气象服务', 'weather-alert', '成都地区今日云量预报偏高，预计午前发电量较计划低 8%~12%。', '待确认', '调度中心', '调整日前基线计划，预留下调裕量以应对云量遮挡。');

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
WITH RECURSIVE
  slot_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM slot_range WHERE X < 95)
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
        ) AS SIGNED
    ) AS irradiance,
    ROUND(
        w.temperature + SIN(slot_range.X / 6.0) * 2.1,
        1
    ) AS temperature
FROM pm_station s
JOIN pm_weather_snapshot w ON w.resource_unit_id = s.resource_unit_id
CROSS JOIN slot_range;

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
WITH RECURSIVE
  slot_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM slot_range WHERE X < 5)
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
CROSS JOIN slot_range;

INSERT INTO sa_company (id, name, region, sort_index) VALUES
('com-001', '深圳市悦美颐华物业管理有限公司', '华南', 1),
('com-002', '武汉恒实物业管理有限公司', '华中', 2),
('com-003', '合肥盛景新能源有限公司', '华东', 3),
('com-004', '天津港储能科技有限公司', '华北', 4),
('com-005', '成都西部新能源有限公司', '西南', 5);

INSERT INTO sa_station (
    id, company_id, name, capacity_kwp, status, data_quality, grid_status,
    grid_status_label, commission_date, address, load_base_kw, sort_index
) VALUES
('SZ-001', 'com-001', '深圳湾科创园 A 站', 1800, 'normal', 'good', 'grid-connected', '并网', DATE '2024-06-15', '深圳湾科创园 A1 栋屋顶', 2100, 1),
('SZ-002', 'com-001', '南山智造中心 B 站', 1400, 'normal', 'good', 'grid-connected', '并网', DATE '2024-07-08', '南山智造中心 B 区连廊屋顶', 1600, 2),
('SZ-003', 'com-001', '前海冷站屋顶 C 站', 1200, 'warning', 'missing', 'grid-connected', '并网', DATE '2024-08-22', '前海冷站 C 区屋顶', 1500, 3),
('SZ-004', 'com-001', '生态园 D 站', 1000, 'normal', 'good', 'grid-connected', '并网', DATE '2024-09-12', '生态园 D 区停车棚', 1200, 4),
('DG-001', 'com-001', '松山湖智造园 A 站', 1900, 'normal', 'good', 'grid-connected', '并网', DATE '2024-05-18', '松山湖智造园 A 区厂房屋顶', 2200, 5),
('DG-002', 'com-001', '松山湖制造园 B 站', 1600, 'warning', 'missing', 'grid-connected', '并网', DATE '2024-10-03', '松山湖制造园 B 区屋顶', 1800, 6),
('WH-001', 'com-002', '武汉物流园 A 站', 1600, 'fault', 'missing', 'grid-connected', '并网', DATE '2024-04-18', '武汉物流园 A 区仓库屋顶', 1900, 7),
('WH-002', 'com-002', '武汉仓储园 B 站', 1400, 'warning', 'missing', 'grid-connected', '并网', DATE '2024-11-06', '武汉仓储园 B 区屋顶', 1700, 8),
('WH-003', 'com-002', '鄂州协同园 C 站', 1200, 'normal', 'good', 'grid-connected', '并网', DATE '2025-01-16', '鄂州协同园 C 区连廊', 1400, 9),
('HF-001', 'com-003', '合肥研发中心 A 站', 1000, 'maintenance', 'missing', 'grid-connected', '并网', DATE '2024-06-28', '合肥研发中心 A 区车棚', 1100, 10),
('HF-002', 'com-003', '合肥实证基地 B 站', 900, 'normal', 'good', 'grid-connected', '并网', DATE '2024-12-09', '合肥实证基地 B 区实验楼', 1000, 11),
('TJ-001', 'com-004', '天津港储能园 A 站', 1200, 'offline', 'missing', 'grid-connected', '并网', DATE '2024-05-25', '天津港储能园 A 区屋顶', 1400, 12),
('TJ-002', 'com-004', '天津港仓储园 B 站', 1000, 'offline', 'missing', 'grid-connected', '并网', DATE '2024-07-18', '天津港仓储园 B 区仓顶', 1200, 13),
('CD-001', 'com-005', '成都西部基地 A 站', 1700, 'normal', 'good', 'grid-connected', '并网', DATE '2024-02-28', '成都西部基地 A 区生产楼', 2000, 14),
('CD-002', 'com-005', '成都连廊产区 B 站', 1500, 'normal', 'good', 'grid-connected', '并网', DATE '2024-10-15', '成都连廊产区 B 区连廊', 1700, 15),
('CD-003', 'com-005', '成都仓储园 C 站', 1300, 'warning', 'missing', 'grid-connected', '并网', DATE '2025-02-20', '成都仓储园 C 区仓顶', 1500, 16);

INSERT INTO sa_station_curve_15m (
    station_id, biz_date, time_slot, load_kw, pv_output_kw,
    forecast_day_ahead_kw, forecast_ultra_short_kw
)
WITH RECURSIVE
  slot_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM slot_range WHERE X < 95)
SELECT
    s.id,
    DATE '2026-03-30',
    slot_range.X,
    ROUND(
        s.load_base_kw * (
            0.42
            + 0.32 * EXP(-0.5 * POWER((((slot_range.X * 15.0) / 60.0) - 10.0) / 1.6, 2))
            + 0.28 * EXP(-0.5 * POWER((((slot_range.X * 15.0) / 60.0) - 15.2) / 2.1, 2))
            + CASE
                WHEN ((slot_range.X * 15.0) / 60.0) < 6 OR ((slot_range.X * 15.0) / 60.0) > 22 THEN -0.14
                ELSE 0
            END
            + SIN((s.sort_index * 11.0 + slot_range.X) / 7.0) * 0.03
        ),
        1
    ) AS load_kw,
    ROUND(
        GREATEST(
            0,
            s.capacity_kwp
            * GREATEST(0, SIN((((slot_range.X * 15.0) / 60.0) - 6.0) / 12.0 * PI()))
            * CASE s.status
                WHEN 'normal' THEN 0.78
                WHEN 'warning' THEN 0.70
                WHEN 'fault' THEN 0.42
                WHEN 'maintenance' THEN 0.58
                WHEN 'offline' THEN 0.05
                ELSE 0.74
            END
            * (0.96 + COS((s.sort_index * 5.0 + slot_range.X) / 8.0) * 0.04)
        ),
        1
    ) AS pv_output_kw,
    ROUND(
        GREATEST(
            0,
            s.capacity_kwp
            * GREATEST(0, SIN((((slot_range.X * 15.0) / 60.0) - 6.0) / 12.0 * PI()))
            * CASE s.status
                WHEN 'offline' THEN 0.16
                WHEN 'fault' THEN 0.58
                WHEN 'maintenance' THEN 0.66
                WHEN 'warning' THEN 0.74
                ELSE 0.81
            END
            * (0.98 + SIN((s.sort_index * 3.0 + slot_range.X) / 10.0) * 0.03)
        ),
        1
    ) AS forecast_day_ahead_kw,
    ROUND(
        GREATEST(
            0,
            s.capacity_kwp
            * GREATEST(0, SIN((((slot_range.X * 15.0) / 60.0) - 6.0) / 12.0 * PI()))
            * CASE s.status
                WHEN 'offline' THEN 0.10
                WHEN 'fault' THEN 0.48
                WHEN 'maintenance' THEN 0.61
                WHEN 'warning' THEN 0.72
                ELSE 0.79
            END
            * (0.99 + COS((s.sort_index * 4.0 + slot_range.X) / 11.0) * 0.02)
        ),
        1
    ) AS forecast_ultra_short_kw
FROM sa_station s
CROSS JOIN slot_range;

INSERT INTO sa_inverter (
    id, station_id, name, sort_index, rated_power_kw, status, model, manufacturer,
    serial_no, firmware_version, install_date, mppt_channels, dc_input_voltage_v,
    ac_output_voltage_v, grid_frequency_hz, module_temperature_c, ambient_temperature_c,
    string_count, panels_per_string
)
WITH RECURSIVE
  seq_range AS (SELECT 1 AS X UNION ALL SELECT X + 1 FROM seq_range WHERE X < 3)
SELECT
    CONCAT(s.id, '-INV-', RIGHT(CONCAT('00', CAST(seq_range.X AS CHAR)), 2)),
    s.id,
    CONCAT('INV-', RIGHT(CONCAT('00', CAST(seq_range.X AS CHAR)), 2), ' (', CAST(CAST(ROUND(s.capacity_kwp / CASE WHEN s.capacity_kwp > 1500 THEN 3 ELSE 2 END, 0) AS SIGNED) AS CHAR), 'kW)'),
    seq_range.X,
    ROUND(s.capacity_kwp / CASE WHEN s.capacity_kwp > 1500 THEN 3 ELSE 2 END, 0),
    CASE
        WHEN s.status = 'offline' THEN 'offline'
        WHEN s.status = 'fault' AND seq_range.X = CASE WHEN s.capacity_kwp > 1500 THEN 3 ELSE 2 END THEN 'fault'
        WHEN s.status = 'warning' AND seq_range.X = CASE WHEN s.capacity_kwp > 1500 THEN 2 ELSE 1 END THEN 'warning'
        WHEN s.status = 'maintenance' AND seq_range.X = 1 THEN 'maintenance'
        ELSE 'normal'
    END,
    CONCAT(CONCAT('SUN2000-', CAST(CAST(ROUND(s.capacity_kwp / CASE WHEN s.capacity_kwp > 1500 THEN 3 ELSE 2 END, 0) AS SIGNED) AS CHAR)), 'KTL'),
    CASE WHEN MOD(s.sort_index, 2) = 0 THEN '华为' ELSE '阳光电源' END,
    CONCAT('SN', REPLACE(s.id, '-', ''), RIGHT(CONCAT('00', CAST(seq_range.X AS CHAR)), 2)),
    'V300R001C10SPC230',
    s.commission_date,
    CASE WHEN s.capacity_kwp > 1500 THEN 6 ELSE 4 END,
    ROUND(580 + s.sort_index * 4 + seq_range.X * 12, 1),
    380,
    50,
    ROUND(36 + MOD(s.sort_index, 6) * 1.6 + seq_range.X * 0.8, 1),
    ROUND(23 + MOD(s.sort_index, 4) * 1.5, 1),
    CASE WHEN s.capacity_kwp > 1500 THEN 12 + seq_range.X ELSE 8 + seq_range.X END,
    20 + MOD(s.sort_index + seq_range.X, 6)
FROM sa_station s
CROSS JOIN seq_range
WHERE seq_range.X <= CASE WHEN s.capacity_kwp > 1500 THEN 3 ELSE 2 END;

INSERT INTO sa_station_strategy (
    station_id, name, type, status, start_time, end_time, target_power_kw, estimated_revenue_cny
) VALUES
('SZ-001', '华南园区调峰策略', '需求响应', '执行中', TIMESTAMP '2026-03-26 06:00:00', TIMESTAMP '2026-03-26 22:00:00', 1200, 580),
('SZ-002', '南山区域调频策略', '调频辅助', '执行中', TIMESTAMP '2026-03-26 07:00:00', TIMESTAMP '2026-03-26 20:00:00', 1000, 420),
('SZ-003', '前海分布式限出策略', '电网约束', '告警中', TIMESTAMP '2026-03-26 08:00:00', TIMESTAMP '2026-03-26 18:00:00', 800, 310),
('SZ-004', '生态园自发自用策略', '自发自用', '执行中', TIMESTAMP '2026-03-26 06:30:00', TIMESTAMP '2026-03-26 19:00:00', 700, 260),
('DG-001', '松山湖调峰策略', '需求响应', '执行中', TIMESTAMP '2026-03-26 06:00:00', TIMESTAMP '2026-03-26 21:00:00', 1500, 720),
('DG-002', '松山湖制造园限出策略', '电网约束', '告警中', TIMESTAMP '2026-03-26 07:30:00', TIMESTAMP '2026-03-26 19:30:00', 1100, 480),
('WH-001', '武汉物流园故障降额策略', '故障响应', '异常', TIMESTAMP '2026-03-26 05:00:00', TIMESTAMP '2026-03-26 23:00:00', 600, 180),
('WH-002', '武汉仓储园限出策略', '电网约束', '告警中', TIMESTAMP '2026-03-26 07:00:00', TIMESTAMP '2026-03-26 20:00:00', 1000, 380),
('WH-003', '鄂州协同园调峰策略', '需求响应', '执行中', TIMESTAMP '2026-03-26 06:30:00', TIMESTAMP '2026-03-26 21:30:00', 900, 410),
('HF-001', '合肥研发中心检修策略', '检修窗口', '检修中', TIMESTAMP '2026-03-26 06:00:00', TIMESTAMP '2026-03-26 18:00:00', 400, 120),
('HF-002', '合肥实证基地调峰策略', '需求响应', '执行中', TIMESTAMP '2026-03-26 07:00:00', TIMESTAMP '2026-03-26 20:00:00', 650, 280),
('TJ-001', '天津港储能园离线恢复策略', '故障响应', '离线', TIMESTAMP '2026-03-26 00:00:00', TIMESTAMP '2026-03-26 23:59:00', 0, 0),
('TJ-002', '天津港仓储园离线恢复策略', '故障响应', '离线', TIMESTAMP '2026-03-26 00:00:00', TIMESTAMP '2026-03-26 23:59:00', 0, 0),
('CD-001', '成都西部基地调峰策略', '需求响应', '执行中', TIMESTAMP '2026-03-26 06:00:00', TIMESTAMP '2026-03-26 22:00:00', 1400, 680),
('CD-002', '成都连廊产区调频策略', '调频辅助', '执行中', TIMESTAMP '2026-03-26 07:00:00', TIMESTAMP '2026-03-26 21:00:00', 1200, 550),
('CD-003', '成都仓储园限出策略', '电网约束', '告警中', TIMESTAMP '2026-03-26 08:00:00', TIMESTAMP '2026-03-26 19:00:00', 900, 340);

INSERT INTO sa_inverter_alarm (id, inverter_id, event_time, type, level, description, status) VALUES
('SA-ALM-001', 'SZ-003-INV-02', TIMESTAMP '2026-03-30 12:18:00', 'MPPT偏低', '告警', 'MPPT 跟踪效率低于阈值 92%', '未处理'),
('SA-ALM-002', 'DG-002-INV-02', TIMESTAMP '2026-03-30 13:05:00', '组串失衡', '告警', 'B2 屋顶区域组串电流偏差持续放大', '待确认'),
('SA-ALM-003', 'WH-001-INV-03', TIMESTAMP '2026-03-30 13:42:00', '过温保护', '严重', '模块温度超过 75℃，设备已自动降额', '未处理'),
('SA-ALM-004', 'WH-002-INV-01', TIMESTAMP '2026-03-30 11:56:00', '通信异常', '告警', '与采集器通信超时超过 30 秒', '已确认'),
('SA-ALM-005', 'TJ-001-INV-01', TIMESTAMP '2026-03-30 10:14:00', '离线告警', '故障', '园区通信链路中断，逆变器处于离线状态', '未处理'),
('SA-ALM-006', 'CD-003-INV-01', TIMESTAMP '2026-03-30 14:06:00', '绝缘阻抗低', '告警', '绝缘阻抗低于 500kΩ，建议安排现场排查', '待确认');
INSERT INTO fc_model (id, name, type, version, provider, description, status) VALUES
('FC-M-DA-001', 'Forecast Day-Ahead Seed Model', 'day-ahead', 'v1.3.0', 'PVMS Seed', 'Development day-ahead forecast metadata model', 'active'),
('FC-M-US-001', 'Forecast Ultra-Short Seed Model', 'ultra-short', 'v2.1.0', 'PVMS Seed', 'Development ultra-short forecast metadata model', 'active');

INSERT INTO fc_station_model_binding (station_id, day_ahead_model_id, ultra_short_model_id, confidence_level)
SELECT
    id,
    'FC-M-DA-001',
    'FC-M-US-001',
    ROUND(88 + MOD(sort_index, 6) * 1.5, 1)
FROM sa_station;

INSERT INTO fc_prediction_series_15m (
    station_id, biz_date, time_slot, forecast_type, predicted_power_kw, upper_bound_kw, lower_bound_kw, scenario_tag
)
SELECT
    c.station_id,
    c.biz_date,
    c.time_slot,
    'day-ahead',
    ROUND(GREATEST(c.forecast_day_ahead_kw * (0.995 + SIN((c.time_slot + s.sort_index) / 11.0) * 0.012), 0), 1),
    ROUND(GREATEST(c.forecast_day_ahead_kw * 1.10 + 18, 0), 1),
    ROUND(GREATEST(c.forecast_day_ahead_kw * 0.90 - 16, 0), 1),
    'baseline'
FROM sa_station_curve_15m c
JOIN sa_station s ON s.id = c.station_id
WHERE c.biz_date = DATE '2026-03-30';

INSERT INTO fc_prediction_series_15m (
    station_id, biz_date, time_slot, forecast_type, predicted_power_kw, upper_bound_kw, lower_bound_kw, scenario_tag
)
SELECT
    c.station_id,
    c.biz_date,
    c.time_slot,
    'ultra-short',
    ROUND(GREATEST(c.forecast_ultra_short_kw * (1.002 + COS((c.time_slot + s.sort_index) / 13.0) * 0.008), 0), 1),
    ROUND(GREATEST(c.forecast_ultra_short_kw * 1.06 + 10, 0), 1),
    ROUND(GREATEST(c.forecast_ultra_short_kw * 0.94 - 8, 0), 1),
    'rolling'
FROM sa_station_curve_15m c
JOIN sa_station s ON s.id = c.station_id
WHERE c.biz_date = DATE '2026-03-30';

INSERT INTO fc_error_sample (
    station_id, biz_date, hour_slot, forecast_type, error_kw, qualified
)
WITH RECURSIVE
  day_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM day_range WHERE X < 29),
  hour_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM hour_range WHERE X < 23)
SELECT
    s.id,
    CAST(DATE_SUB(DATE '2026-03-30', INTERVAL day_range.X DAY) AS DATE),
    hour_range.X,
    'day-ahead',
    ROUND(
        SIN((s.sort_index * 5.0 + day_range.X * 2.0 + hour_range.X) / 4.5) * 118
        + COS((hour_range.X + 1.0) / 3.1) * 22,
        1
    ),
    ABS(
        ROUND(
            SIN((s.sort_index * 5.0 + day_range.X * 2.0 + hour_range.X) / 4.5) * 118
            + COS((hour_range.X + 1.0) / 3.1) * 22,
            1
        )
    ) <= 120
FROM sa_station s
CROSS JOIN day_range
CROSS JOIN hour_range;

INSERT INTO fc_error_sample (
    station_id, biz_date, hour_slot, forecast_type, error_kw, qualified
)
WITH RECURSIVE
  day_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM day_range WHERE X < 29),
  hour_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM hour_range WHERE X < 23)
SELECT
    s.id,
    CAST(DATE_SUB(DATE '2026-03-30', INTERVAL day_range.X DAY) AS DATE),
    hour_range.X,
    'ultra-short',
    ROUND(
        SIN((s.sort_index * 4.0 + day_range.X * 3.0 + hour_range.X) / 5.2) * 72
        + COS((hour_range.X + 2.0) / 2.7) * 15,
        1
    ),
    ABS(
        ROUND(
            SIN((s.sort_index * 4.0 + day_range.X * 3.0 + hour_range.X) / 5.2) * 72
            + COS((hour_range.X + 2.0) / 2.7) * 15,
            1
        )
    ) <= 80
FROM sa_station s
CROSS JOIN day_range
CROSS JOIN hour_range;

INSERT INTO fc_monthly_accuracy_snapshot (
    station_id, month_key, forecast_type, mae_kw, rmse_kw, accuracy_pct
)
WITH RECURSIVE
  month_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM month_range WHERE X < 5)
SELECT
    s.id,
    DATE_FORMAT(DATE_SUB(DATE '2026-03-01', INTERVAL month_range.X MONTH), '%Y-%m'),
    'day-ahead',
    ROUND(138 - month_range.X * 7 + MOD(s.sort_index, 4) * 6, 1),
    ROUND(182 - month_range.X * 8 + MOD(s.sort_index, 5) * 7, 1),
    ROUND(89.6 + month_range.X * 0.8 - MOD(s.sort_index, 4) * 0.35, 1)
FROM sa_station s
CROSS JOIN month_range;

INSERT INTO fc_monthly_accuracy_snapshot (
    station_id, month_key, forecast_type, mae_kw, rmse_kw, accuracy_pct
)
WITH RECURSIVE
  month_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM month_range WHERE X < 5)
SELECT
    s.id,
    DATE_FORMAT(DATE_SUB(DATE '2026-03-01', INTERVAL month_range.X MONTH), '%Y-%m'),
    'ultra-short',
    ROUND(92 - month_range.X * 5 + MOD(s.sort_index, 4) * 4, 1),
    ROUND(128 - month_range.X * 6 + MOD(s.sort_index, 5) * 5, 1),
    ROUND(92.4 + month_range.X * 0.7 - MOD(s.sort_index, 4) * 0.28, 1)
FROM sa_station s
CROSS JOIN month_range;

INSERT INTO fc_adjustable_window (
    station_id, biz_date, window_order, start_slot, end_slot, window_status
)
SELECT
    s.id,
    DATE '2026-03-30',
    1,
    24,
    43,
    CASE
        WHEN s.status IN ('offline', 'fault') THEN 'unavailable'
        WHEN s.status = 'maintenance' THEN 'dispatching'
        ELSE 'available'
    END
FROM sa_station s;

INSERT INTO fc_adjustable_window (
    station_id, biz_date, window_order, start_slot, end_slot, window_status
)
SELECT
    s.id,
    DATE '2026-03-30',
    2,
    44,
    59,
    CASE
        WHEN s.status = 'offline' THEN 'unavailable'
        ELSE 'dispatching'
    END
FROM sa_station s;

INSERT INTO fc_adjustable_window (
    station_id, biz_date, window_order, start_slot, end_slot, window_status
)
SELECT
    s.id,
    DATE '2026-03-30',
    3,
    60,
    75,
    CASE
        WHEN s.status IN ('offline', 'fault') THEN 'unavailable'
        ELSE 'available'
    END
FROM sa_station s;

INSERT INTO sg_strategy (
    id, station_id, company_id, name, type, status, mode, target_power_kw,
    start_time, end_time, version_no, remark, created_at, updated_at
) VALUES
('SG-001', 'SZ-001', 'com-001', '深圳湾削峰策略 A', 'peak-shaving', 'executing', 'single', 1200, TIMESTAMP '2026-03-30 08:00:00', TIMESTAMP '2026-03-30 18:00:00', 3, '园区工作日削峰', TIMESTAMP '2026-03-26 09:00:00', TIMESTAMP '2026-03-30 08:10:00'),
('SG-002', 'SZ-002', 'com-001', '南山需求响应策略 A', 'demand-response', 'pending', 'single', 950, TIMESTAMP '2026-03-30 09:00:00', TIMESTAMP '2026-03-30 17:00:00', 2, '待市场指令下发', TIMESTAMP '2026-03-26 10:00:00', TIMESTAMP '2026-03-30 07:40:00'),
('SG-003', 'SZ-003', 'com-001', '前海电网约束策略 A', 'grid-constraint', 'draft', 'single', 780, TIMESTAMP '2026-03-30 10:00:00', TIMESTAMP '2026-03-30 16:00:00', 1, '草稿待校核', TIMESTAMP '2026-03-27 11:00:00', TIMESTAMP '2026-03-29 16:30:00'),
('SG-004', 'SZ-004', 'com-001', '生态园调频策略 A', 'frequency-regulation', 'completed', 'single', 680, TIMESTAMP '2026-03-29 08:00:00', TIMESTAMP '2026-03-29 15:00:00', 2, '已完成归档', TIMESTAMP '2026-03-24 10:00:00', TIMESTAMP '2026-03-29 15:30:00'),
('SG-005', 'DG-001', 'com-001', '松山湖削峰策略 A', 'peak-shaving', 'executing', 'single', 1450, TIMESTAMP '2026-03-30 08:30:00', TIMESTAMP '2026-03-30 18:30:00', 4, '制造园削峰执行中', TIMESTAMP '2026-03-25 09:30:00', TIMESTAMP '2026-03-30 08:35:00'),
('SG-006', 'DG-002', 'com-001', '松山湖需求响应策略 A', 'demand-response', 'terminated', 'single', 1080, TIMESTAMP '2026-03-29 09:00:00', TIMESTAMP '2026-03-29 17:00:00', 2, '因设备告警终止', TIMESTAMP '2026-03-25 12:00:00', TIMESTAMP '2026-03-29 11:40:00'),
('SG-007', 'WH-001', 'com-002', '武汉物流园约束策略 A', 'grid-constraint', 'executing', 'single', 920, TIMESTAMP '2026-03-30 07:30:00', TIMESTAMP '2026-03-30 16:30:00', 3, '物流园电网约束', TIMESTAMP '2026-03-26 08:40:00', TIMESTAMP '2026-03-30 07:35:00'),
('SG-008', 'WH-002', 'com-002', '武汉仓储园调频策略 A', 'frequency-regulation', 'pending', 'batch', 860, TIMESTAMP '2026-03-30 08:00:00', TIMESTAMP '2026-03-30 14:00:00', 1, '待批量提交流程', TIMESTAMP '2026-03-27 09:20:00', TIMESTAMP '2026-03-30 07:00:00'),
('SG-009', 'WH-003', 'com-002', '鄂州协同园削峰策略 A', 'peak-shaving', 'draft', 'single', 720, TIMESTAMP '2026-03-30 11:00:00', TIMESTAMP '2026-03-30 19:00:00', 1, '新建待模拟', TIMESTAMP '2026-03-28 13:00:00', TIMESTAMP '2026-03-30 06:20:00'),
('SG-010', 'HF-001', 'com-003', '合肥研发中心需求响应策略 A', 'demand-response', 'completed', 'single', 660, TIMESTAMP '2026-03-28 09:00:00', TIMESTAMP '2026-03-28 17:00:00', 3, '历史完成策略', TIMESTAMP '2026-03-23 09:00:00', TIMESTAMP '2026-03-28 18:10:00'),
('SG-011', 'HF-002', 'com-003', '合肥实证基地约束策略 A', 'grid-constraint', 'executing', 'single', 610, TIMESTAMP '2026-03-30 08:00:00', TIMESTAMP '2026-03-30 17:00:00', 2, '实证基地出力约束', TIMESTAMP '2026-03-26 10:30:00', TIMESTAMP '2026-03-30 08:00:00'),
('SG-012', 'TJ-001', 'com-004', '天津港储能园调频策略 A', 'frequency-regulation', 'terminated', 'single', 880, TIMESTAMP '2026-03-29 10:00:00', TIMESTAMP '2026-03-29 18:00:00', 2, '站点离线终止', TIMESTAMP '2026-03-26 11:00:00', TIMESTAMP '2026-03-29 10:45:00'),
('SG-013', 'TJ-002', 'com-004', '天津港仓储园削峰策略 A', 'peak-shaving', 'pending', 'batch', 760, TIMESTAMP '2026-03-30 09:00:00', TIMESTAMP '2026-03-30 17:00:00', 1, '待确认站点恢复', TIMESTAMP '2026-03-28 10:00:00', TIMESTAMP '2026-03-30 07:10:00'),
('SG-014', 'CD-001', 'com-005', '成都西部基地需求响应策略 A', 'demand-response', 'executing', 'single', 1320, TIMESTAMP '2026-03-30 08:00:00', TIMESTAMP '2026-03-30 18:00:00', 3, '西部基地主策略', TIMESTAMP '2026-03-25 08:30:00', TIMESTAMP '2026-03-30 08:05:00'),
('SG-015', 'CD-002', 'com-005', '成都连廊产区约束策略 A', 'grid-constraint', 'draft', 'single', 1020, TIMESTAMP '2026-03-30 10:00:00', TIMESTAMP '2026-03-30 20:00:00', 1, '待确认约束边界', TIMESTAMP '2026-03-28 15:00:00', TIMESTAMP '2026-03-30 05:50:00'),
('SG-016', 'CD-003', 'com-005', '成都仓储园调频策略 A', 'frequency-regulation', 'completed', 'single', 960, TIMESTAMP '2026-03-29 09:00:00', TIMESTAMP '2026-03-29 16:30:00', 2, '已完成结算', TIMESTAMP '2026-03-24 14:00:00', TIMESTAMP '2026-03-29 17:10:00'),
('SG-017', 'SZ-001', 'com-001', '深圳湾需求响应策略 B', 'demand-response', 'executing', 'batch', 980, TIMESTAMP '2026-03-30 07:00:00', TIMESTAMP '2026-03-30 12:00:00', 2, '上午快速响应', TIMESTAMP '2026-03-27 08:00:00', TIMESTAMP '2026-03-30 07:05:00'),
('SG-018', 'DG-001', 'com-001', '松山湖电网约束策略 B', 'grid-constraint', 'pending', 'single', 1180, TIMESTAMP '2026-03-30 11:00:00', TIMESTAMP '2026-03-30 18:00:00', 1, '待确认园区负荷', TIMESTAMP '2026-03-29 09:10:00', TIMESTAMP '2026-03-30 07:20:00'),
('SG-019', 'WH-002', 'com-002', '武汉仓储园削峰策略 B', 'peak-shaving', 'draft', 'single', 790, TIMESTAMP '2026-03-30 12:00:00', TIMESTAMP '2026-03-30 19:00:00', 1, '待二次模拟', TIMESTAMP '2026-03-29 11:00:00', TIMESTAMP '2026-03-30 06:30:00'),
('SG-020', 'HF-002', 'com-003', '合肥实证基地调频策略 B', 'frequency-regulation', 'executing', 'batch', 560, TIMESTAMP '2026-03-30 08:30:00', TIMESTAMP '2026-03-30 15:30:00', 2, '实证基地调频执行中', TIMESTAMP '2026-03-26 16:00:00', TIMESTAMP '2026-03-30 08:32:00'),
('SG-021', 'CD-001', 'com-005', '成都西部基地削峰策略 B', 'peak-shaving', 'terminated', 'single', 1260, TIMESTAMP '2026-03-29 08:30:00', TIMESTAMP '2026-03-29 18:30:00', 2, '人工终止', TIMESTAMP '2026-03-27 09:40:00', TIMESTAMP '2026-03-29 13:20:00'),
('SG-022', 'SZ-004', 'com-001', '生态园电网约束策略 B', 'grid-constraint', 'completed', 'single', 640, TIMESTAMP '2026-03-28 09:00:00', TIMESTAMP '2026-03-28 17:00:00', 2, '已执行完成', TIMESTAMP '2026-03-24 09:20:00', TIMESTAMP '2026-03-28 17:05:00'),
('SG-023', 'DG-002', 'com-001', '松山湖制造园调频策略 B', 'frequency-regulation', 'pending', 'batch', 980, TIMESTAMP '2026-03-30 13:00:00', TIMESTAMP '2026-03-30 20:00:00', 1, '待告警恢复后执行', TIMESTAMP '2026-03-29 15:00:00', TIMESTAMP '2026-03-30 07:50:00'),
('SG-024', 'CD-003', 'com-005', '成都仓储园需求响应策略 B', 'demand-response', 'executing', 'single', 920, TIMESTAMP '2026-03-30 09:00:00', TIMESTAMP '2026-03-30 17:30:00', 2, '仓储园快速响应', TIMESTAMP '2026-03-27 10:30:00', TIMESTAMP '2026-03-30 09:02:00');

INSERT INTO sg_strategy_period (
    strategy_id, period_order, start_slot, end_slot, action_type, target_ratio
)
SELECT
    id,
    1,
    CASE
        WHEN type IN ('peak-shaving', 'frequency-regulation') THEN 32
        ELSE 28
    END,
    CASE
        WHEN type IN ('peak-shaving', 'frequency-regulation') THEN 43
        ELSE 39
    END,
    CASE
        WHEN type = 'peak-shaving' THEN 'peak-cut'
        WHEN type = 'demand-response' THEN 'response-up'
        WHEN type = 'grid-constraint' THEN 'limit-output'
        ELSE 'regulate-up'
    END,
    CASE
        WHEN type IN ('peak-shaving', 'demand-response') THEN 0.72
        WHEN type = 'grid-constraint' THEN 0.66
        ELSE 0.58
    END
FROM sg_strategy;

INSERT INTO sg_strategy_period (
    strategy_id, period_order, start_slot, end_slot, action_type, target_ratio
)
SELECT
    id,
    2,
    CASE
        WHEN type IN ('peak-shaving', 'frequency-regulation') THEN 52
        ELSE 56
    END,
    CASE
        WHEN type IN ('peak-shaving', 'frequency-regulation') THEN 67
        ELSE 71
    END,
    CASE
        WHEN type = 'peak-shaving' THEN 'peak-hold'
        WHEN type = 'demand-response' THEN 'response-hold'
        WHEN type = 'grid-constraint' THEN 'limit-hold'
        ELSE 'regulate-down'
    END,
    CASE
        WHEN type IN ('peak-shaving', 'demand-response') THEN 0.54
        WHEN type = 'grid-constraint' THEN 0.48
        ELSE 0.44
    END
FROM sg_strategy;

INSERT INTO sg_execution_log (
    id, strategy_id, event_time, action, result, deviation_rate_pct, operator_name
)
SELECT
    CONCAT(id, '-LOG-01'),
    id,
    DATE_ADD(created_at, INTERVAL 1 HOUR),
    'created',
    'success',
    ROUND(ABS(MOD(target_power_kw, 90)) / 10.0, 1),
    'system-seed'
FROM sg_strategy;

INSERT INTO sg_execution_log (
    id, strategy_id, event_time, action, result, deviation_rate_pct, operator_name
)
SELECT
    CONCAT(id, '-LOG-02'),
    id,
    DATE_ADD(created_at, INTERVAL 2 HOUR),
    CASE
        WHEN status IN ('pending', 'executing', 'completed') THEN 'submitted'
        WHEN status = 'terminated' THEN 'terminated'
        ELSE 'simulated'
    END,
    CASE
        WHEN status = 'terminated' THEN 'warning'
        ELSE 'success'
    END,
    ROUND(ABS(MOD(target_power_kw, 130)) / 12.0, 1),
    'dispatcher'
FROM sg_strategy;

INSERT INTO sg_execution_log (
    id, strategy_id, event_time, action, result, deviation_rate_pct, operator_name
)
SELECT
    CONCAT(id, '-LOG-03'),
    id,
    updated_at,
    CASE
        WHEN status = 'executing' THEN 'executing'
        WHEN status = 'completed' THEN 'completed'
        WHEN status = 'terminated' THEN 'terminated'
        WHEN status = 'pending' THEN 'queued'
        ELSE 'draft-saved'
    END,
    CASE
        WHEN status = 'terminated' THEN 'warning'
        ELSE 'success'
    END,
    ROUND(ABS(MOD(target_power_kw, 170)) / 13.0, 1),
    'operator-01'
FROM sg_strategy;

INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 1, 0, 23, 'valley', 0.25 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 2, 24, 31, 'flat', 0.65 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 3, 32, 43, 'peak', 1.11 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 4, 44, 51, 'flat', 0.65 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 5, 52, 59, 'peak', 1.11 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 6, 60, 67, 'flat', 0.65 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 7, 68, 75, 'peak', 1.11 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 8, 76, 87, 'flat', 0.65 FROM sa_station;
INSERT INTO sg_price_period (
    station_id, period_order, start_slot, end_slot, price_type, price_cny_per_kwh
)
SELECT id, 9, 88, 95, 'valley', 0.25 FROM sa_station;

INSERT INTO sg_revenue_daily (
    strategy_id, biz_date, estimated_revenue_cny, actual_revenue_cny,
    peak_saving_cny, response_reward_cny, penalty_cny
)
WITH RECURSIVE
  day_range AS (SELECT 0 AS X UNION ALL SELECT X + 1 FROM day_range WHERE X < 13)
SELECT
    s.id,
    CAST(DATE_SUB(DATE '2026-03-30', INTERVAL day_range.X DAY) AS DATE),
    ROUND(
        180
        + MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 7) * 36
        + CASE s.type
            WHEN 'peak-shaving' THEN 96
            WHEN 'demand-response' THEN 78
            WHEN 'grid-constraint' THEN 58
            ELSE 66
          END
        - day_range.X * 3.2,
        2
    ),
    ROUND(
        170
        + MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 7) * 34
        + CASE s.type
            WHEN 'peak-shaving' THEN 88
            WHEN 'demand-response' THEN 72
            WHEN 'grid-constraint' THEN 52
            ELSE 61
          END
        - day_range.X * 3.4,
        2
    ),
    ROUND(
        60
        + CASE s.type
            WHEN 'peak-shaving' THEN 70
            WHEN 'demand-response' THEN 24
            WHEN 'grid-constraint' THEN 18
            ELSE 30
          END
        + MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 5) * 8
        - day_range.X * 1.4,
        2
    ),
    ROUND(
        20
        + CASE s.type
            WHEN 'demand-response' THEN 88
            WHEN 'frequency-regulation' THEN 64
            ELSE 22
          END
        + MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 4) * 6
        - day_range.X * 0.9,
        2
    ),
    ROUND(
        CASE
            WHEN s.status = 'terminated' THEN 28 + day_range.X * 0.6
            WHEN s.status = 'draft' THEN 6 + day_range.X * 0.2
            ELSE 10 + day_range.X * 0.3
        END,
        2
    )
FROM sg_strategy s
CROSS JOIN day_range;

INSERT INTO sg_strategy_snapshot (
    strategy_id, last_simulated_revenue_cny, confidence_low_cny,
    confidence_high_cny, success_probability_pct
)
SELECT
    s.id,
    ROUND(
        220
        + MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 8) * 40
        + CASE s.type
            WHEN 'peak-shaving' THEN 120
            WHEN 'demand-response' THEN 90
            WHEN 'grid-constraint' THEN 70
            ELSE 82
          END,
        2
    ),
    ROUND(
        180
        + MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 8) * 34
        + CASE s.type
            WHEN 'peak-shaving' THEN 96
            WHEN 'demand-response' THEN 72
            WHEN 'grid-constraint' THEN 56
            ELSE 64
          END,
        2
    ),
    ROUND(
        260
        + MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 8) * 48
        + CASE s.type
            WHEN 'peak-shaving' THEN 136
            WHEN 'demand-response' THEN 106
            WHEN 'grid-constraint' THEN 82
            ELSE 94
          END,
        2
    ),
    ROUND(
        CASE s.status
            WHEN 'executing' THEN 93.0
            WHEN 'completed' THEN 95.0
            WHEN 'pending' THEN 88.0
            WHEN 'terminated' THEN 72.0
            ELSE 84.0
        END - MOD(CAST(SUBSTRING(s.id, 4, 3) AS SIGNED), 4) * 1.5,
        1
    )
FROM sg_strategy s;
