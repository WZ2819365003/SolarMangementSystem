function buildSuccess(data) {
  return {
    code: 0,
    message: 'success',
    data
  }
}

const statusMeta = {
  normal: { label: '正常', color: '#67C23A' },
  warning: { label: '告警', color: '#E6A23C' },
  fault: { label: '故障', color: '#F56C6C' },
  maintenance: { label: '检修', color: '#409EFF' },
  offline: { label: '离线', color: '#909399' }
}

const stationStatusFactor = {
  normal: 1,
  warning: 0.88,
  fault: 0.52,
  maintenance: 0.72,
  offline: 0.18
}

const resourceUnits = [
  {
    id: 'RU-001',
    anchorStationId: 'PV-001',
    name: '深圳湾科创园聚合单元',
    region: '华南区域',
    city: '深圳',
    status: 'normal',
    clusterRadiusKm: 8,
    stationCount: 4,
    dispatchableCapacityMw: 5.4,
    realtimePowerMw: 4.18,
    todayEnergyMwh: 27.6,
    upRegulationMw: 0.92,
    downRegulationMw: 1.44,
    onlineRate: 98.4,
    forecastAccuracy: 95.6,
    forecastDeviationRate: -2.1,
    dispatchMode: '日前基线 + 实时修正',
    strategyLabel: '深圳南山与前海相邻工商业屋顶光伏共享同城天气剖面，仅按权重分摊出力。',
    weather: {
      weather: '多云',
      cloudiness: '中云',
      temperature: 29,
      irradiance: 746,
      humidity: 61,
      windSpeed: 3.1,
      conclusion: '午后辐照条件稳定，适合维持上调空间。'
    },
    stations: [
      { id: 'SZ-001', name: '深圳湾科创园 A 站', capacityMw: 1.8, weight: 0.34, status: 'normal', onlineRate: 99.1, alarmCount: 0 },
      { id: 'SZ-002', name: '南山智造中心 B 站', capacityMw: 1.4, weight: 0.26, status: 'normal', onlineRate: 98.7, alarmCount: 0 },
      { id: 'SZ-003', name: '前海冷站屋顶 C 站', capacityMw: 1.2, weight: 0.22, status: 'warning', onlineRate: 96.8, alarmCount: 1 },
      { id: 'SZ-004', name: '生态园 D 站', capacityMw: 1.0, weight: 0.18, status: 'normal', onlineRate: 98.9, alarmCount: 0 }
    ],
    alarm: {
      total: 2,
      critical: 0,
      major: 1,
      minor: 1,
      latestTitle: '前海冷站屋顶 C 站采集延迟，连续两个时段偏差超阈。',
      latestTime: '2026-03-23 14:18:00'
    }
  },
  {
    id: 'RU-002',
    anchorStationId: 'PV-002',
    name: '松山湖智造聚合单元',
    region: '华南区域',
    city: '东莞',
    status: 'warning',
    clusterRadiusKm: 10,
    stationCount: 4,
    dispatchableCapacityMw: 6.1,
    realtimePowerMw: 3.98,
    todayEnergyMwh: 24.3,
    upRegulationMw: 0.66,
    downRegulationMw: 1.26,
    onlineRate: 95.2,
    forecastAccuracy: 92.4,
    forecastDeviationRate: 4.1,
    dispatchMode: '滚动预测 + 偏差压缩',
    strategyLabel: '松山湖制造园区资源共享同一片天气趋势，按容量权重分摊实际出力。',
    weather: {
      weather: '晴转多云',
      cloudiness: '低云',
      temperature: 28,
      irradiance: 714,
      humidity: 57,
      windSpeed: 2.8,
      conclusion: '16:00 后云量抬升，预计实际出力略低于预测。'
    },
    stations: [
      { id: 'DG-001', name: '松山湖智造园 A 站', capacityMw: 1.9, weight: 0.31, status: 'normal', onlineRate: 97.5, alarmCount: 0 },
      { id: 'DG-002', name: '松山湖制造园 B 站', capacityMw: 1.6, weight: 0.27, status: 'warning', onlineRate: 94.2, alarmCount: 1 },
      { id: 'DG-003', name: '松山湖配套园 C 站', capacityMw: 1.4, weight: 0.23, status: 'warning', onlineRate: 93.6, alarmCount: 1 },
      { id: 'DG-004', name: '松山湖仓储园 D 站', capacityMw: 1.2, weight: 0.19, status: 'normal', onlineRate: 95.8, alarmCount: 0 }
    ],
    alarm: {
      total: 4,
      critical: 0,
      major: 2,
      minor: 2,
      latestTitle: '松山湖制造园 B 站在 13:45 后出现预测偏差持续放大。',
      latestTime: '2026-03-23 14:06:00'
    }
  },
  {
    id: 'RU-003',
    anchorStationId: 'PV-003',
    name: '武汉物流基地聚合单元',
    region: '华中区域',
    city: '武汉',
    status: 'fault',
    clusterRadiusKm: 14,
    stationCount: 3,
    dispatchableCapacityMw: 4.2,
    realtimePowerMw: 1.82,
    todayEnergyMwh: 13.4,
    upRegulationMw: 0.22,
    downRegulationMw: 0.74,
    onlineRate: 86.2,
    forecastAccuracy: 84.3,
    forecastDeviationRate: -9.6,
    dispatchMode: '偏差压降',
    strategyLabel: '武汉物流园与仓储园站点共享天气剖面，但故障站拖低聚合单元可调能力。',
    weather: {
      weather: '阴',
      cloudiness: '厚云',
      temperature: 23,
      irradiance: 402,
      humidity: 72,
      windSpeed: 2.1,
      conclusion: '云层持续偏厚，建议限制上调并优先压缩偏差风险。'
    },
    stations: [
      { id: 'WH-001', name: '武汉物流园 A 站', capacityMw: 1.6, weight: 0.41, status: 'fault', onlineRate: 82.4, alarmCount: 3 },
      { id: 'WH-002', name: '武汉仓储园 B 站', capacityMw: 1.4, weight: 0.34, status: 'warning', onlineRate: 88.9, alarmCount: 1 },
      { id: 'WH-003', name: '鄂州协同园 C 站', capacityMw: 1.2, weight: 0.25, status: 'normal', onlineRate: 91.6, alarmCount: 1 }
    ],
    alarm: {
      total: 6,
      critical: 2,
      major: 2,
      minor: 2,
      latestTitle: '武汉物流园 A 站逆变器簇故障，导致资源单元响应不完整。',
      latestTime: '2026-03-23 14:02:00'
    }
  },
  {
    id: 'RU-004',
    anchorStationId: 'PV-004',
    name: '合肥研发协同聚合单元',
    region: '华东区域',
    city: '合肥',
    status: 'maintenance',
    clusterRadiusKm: 9,
    stationCount: 3,
    dispatchableCapacityMw: 2.6,
    realtimePowerMw: 1.18,
    todayEnergyMwh: 8.2,
    upRegulationMw: 0.34,
    downRegulationMw: 0.58,
    onlineRate: 91.5,
    forecastAccuracy: 89.1,
    forecastDeviationRate: -3.8,
    dispatchMode: '检修窗口 + 保守基线',
    strategyLabel: '合肥研发中心邻近站群共享天气剖面，检修期以保守调度为主。',
    weather: {
      weather: '小雨',
      cloudiness: '中云',
      temperature: 21,
      irradiance: 318,
      humidity: 76,
      windSpeed: 3.5,
      conclusion: '检修时段叠加阵雨，建议降低上调预期并保持基线稳定。'
    },
    stations: [
      { id: 'HF-001', name: '合肥研发中心 A 站', capacityMw: 1.0, weight: 0.39, status: 'maintenance', onlineRate: 90.8, alarmCount: 1 },
      { id: 'HF-002', name: '合肥实证基地 B 站', capacityMw: 0.9, weight: 0.34, status: 'normal', onlineRate: 93.1, alarmCount: 0 },
      { id: 'HF-003', name: '合肥车棚屋顶 C 站', capacityMw: 0.7, weight: 0.27, status: 'maintenance', onlineRate: 90.6, alarmCount: 1 }
    ],
    alarm: {
      total: 2,
      critical: 0,
      major: 1,
      minor: 1,
      latestTitle: '合肥研发中心 A 站处于检修窗口，可调空间暂时下调。',
      latestTime: '2026-03-23 13:32:00'
    }
  },
  {
    id: 'RU-005',
    anchorStationId: 'PV-005',
    name: '天津港储能园聚合单元',
    region: '华北区域',
    city: '天津',
    status: 'offline',
    clusterRadiusKm: 11,
    stationCount: 3,
    dispatchableCapacityMw: 3.1,
    realtimePowerMw: 0.42,
    todayEnergyMwh: 2.6,
    upRegulationMw: 0.08,
    downRegulationMw: 0.46,
    onlineRate: 78.9,
    forecastAccuracy: 80.4,
    forecastDeviationRate: -12.8,
    dispatchMode: '退出调度 + 边界保电',
    strategyLabel: '天津港储能园区部分站点因通信中断脱网，当前按离线资源单元独立管理。',
    weather: {
      weather: '大风',
      cloudiness: '少云',
      temperature: 16,
      irradiance: 458,
      humidity: 48,
      windSpeed: 7.3,
      conclusion: '大风和通信中断叠加，当前不建议承接新增调度指令。'
    },
    stations: [
      { id: 'TJ-001', name: '天津港储能园 A 站', capacityMw: 1.2, weight: 0.38, status: 'offline', onlineRate: 75.2, alarmCount: 2 },
      { id: 'TJ-002', name: '天津港仓储园 B 站', capacityMw: 1.0, weight: 0.34, status: 'offline', onlineRate: 79.1, alarmCount: 1 },
      { id: 'TJ-003', name: '天津港堆场园 C 站', capacityMw: 0.9, weight: 0.28, status: 'warning', onlineRate: 82.6, alarmCount: 1 }
    ],
    alarm: {
      total: 4,
      critical: 1,
      major: 2,
      minor: 1,
      latestTitle: '天津港储能园 A 站通信中断超过 15 分钟，当前已切换至边界保电模式。',
      latestTime: '2026-03-23 14:12:00'
    }
  },
  {
    id: 'RU-006',
    anchorStationId: 'PV-006',
    name: '成都西部基地聚合单元',
    region: '西南区域',
    city: '成都',
    status: 'normal',
    clusterRadiusKm: 13,
    stationCount: 4,
    dispatchableCapacityMw: 5.6,
    realtimePowerMw: 4.06,
    todayEnergyMwh: 29.1,
    upRegulationMw: 1.02,
    downRegulationMw: 1.38,
    onlineRate: 97.8,
    forecastAccuracy: 95.1,
    forecastDeviationRate: -1.7,
    dispatchMode: '日前基线 + 实时优化',
    strategyLabel: '成都西部基地相邻园区光伏站群共享天气建模，当前可稳定提供上下调能力。',
    weather: {
      weather: '晴',
      cloudiness: '少云',
      temperature: 24,
      irradiance: 648,
      humidity: 57,
      windSpeed: 2.4,
      conclusion: '辐照度和温度均处于稳定区间，当前具备较好调度弹性。'
    },
    stations: [
      { id: 'CD-001', name: '成都西部基地 A 站', capacityMw: 1.7, weight: 0.31, status: 'normal', onlineRate: 98.2, alarmCount: 0 },
      { id: 'CD-002', name: '成都连廊产区 B 站', capacityMw: 1.5, weight: 0.28, status: 'normal', onlineRate: 97.5, alarmCount: 0 },
      { id: 'CD-003', name: '成都仓储园 C 站', capacityMw: 1.3, weight: 0.23, status: 'warning', onlineRate: 95.9, alarmCount: 1 },
      { id: 'CD-004', name: '成都配套园 D 站', capacityMw: 1.1, weight: 0.18, status: 'normal', onlineRate: 98.4, alarmCount: 0 }
    ],
    alarm: {
      total: 1,
      critical: 0,
      major: 0,
      minor: 1,
      latestTitle: '成都仓储园 C 站传感器上报偶发延迟，已自动补采。',
      latestTime: '2026-03-23 13:26:00'
    }
  }
]

function round(value, digits = 1) {
  return Number(Number(value).toFixed(digits))
}

function findResourceUnit(resourceUnitId) {
  return resourceUnits.find(item => item.id === resourceUnitId) || resourceUnits[0]
}

function findResourceUnitByAnchorStationId(stationId) {
  return resourceUnits.find(item => item.anchorStationId === stationId) || null
}

function buildRegionOptions() {
  return [{ label: '全部区域', value: '' }].concat(
    Array.from(new Set(resourceUnits.map(item => item.region))).map(item => ({
      label: item,
      value: item
    }))
  )
}

function buildResourceUnitMetaItem(item) {
  return {
    id: item.id,
    anchorStationId: item.anchorStationId,
    name: item.name,
    region: item.region,
    city: item.city,
    status: item.status,
    statusLabel: statusMeta[item.status].label,
    statusColor: statusMeta[item.status].color,
    clusterRadiusKm: item.clusterRadiusKm,
    stationCount: item.stationCount,
    dispatchMode: item.dispatchMode,
    strategyLabel: item.strategyLabel,
    dispatchableCapacityMw: item.dispatchableCapacityMw
  }
}

function createTimeAxis(granularity) {
  const stepMap = {
    '15m': 15,
    '30m': 30,
    '60m': 60
  }
  const stepMinutes = stepMap[granularity] || 15
  const points = []

  for (let minute = 0; minute < 24 * 60; minute += stepMinutes) {
    const hour = String(Math.floor(minute / 60)).padStart(2, '0')
    const minuteText = String(minute % 60).padStart(2, '0')
    points.push(`${hour}:${minuteText}`)
  }

  return points
}

function buildSharedCurve(unit, granularity) {
  const axis = createTimeAxis(granularity)
  const statusFactor = stationStatusFactor[unit.status] || 1
  const cloudPenalty =
    unit.weather.cloudiness === '厚云'
      ? 0.55
      : unit.weather.cloudiness === '中云'
        ? 0.82
        : 0.94

  const actual = []
  const forecast = []
  const baseline = []
  const irradiance = []
  const temperature = []
  const cloudiness = []

  axis.forEach((time, index) => {
    const decimalHour =
      index * (granularity === '60m' ? 1 : granularity === '30m' ? 0.5 : 0.25)
    const solarWindow = Math.max(0, Math.sin(((decimalHour - 6) / 12) * Math.PI))
    const irradianceValue = round(
      unit.weather.irradiance * solarWindow + Math.sin(index / 3) * 28,
      0
    )
    const forecastValue = round(
      unit.dispatchableCapacityMw * solarWindow * 0.96 * cloudPenalty + 0.14,
      2
    )
    const actualValue = round(
      forecastValue * statusFactor + Math.cos(index / 4) * 0.08,
      2
    )
    const baselineValue = round(forecastValue * 0.98 + 0.1, 2)

    actual.push(Math.max(0, actualValue))
    forecast.push(Math.max(0, forecastValue))
    baseline.push(Math.max(0, baselineValue))
    irradiance.push(Math.max(0, irradianceValue))
    temperature.push(round(unit.weather.temperature + Math.sin(index / 6) * 2.4, 1))
    cloudiness.push(unit.weather.cloudiness)
  })

  return {
    axis,
    actual,
    forecast,
    baseline,
    irradiance,
    temperature,
    cloudiness,
    powerUnit: 'MW',
    weatherUnit: 'W/m²'
  }
}

function buildMemberStations(unit) {
  return unit.stations.map(item => {
    const factor = stationStatusFactor[item.status] || 1
    const realtimePowerMw = round(unit.realtimePowerMw * item.weight * factor, 2)
    return {
      id: item.id,
      name: item.name,
      capacityMw: item.capacityMw,
      status: item.status,
      statusLabel: statusMeta[item.status].label,
      realtimePowerMw,
      onlineRate: item.onlineRate,
      alarmCount: item.alarmCount,
      weight: item.weight,
      adjustableCapacityMw: round(item.capacityMw * 0.35 * factor, 2),
      weatherText: `${unit.weather.weather} / ${unit.weather.temperature}°C`
    }
  })
}

function buildOverviewPayload(unit) {
  return {
    info: buildResourceUnitMetaItem(unit),
    kpis: [
      { key: 'realtime', title: '实时总出力', value: unit.realtimePowerMw, unit: 'MW', helper: '当前资源单元聚合总出力', icon: 'el-icon-lightning', accent: 'blue' },
      { key: 'today-energy', title: '当日累计电量', value: unit.todayEnergyMwh, unit: 'MWh', helper: '当前日内累计电量', icon: 'el-icon-data-analysis', accent: 'emerald' },
      { key: 'up', title: '上调可调能力', value: unit.upRegulationMw, unit: 'MW', helper: '当前可承接上调空间', icon: 'el-icon-top', accent: 'teal' },
      { key: 'down', title: '下调可调能力', value: unit.downRegulationMw, unit: 'MW', helper: '当前可承接下调空间', icon: 'el-icon-bottom', accent: 'orange' },
      { key: 'online', title: '在线率', value: unit.onlineRate, unit: '%', helper: '按成员电站在线质量聚合', icon: 'el-icon-success', accent: 'teal' },
      { key: 'accuracy', title: '预测准确率', value: unit.forecastAccuracy, unit: '%', helper: '同区域天气下的资源单元预测命中率', icon: 'el-icon-s-marketing', accent: 'blue' }
    ],
    memberStations: buildMemberStations(unit),
    weatherBrief: {
      ...unit.weather
    },
    alarmBrief: {
      ...unit.alarm
    },
    summaryTable: [
      {
        time: '09:00',
        realtimePowerMw: round(unit.realtimePowerMw * 0.74, 2),
        dispatchableCapacityMw: round(unit.dispatchableCapacityMw * 0.86, 2),
        onlineRate: round(unit.onlineRate - 0.6, 1),
        forecastDeviationRate: round(unit.forecastDeviationRate + 1.1, 1),
        weatherText: unit.weather.conclusion
      },
      {
        time: '12:00',
        realtimePowerMw: round(unit.realtimePowerMw * 0.96, 2),
        dispatchableCapacityMw: round(unit.dispatchableCapacityMw * 0.94, 2),
        onlineRate: unit.onlineRate,
        forecastDeviationRate: unit.forecastDeviationRate,
        weatherText: unit.weather.conclusion
      },
      {
        time: '15:00',
        realtimePowerMw: round(unit.realtimePowerMw, 2),
        dispatchableCapacityMw: round(unit.dispatchableCapacityMw, 2),
        onlineRate: unit.onlineRate,
        forecastDeviationRate: round(unit.forecastDeviationRate - 0.5, 1),
        weatherText: unit.weather.conclusion
      }
    ]
  }
}

function buildOutputPayload(unit, granularity) {
  const curve = buildSharedCurve(unit, granularity)
  const memberStations = buildMemberStations(unit)

  return {
    summary: [
      { key: 'peak', title: '峰值出力', value: round(Math.max.apply(null, curve.actual), 2), unit: 'MW', helper: '按当前粒度计算的峰值', icon: 'el-icon-data-line', accent: 'blue' },
      { key: 'deviation', title: '预测偏差率', value: unit.forecastDeviationRate, unit: '%', helper: '资源单元整体偏差', icon: 'el-icon-warning-outline', accent: 'orange' },
      { key: 'irradiance', title: '峰值辐照度', value: Math.max.apply(null, curve.irradiance), unit: 'W/m²', helper: '同区域共享天气趋势', icon: 'el-icon-s-opportunity', accent: 'teal' },
      { key: 'granularity', title: '当前粒度', value: granularity.replace('m', ''), unit: 'min', helper: '切换粒度后曲线和表格联动', icon: 'el-icon-time', accent: 'emerald' }
    ],
    curve,
    weatherTrend: {
      axis: curve.axis,
      series: [
        { name: '辐照度', data: curve.irradiance, type: 'bar', yAxisIndex: 1, color: 'rgba(130, 208, 255, 0.35)' },
        { name: '温度', data: curve.temperature, color: '#00b578' }
      ]
    },
    contributionRanking: memberStations
      .slice()
      .sort((prev, next) => next.realtimePowerMw - prev.realtimePowerMw)
      .map(item => ({
        id: item.id,
        name: item.name,
        realtimePowerMw: item.realtimePowerMw,
        shareRate: round(item.weight * 100, 0),
        weatherText: item.weatherText
      })),
    table: curve.axis.slice(0, 8).map((time, index) => ({
      time,
      actualPowerMw: curve.actual[index],
      forecastPowerMw: curve.forecast[index],
      baselinePowerMw: curve.baseline[index],
      deviationRate: round(
        ((curve.actual[index] - curve.forecast[index]) / (curve.forecast[index] || 1)) * 100,
        1
      ),
      irradiance: curve.irradiance[index],
      temperature: curve.temperature[index],
      cloudiness: curve.cloudiness[index],
      maxUpCapacityKw: round(unit.upRegulationMw * 1000 * (0.8 + Math.random() * 0.4), 0),
      maxDownCapacityKw: round(unit.downRegulationMw * 1000 * (0.7 + Math.random() * 0.3), 0)
    }))
  }
}

function buildDispatchPayload(unit) {
  const faultMode = unit.status === 'fault' || unit.status === 'offline'

  return {
    summary: [
      { key: 'issued', title: '当日下发指令', value: faultMode ? 9 : 14, unit: '条', helper: '按资源单元口径统计', icon: 'el-icon-s-promotion', accent: 'blue' },
      { key: 'success', title: '成功执行', value: faultMode ? 6 : 13, unit: '条', helper: '成功回执数', icon: 'el-icon-success', accent: 'emerald' },
      { key: 'rate', title: '执行成功率', value: faultMode ? 66.7 : 92.9, unit: '%', helper: '按资源单元整体执行率计算', icon: 'el-icon-finished', accent: 'teal' },
      { key: 'response', title: '平均响应时长', value: faultMode ? 182 : 96, unit: 's', helper: '按指令闭环时间计算', icon: 'el-icon-time', accent: 'orange' },
      { key: 'adj-up', title: '今日可调上限', value: round(unit.upRegulationMw * 1000, 0), unit: 'kW', helper: '储能可放电 + 光伏可上调', icon: 'el-icon-top', accent: 'emerald' },
      { key: 'adj-used', title: '今日已调用量', value: round(unit.upRegulationMw * 1000 * (faultMode ? 0.3 : 0.65), 0), unit: 'kW', helper: '累计调度消耗可调资源', icon: 'el-icon-data-board', accent: 'blue' }
    ],
    executionTrend: {
      axis: ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00'],
      issued: faultMode ? [1, 1, 2, 1, 2, 2] : [2, 2, 3, 2, 2, 3],
      success: faultMode ? [1, 1, 1, 1, 1, 1] : [2, 2, 2, 2, 2, 3],
      responseSeconds: faultMode ? [168, 172, 184, 178, 196, 182] : [92, 88, 96, 102, 94, 96]
    },
    riskHints: [
      {
        title: `${unit.name} 当前天气结论`,
        description: unit.weather.conclusion,
        level: faultMode ? '高风险' : '低风险',
        tagType: faultMode ? 'danger' : 'success'
      },
      {
        title: '成员电站距离约束',
        description: `当前站群最远距离 ${unit.clusterRadiusKm}km，满足同区位共享天气建模；超出该范围建议拆分资源单元。`,
        level: '提示',
        tagType: 'info'
      }
    ],
    records: [
      {
        issuedAt: '2026-03-23 09:15:00',
        commandType: '上调',
        targetPowerMw: round(unit.realtimePowerMw + 0.24, 2),
        actualPowerMw: round(unit.realtimePowerMw + (faultMode ? 0.08 : 0.2), 2),
        responseSeconds: faultMode ? 168 : 92,
        status: faultMode ? '处理中' : '已完成',
        deviationReason: faultMode ? '故障站拖低执行完整率' : '执行正常'
      },
      {
        issuedAt: '2026-03-23 12:00:00',
        commandType: '下调',
        targetPowerMw: round(unit.realtimePowerMw - 0.36, 2),
        actualPowerMw: round(unit.realtimePowerMw - (faultMode ? 0.18 : 0.34), 2),
        responseSeconds: faultMode ? 196 : 104,
        status: faultMode ? '偏差超阈' : '已完成',
        deviationReason: faultMode ? '天气与设备故障叠加导致响应不足' : '执行在安全区间'
      }
    ]
  }
}

function buildWeatherPayload(unit) {
  return {
    summary: [
      { key: 'weather', title: '当前天气', value: unit.weather.weather, unit: '', helper: '与资源单元成员站共享', icon: 'el-icon-cloudy', accent: 'blue' },
      { key: 'temp', title: '当前温度', value: unit.weather.temperature, unit: '°C', helper: '同城实时气象观测', icon: 'el-icon-sunny', accent: 'orange' },
      { key: 'irradiance', title: '当前辐照度', value: unit.weather.irradiance, unit: 'W/m²', helper: '用于光伏出力研判', icon: 'el-icon-s-opportunity', accent: 'teal' },
      { key: 'wind', title: '风速', value: unit.weather.windSpeed, unit: 'm/s', helper: '当前风场条件', icon: 'el-icon-time', accent: 'emerald' }
    ],
    trend: {
      axis: ['今 12:00', '今 15:00', '今 18:00', '今 21:00', '明 09:00', '明 12:00', '明 15:00', '明 18:00'],
      series: [
        { name: '辐照度', data: [unit.weather.irradiance - 40, unit.weather.irradiance, 260, 20, 540, 720, 688, 214], type: 'bar', yAxisIndex: 1, color: 'rgba(130, 208, 255, 0.35)' },
        { name: '温度', data: [unit.weather.temperature, unit.weather.temperature + 1, unit.weather.temperature - 2, unit.weather.temperature - 5, unit.weather.temperature - 2, unit.weather.temperature + 1, unit.weather.temperature, unit.weather.temperature - 3], color: '#00b578' },
        { name: '风速', data: [unit.weather.windSpeed, unit.weather.windSpeed + 0.2, unit.weather.windSpeed + 0.4, unit.weather.windSpeed + 0.1, unit.weather.windSpeed + 0.3, unit.weather.windSpeed + 0.5, unit.weather.windSpeed + 0.2, unit.weather.windSpeed], color: '#f59b23' }
      ]
    },
    impactTable: [
      {
        timeRange: '今天 12:00-15:00',
        weather: unit.weather.weather,
        irradianceRange: `${unit.weather.irradiance - 40}-${unit.weather.irradiance} W/m²`,
        temperatureRange: `${unit.weather.temperature}-${unit.weather.temperature + 1}°C`,
        windSpeedRange: `${unit.weather.windSpeed}-${round(unit.weather.windSpeed + 0.2, 1)} m/s`,
        outputLevel: '高',
        suggestion: '维持当前调度基线，保留上调能力。'
      },
      {
        timeRange: '今天 15:00-18:00',
        weather: unit.weather.cloudiness,
        irradianceRange: '220-420 W/m²',
        temperatureRange: `${unit.weather.temperature - 2}-${unit.weather.temperature}°C`,
        windSpeedRange: `${unit.weather.windSpeed}-${round(unit.weather.windSpeed + 0.4, 1)} m/s`,
        outputLevel: unit.status === 'fault' || unit.status === 'offline' ? '低' : '中',
        suggestion:
          unit.status === 'fault' || unit.status === 'offline'
            ? '建议压缩上调预期并关注设备恢复。'
            : '注意云量抬升带来的短时波动。'
      }
    ]
  }
}

// ---------------------------------------------------------------------------
// Load table view — all stations with grid interaction data
// ---------------------------------------------------------------------------
function buildLoadTablePayload() {
  // Inline station data for load table (mirrors station-archive stations)
  var allStations = [
    { id: 'SZ-001', name: '深圳湾科创园 A 站', capacityKwp: 1800, status: 'normal', loadBaseKw: 2100 },
    { id: 'SZ-002', name: '南山智造中心 B 站', capacityKwp: 1400, status: 'normal', loadBaseKw: 1600 },
    { id: 'SZ-003', name: '前海冷站屋顶 C 站', capacityKwp: 1200, status: 'warning', loadBaseKw: 1500 },
    { id: 'SZ-004', name: '生态园 D 站', capacityKwp: 1000, status: 'normal', loadBaseKw: 1200 },
    { id: 'DG-001', name: '松山湖智造园 A 站', capacityKwp: 1900, status: 'normal', loadBaseKw: 2200 },
    { id: 'DG-002', name: '松山湖制造园 B 站', capacityKwp: 1600, status: 'warning', loadBaseKw: 1800 },
    { id: 'WH-001', name: '武汉物流园 A 站', capacityKwp: 1600, status: 'fault', loadBaseKw: 1900 },
    { id: 'WH-002', name: '武汉仓储园 B 站', capacityKwp: 1400, status: 'warning', loadBaseKw: 1700 },
    { id: 'WH-003', name: '鄂州协同园 C 站', capacityKwp: 1200, status: 'normal', loadBaseKw: 1400 },
    { id: 'HF-001', name: '合肥研发中心 A 站', capacityKwp: 1000, status: 'maintenance', loadBaseKw: 1100 },
    { id: 'HF-002', name: '合肥实证基地 B 站', capacityKwp: 900, status: 'normal', loadBaseKw: 1000 },
    { id: 'TJ-001', name: '天津港储能园 A 站', capacityKwp: 1200, status: 'offline', loadBaseKw: 1400 },
    { id: 'TJ-002', name: '天津港仓储园 B 站', capacityKwp: 1000, status: 'offline', loadBaseKw: 1200 },
    { id: 'CD-001', name: '成都西部基地 A 站', capacityKwp: 1700, status: 'normal', loadBaseKw: 2000 },
    { id: 'CD-002', name: '成都连廊产区 B 站', capacityKwp: 1500, status: 'normal', loadBaseKw: 1700 },
    { id: 'CD-003', name: '成都仓储园 C 站', capacityKwp: 1300, status: 'warning', loadBaseKw: 1500 }
  ]
  var seed = function (s, min, max) {
    var x = Math.sin(s * 9301 + 49297) * 233280
    var r = x - Math.floor(x)
    return min + r * (max - min)
  }
  var rd = function (v) { return Number(Number(v).toFixed(1)) }
  var sf = { normal: 1, warning: 0.88, fault: 0.52, maintenance: 0.72, offline: 0.18 }
  var solarFactor = function (m) { var h = m / 60; return Math.max(0, Math.sin(((h - 6) / 12) * Math.PI)) }
  var loadFactor = function (m) {
    var h = m / 60
    var base = 0.4
    var mp = Math.max(0, 0.35 * Math.exp(-0.5 * Math.pow((h - 10) / 1.5, 2)))
    var ap = Math.max(0, 0.30 * Math.exp(-0.5 * Math.pow((h - 15.5) / 2, 2)))
    var nd = (h < 6 || h > 22) ? -0.15 : 0
    return Math.max(0.1, base + mp + ap + nd)
  }

  var totalLoadKw = 0, totalPvKw = 0, onlineCount = 0
  var stationRows = allStations.map(function (s, idx) {
    var factor = sf[s.status] || 1
    var pvOut = rd(s.capacityKwp * 0.65 * factor)
    var adjKw = rd(s.loadBaseKw - pvOut)
    var rampRate = rd(seed(idx * 100, 15, 45))
    var realtimePowerKw = rd(s.capacityKwp * seed(idx * 200, 0.55, 0.85) * factor)
    totalLoadKw += s.loadBaseKw
    totalPvKw += pvOut
    if (s.status !== 'offline') onlineCount++

    // Build grid interaction curve for modal
    var times = []
    var loadSeries = []
    var pvSeries = []
    var gridSeries = []
    for (var i = 0; i < 96; i++) {
      var minuteOfDay = i * 15
      var hh = String(Math.floor(minuteOfDay / 60)).padStart(2, '0')
      var mm = String(minuteOfDay % 60).padStart(2, '0')
      times.push(hh + ':' + mm)
      var lfv = loadFactor(minuteOfDay)
      var sfv = solarFactor(minuteOfDay)
      var nl = seed(idx * 1000 + i, -0.03, 0.03)
      var np = seed(idx * 2000 + i, -0.04, 0.04)
      var ld = rd(s.loadBaseKw * (lfv + nl))
      var pv = rd(Math.max(0, s.capacityKwp * sfv * (0.78 + np) * factor))
      var grid = rd(ld - pv)
      loadSeries.push(ld)
      pvSeries.push(pv)
      gridSeries.push(grid)
    }

    return {
      id: s.id,
      name: s.name,
      capacityKwp: s.capacityKwp,
      realtimePowerKw: realtimePowerKw,
      loadKw: s.loadBaseKw,
      adjustableKw: adjKw,
      maxRampRate: rampRate,
      status: s.status,
      gridInteraction: {
        times: times,
        series: [
          { name: '负荷', data: loadSeries },
          { name: '光伏出力', data: pvSeries },
          { name: '电网交互(负荷-出力)', data: gridSeries, area: true }
        ]
      }
    }
  })

  var totalAdjMw = rd((totalLoadKw - totalPvKw) / 1000)
  var avgRamp = rd(stationRows.reduce(function (a, b) { return a + b.maxRampRate }, 0) / stationRows.length)

  return {
    summary: {
      totalLoadMw: rd(totalLoadKw / 1000),
      totalPvOutputMw: rd(totalPvKw / 1000),
      totalAdjustableMw: totalAdjMw,
      avgRampRate: avgRamp,
      stationCount: allStations.length,
      onlineCount: onlineCount
    },
    stations: stationRows
  }
}

const stationMonitoringMockHandlers = {
  '/pvms/production-monitor/meta': () =>
    buildSuccess({
      defaultResourceUnitId: resourceUnits[0].id,
      regionOptions: buildRegionOptions(),
      resourceUnits: resourceUnits.map(buildResourceUnitMetaItem)
    }),
  '/pvms/production-monitor/overview': ({ params = {} } = {}) =>
    buildSuccess(buildOverviewPayload(findResourceUnit(params.resourceUnitId))),
  '/pvms/production-monitor/output': ({ params = {} } = {}) =>
    buildSuccess(
      buildOutputPayload(
        findResourceUnit(params.resourceUnitId),
        params.granularity || '15m'
      )
    ),
  '/pvms/production-monitor/dispatch': ({ params = {} } = {}) =>
    buildSuccess(buildDispatchPayload(findResourceUnit(params.resourceUnitId))),
  '/pvms/production-monitor/weather': ({ params = {} } = {}) =>
    buildSuccess(buildWeatherPayload(findResourceUnit(params.resourceUnitId))),
  '/pvms/production-monitor/load': () =>
    buildSuccess(buildLoadTablePayload())
}

export {
  buildDispatchPayload,
  buildOverviewPayload,
  buildOutputPayload,
  buildWeatherPayload,
  findResourceUnitByAnchorStationId,
  resourceUnits,
  stationMonitoringMockHandlers
}
