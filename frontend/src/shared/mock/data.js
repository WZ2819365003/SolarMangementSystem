import {
  findResourceUnitByAnchorStationId,
  stationMonitoringMockHandlers
} from '@/shared/mock/station-monitoring'
import {
  stationArchiveMockHandlers
} from '@/shared/mock/station-archive'
import { forecastMockHandlers } from '@/shared/mock/forecast'

function buildSuccess(data) {
  return {
    code: 0,
    message: 'success',
    data
  }
}

const dashboardStatusMeta = {
  normal: {
    label: '正常',
    color: '#67C23A'
  },
  warning: {
    label: '告警',
    color: '#E6A23C'
  },
  fault: {
    label: '故障',
    color: '#F56C6C'
  },
  maintenance: {
    label: '检修',
    color: '#409EFF'
  },
  offline: {
    label: '离线',
    color: '#909399'
  }
}

const rawDashboardStations = [
  {
    id: 'PV-001',
    name: '深圳湾科技园光伏电站',
    region: '华南园区',
    longitude: 113.9489,
    latitude: 22.5408,
    status: 'normal',
    capacityKwp: 2500,
    realtimePowerKw: 1823,
    todayEnergyKwh: 8432,
    todayRevenueCny: 3372.8,
    pr: 84.1,
    equivalentHours: 3.37,
    healthGrade: 'A',
    availability: 99.2,
    address: '深圳湾科技生态园 8 栋屋面'
  },
  {
    id: 'PV-002',
    name: '松山湖智造园光伏电站',
    region: '华南园区',
    longitude: 113.8854,
    latitude: 22.9192,
    status: 'warning',
    capacityKwp: 6200,
    realtimePowerKw: 3986,
    todayEnergyKwh: 20840,
    todayRevenueCny: 8336,
    pr: 82.3,
    equivalentHours: 3.36,
    healthGrade: 'A-',
    availability: 98.1,
    address: '松山湖智造园 A 区厂房屋顶'
  },
  {
    id: 'PV-003',
    name: '武汉物流基地光伏电站',
    region: '华中园区',
    longitude: 114.3066,
    latitude: 30.5928,
    status: 'fault',
    capacityKwp: 4800,
    realtimePowerKw: 2156,
    todayEnergyKwh: 13542,
    todayRevenueCny: 5416.8,
    pr: 76.8,
    equivalentHours: 2.82,
    healthGrade: 'B',
    availability: 95.7,
    address: '武汉东西湖物流园 2 号仓屋顶'
  },
  {
    id: 'PV-004',
    name: '合肥研发中心光伏电站',
    region: '华东园区',
    longitude: 117.2272,
    latitude: 31.8206,
    status: 'maintenance',
    capacityKwp: 1800,
    realtimePowerKw: 684,
    todayEnergyKwh: 2958,
    todayRevenueCny: 1183.2,
    pr: 71.9,
    equivalentHours: 1.64,
    healthGrade: 'B+',
    availability: 92.4,
    address: '合肥研发中心 C 区车棚'
  },
  {
    id: 'PV-005',
    name: '天津港储能园光伏电站',
    region: '华北园区',
    longitude: 117.3616,
    latitude: 39.3434,
    status: 'offline',
    capacityKwp: 3200,
    realtimePowerKw: 0,
    todayEnergyKwh: 1180,
    todayRevenueCny: 472,
    pr: 52.4,
    equivalentHours: 0.37,
    healthGrade: 'C',
    availability: 81.1,
    address: '天津港储能园区堆场车棚'
  },
  {
    id: 'PV-006',
    name: '成都西部基地光伏电站',
    region: '西南园区',
    longitude: 104.0668,
    latitude: 30.5728,
    status: 'normal',
    capacityKwp: 5400,
    realtimePowerKw: 3618,
    todayEnergyKwh: 17924,
    todayRevenueCny: 7169.6,
    pr: 83.5,
    equivalentHours: 3.32,
    healthGrade: 'A',
    availability: 98.7,
    address: '成都西部基地生产楼及连廊'
  }
]

const dashboardStations = rawDashboardStations.map(item => {
  const resourceUnit = findResourceUnitByAnchorStationId(item.id)
  return {
    ...item,
    resourceUnitId: resourceUnit ? resourceUnit.id : '',
    resourceUnitName: resourceUnit ? resourceUnit.name : ''
  }
})

const dashboardAlarmItems = [
  {
    id: 'ALM-20260322-001',
    time: '14:32',
    level: 'critical',
    levelLabel: '紧急',
    deviceName: '逆变器 INV-03',
    stationId: 'PV-003',
    stationName: '武汉物流基地光伏电站',
    alarmType: 'dc-over-voltage',
    description: '直流侧过压告警，需立即复核组串电压与断路器状态。',
    status: '未处理',
    owner: '华中运维一组',
    suggestion: '建议 15 分钟内派单复核，并同步检查同列逆变器。'
  },
  {
    id: 'ALM-20260322-002',
    time: '14:18',
    level: 'major',
    levelLabel: '重要',
    deviceName: '箱变 T1',
    stationId: 'PV-002',
    stationName: '松山湖智造园光伏电站',
    alarmType: 'transformer-high-temp',
    description: '箱变高压侧过温预警，温升超过基线 8.6°C。',
    status: '处理中',
    owner: '华南运维中心',
    suggestion: '核查散热风道和负荷分配，必要时切换低峰窗口检修。'
  },
  {
    id: 'ALM-20260322-003',
    time: '13:57',
    level: 'minor',
    levelLabel: '一般',
    deviceName: '汇流箱 HB-07',
    stationId: 'PV-001',
    stationName: '深圳湾科技园光伏电站',
    alarmType: 'low-current',
    description: '支路电流偏低，疑似单支路积灰或遮挡。',
    status: '待确认',
    owner: '华南运维中心',
    suggestion: '安排现场清扫，并对该支路做 IV 曲线复测。'
  },
  {
    id: 'ALM-20260322-004',
    time: '13:40',
    level: 'hint',
    levelLabel: '提示',
    deviceName: '气象站 WS-02',
    stationId: 'PV-006',
    stationName: '成都西部基地光伏电站',
    alarmType: 'data-delay',
    description: '天气采样上报延迟 90 秒，已自动补采。',
    status: '未处理',
    owner: '平台值班',
    suggestion: '观察网络抖动是否持续，无需立刻派单。'
  },
  {
    id: 'ALM-20260322-005',
    time: '13:12',
    level: 'major',
    levelLabel: '重要',
    deviceName: '采集器 DTU-11',
    stationId: 'PV-005',
    stationName: '天津港储能园光伏电站',
    alarmType: 'communication-lost',
    description: '采集器离线超过 15 分钟，关联设备数据中断。',
    status: '未处理',
    owner: '华北运维组',
    suggestion: '优先检查交换机与 4G 备链路，恢复后补传离线数据。'
  }
]

const dashboardWeatherProfiles = {
  'PV-001': {
    stationName: '深圳湾科技园光伏电站',
    current: {
      weather: '多云',
      temperature: 28,
      irradiance: 680,
      humidity: 65,
      windSpeed: 3.2,
      windDirection: '东南风'
    },
    forecast: [
      { date: '03-23', weather: '多云', tempHigh: 29, tempLow: 23, irradianceEstimate: 620 },
      { date: '03-24', weather: '阵雨', tempHigh: 25, tempLow: 21, irradianceEstimate: 320 },
      { date: '03-25', weather: '晴', tempHigh: 30, tempLow: 24, irradianceEstimate: 750 }
    ]
  },
  'PV-002': {
    stationName: '松山湖智造园光伏电站',
    current: {
      weather: '晴转云',
      temperature: 27,
      irradiance: 702,
      humidity: 59,
      windSpeed: 2.7,
      windDirection: '南风'
    },
    forecast: [
      { date: '03-23', weather: '晴', tempHigh: 30, tempLow: 22, irradianceEstimate: 760 },
      { date: '03-24', weather: '多云', tempHigh: 28, tempLow: 22, irradianceEstimate: 580 },
      { date: '03-25', weather: '阵雨', tempHigh: 26, tempLow: 21, irradianceEstimate: 340 }
    ]
  },
  'PV-003': {
    stationName: '武汉物流基地光伏电站',
    current: {
      weather: '阴',
      temperature: 23,
      irradiance: 412,
      humidity: 71,
      windSpeed: 2.1,
      windDirection: '东北风'
    },
    forecast: [
      { date: '03-23', weather: '多云', tempHigh: 25, tempLow: 18, irradianceEstimate: 520 },
      { date: '03-24', weather: '小雨', tempHigh: 22, tempLow: 17, irradianceEstimate: 210 },
      { date: '03-25', weather: '晴', tempHigh: 27, tempLow: 18, irradianceEstimate: 710 }
    ]
  },
  'PV-004': {
    stationName: '合肥研发中心光伏电站',
    current: {
      weather: '小雨',
      temperature: 21,
      irradiance: 236,
      humidity: 78,
      windSpeed: 3.6,
      windDirection: '北风'
    },
    forecast: [
      { date: '03-23', weather: '阴', tempHigh: 24, tempLow: 17, irradianceEstimate: 300 },
      { date: '03-24', weather: '多云', tempHigh: 26, tempLow: 18, irradianceEstimate: 560 },
      { date: '03-25', weather: '晴', tempHigh: 29, tempLow: 20, irradianceEstimate: 740 }
    ]
  },
  'PV-005': {
    stationName: '天津港储能园光伏电站',
    current: {
      weather: '大风',
      temperature: 16,
      irradiance: 458,
      humidity: 48,
      windSpeed: 7.3,
      windDirection: '西北风'
    },
    forecast: [
      { date: '03-23', weather: '晴', tempHigh: 18, tempLow: 10, irradianceEstimate: 610 },
      { date: '03-24', weather: '晴', tempHigh: 19, tempLow: 11, irradianceEstimate: 640 },
      { date: '03-25', weather: '多云', tempHigh: 17, tempLow: 9, irradianceEstimate: 470 }
    ]
  },
  'PV-006': {
    stationName: '成都西部基地光伏电站',
    current: {
      weather: '晴',
      temperature: 24,
      irradiance: 648,
      humidity: 57,
      windSpeed: 2.4,
      windDirection: '西南风'
    },
    forecast: [
      { date: '03-23', weather: '晴', tempHigh: 27, tempLow: 18, irradianceEstimate: 700 },
      { date: '03-24', weather: '多云', tempHigh: 25, tempLow: 17, irradianceEstimate: 540 },
      { date: '03-25', weather: '阵雨', tempHigh: 22, tempLow: 16, irradianceEstimate: 280 }
    ]
  }
}

function roundNumber(value, digits = 1) {
  return Number(Number(value).toFixed(digits))
}

function getDashboardStation(stationId) {
  return (
    dashboardStations.find(item => item.id === stationId) || dashboardStations[0]
  )
}

function normalizeCapacityFilter(params = {}) {
  if (typeof params.capacityMin !== 'undefined' || typeof params.capacityMax !== 'undefined') {
    return {
      min: Number(params.capacityMin || 0),
      max: Number(params.capacityMax || Infinity)
    }
  }

  switch (params.capacityRange) {
    case 'lt3000':
      return { min: 0, max: 3000 }
    case '3000to5000':
      return { min: 3000, max: 5000 }
    case 'gte5000':
      return { min: 5000, max: Infinity }
    default:
      return { min: 0, max: Infinity }
  }
}

function filterDashboardStations(params = {}) {
  const capacity = normalizeCapacityFilter(params)

  return dashboardStations.filter(item => {
    if (params.status && item.status !== params.status) {
      return false
    }

    if (params.region && item.region !== params.region) {
      return false
    }

    if (
      item.capacityKwp < capacity.min ||
      item.capacityKwp > capacity.max
    ) {
      return false
    }

    return true
  })
}

function buildDashboardMapSummary(stations) {
  return Object.keys(dashboardStatusMeta).map(key => ({
    key,
    label: dashboardStatusMeta[key].label,
    color: dashboardStatusMeta[key].color,
    count: stations.filter(item => item.status === key).length
  }))
}

function buildDashboardFilterOptions() {
  return {
    statusOptions: [
      { label: '全部状态', value: '' },
      { label: '正常', value: 'normal' },
      { label: '告警', value: 'warning' },
      { label: '故障', value: 'fault' },
      { label: '检修', value: 'maintenance' },
      { label: '离线', value: 'offline' }
    ],
    regionOptions: [
      { label: '全部区域', value: '' }
    ].concat(
      Array.from(new Set(dashboardStations.map(item => item.region))).map(item => ({
        label: item,
        value: item
      }))
    ),
    capacityOptions: [
      { label: '全部容量', value: '' },
      { label: '3MW 以下', value: 'lt3000' },
      { label: '3MW - 5MW', value: '3000to5000' },
      { label: '5MW 以上', value: 'gte5000' }
    ]
  }
}

function buildDashboardKpiPayload(stationId) {
  const focusStations = stationId
    ? dashboardStations.filter(item => item.id === stationId)
    : dashboardStations
  const capacityKwp = focusStations.reduce((sum, item) => sum + item.capacityKwp, 0)
  const realtimePowerKw = focusStations.reduce(
    (sum, item) => sum + item.realtimePowerKw,
    0
  )
  const todayEnergyKwh = focusStations.reduce(
    (sum, item) => sum + item.todayEnergyKwh,
    0
  )
  const todayRevenueCny = focusStations.reduce(
    (sum, item) => sum + item.todayRevenueCny,
    0
  )
  const availability = roundNumber(
    focusStations.reduce((sum, item) => sum + item.availability, 0) /
      focusStations.length,
    1
  )
  const equivalentHours = roundNumber(
    focusStations.reduce((sum, item) => sum + item.equivalentHours, 0) /
      focusStations.length,
    2
  )
  const co2ReductionTon = roundNumber(todayEnergyKwh * 0.000785, 1)
  const deviationRate = roundNumber(
    stationId ? 3.8 : 4.6,
    1
  )

  return {
    updatedAt: '2026-03-22 14:35',
    focusLabel: stationId ? getDashboardStation(stationId).name : '全系统',
    items: [
      {
        key: 'capacity',
        title: '总装机容量',
        value: roundNumber(capacityKwp / 1000, 1),
        unit: 'MWp',
        icon: 'el-icon-office-building',
        accent: 'teal',
        group: '发电概况',
        helper: '园区集中式 + 分布式汇总',
        changeRate: null,
        changeLabel: '稳定'
      },
      {
        key: 'realtime',
        title: '实时总功率',
        value: roundNumber(realtimePowerKw / 1000, 2),
        unit: 'MW',
        icon: 'el-icon-lightning',
        accent: 'blue',
        group: '发电概况',
        helper: '较 10 分钟前 +2.6%',
        changeRate: 2.6,
        changeLabel: '较 10 分钟前'
      },
      {
        key: 'todayEnergy',
        title: '当日发电量',
        value: roundNumber(todayEnergyKwh / 1000, 2),
        unit: 'MWh',
        icon: 'el-icon-data-analysis',
        accent: 'emerald',
        group: '发电概况',
        helper: '较昨日 +8.3%',
        changeRate: 8.3,
        changeLabel: '较昨日'
      },
      {
        key: 'todayRevenue',
        title: '当日收益',
        value: roundNumber(todayRevenueCny / 10000, 2),
        unit: '万元',
        icon: 'el-icon-coin',
        accent: 'orange',
        group: '发电概况',
        helper: '较昨日 +6.8%',
        changeRate: 6.8,
        changeLabel: '较昨日'
      },
      {
        key: 'equivalentHours',
        title: '等效利用小时',
        value: equivalentHours,
        unit: 'h',
        icon: 'el-icon-timer',
        accent: 'blue',
        group: '运行质量',
        helper: '较去年 +0.14h',
        changeRate: 5.1,
        changeLabel: '较去年'
      },
      {
        key: 'availability',
        title: '设备在线率',
        value: availability,
        unit: '%',
        icon: 'el-icon-cpu',
        accent: 'teal',
        group: '运行质量',
        helper: '在线设备健康度持续稳定',
        changeRate: 0.8,
        changeLabel: '较昨日'
      },
      {
        key: 'co2',
        title: 'CO₂ 减排量',
        value: co2ReductionTon,
        unit: '吨',
        icon: 'el-icon-s-opportunity',
        accent: 'emerald',
        group: '运行质量',
        helper: '累计环境收益折算',
        changeRate: null,
        changeLabel: '累计'
      },
      {
        key: 'deviationRate',
        title: '偏差率',
        value: deviationRate,
        unit: '%',
        icon: 'el-icon-warning-outline',
        accent: deviationRate > 4 ? 'orange' : 'blue',
        group: '运行质量',
        helper: deviationRate > 4 ? '偏差临近上限，需重点关注' : '偏差控制在安全区间',
        changeRate: -0.6,
        changeLabel: '较昨日'
      }
    ]
  }
}

function resolveCurveDate(date) {
  if (!date || date === 'today') {
    return '2026-03-22'
  }

  if (date === 'yesterday') {
    return '2026-03-21'
  }

  return date
}

function buildCurvePointSeries(station, date, seriesKey) {
  const points = []
  const biasMap = {
    actual: 1,
    plan: 0.94,
    forecast: 0.98,
    irradiance: 0.42
  }
  const capacityMw = station.capacityKwp / 1000

  for (let index = 0; index < 96; index += 1) {
    const hour = index / 4
    const hourText = String(Math.floor(hour)).padStart(2, '0')
    const minuteText = String((index % 4) * 15).padStart(2, '0')
    const solarWindow = Math.max(0, Math.sin(((hour - 6) / 12) * Math.PI))
    const weatherFactor = station.status === 'offline' ? 0.05 : station.status === 'fault' ? 0.62 : 0.9
    const actualBase = capacityMw * 980 * solarWindow * weatherFactor
    const actual = roundNumber(actualBase + Math.cos(index / 8) * 22, 0)
    const plan = roundNumber(actualBase * 1.06 + 35, 0)
    const forecast = roundNumber(actualBase * 1.02 + Math.sin(index / 6) * 18, 0)
    const irradiance = roundNumber(actualBase * 1.8 + solarWindow * 160, 0)

    const valueMap = {
      actual,
      plan,
      forecast,
      irradiance
    }

    points.push({
      time: `${date} ${hourText}:${minuteText}:00`,
      value: Math.max(0, valueMap[seriesKey] * biasMap[seriesKey])
    })
  }

  return points
}

function buildDashboardPowerCurvePayload(params = {}) {
  const station = getDashboardStation(params.stationId)
  const date = resolveCurveDate(params.date)
  const actual = buildCurvePointSeries(station, date, 'actual')
  const plan = buildCurvePointSeries(station, date, 'plan')
  const forecast = buildCurvePointSeries(station, date, 'forecast')
  const irradiance = buildCurvePointSeries(station, date, 'irradiance')
  const peakPower = Math.max.apply(
    null,
    actual.map(item => item.value)
  )

  return {
    stationId: station.id,
    stationName: station.name,
    currentDate: date,
    dateTabs: [
      { label: '今日', value: '2026-03-22' },
      { label: '昨日', value: '2026-03-21' }
    ],
    deviationRate: roundNumber(((peakPower - Math.max.apply(null, plan.map(item => item.value))) / Math.max.apply(null, plan.map(item => item.value))) * 100, 1),
    peakPowerKw: peakPower,
    actual,
    plan,
    forecast,
    irradiance
  }
}

function buildDashboardRankingPayload(params = {}) {
  const metric = params.metric || 'energy'
  const metricMeta = {
    energy: {
      label: '日发电量',
      unit: 'kWh',
      accessor: item => item.todayEnergyKwh
    },
    hours: {
      label: '等效小时',
      unit: 'h',
      accessor: item => item.equivalentHours
    },
    pr: {
      label: 'PR 值',
      unit: '%',
      accessor: item => item.pr
    }
  }
  const currentMetric = metricMeta[metric] ? metric : 'energy'
  const rankings = dashboardStations
    .map(item => ({
      stationId: item.id,
      stationName: item.name,
      value: roundNumber(metricMeta[currentMetric].accessor(item), currentMetric === 'energy' ? 0 : 1),
      rankChange: item.status === 'normal' ? 1 : item.status === 'fault' ? -2 : 0
    }))
    .sort((prev, next) => next.value - prev.value)

  return {
    currentMetric,
    metricOptions: [
      { label: '日发电量', value: 'energy' },
      { label: '等效小时', value: 'hours' },
      { label: 'PR 值', value: 'pr' }
    ],
    unit: metricMeta[currentMetric].unit,
    rankings
  }
}

function buildDashboardAlarmFeedPayload(params = {}) {
  const filteredItems = dashboardAlarmItems.filter(item => {
    if (params.level && item.level !== params.level) {
      return false
    }

    if (params.stationId && item.stationId !== params.stationId) {
      return false
    }

    return true
  })

  return {
    summary: {
      critical: dashboardAlarmItems.filter(item => item.level === 'critical').length,
      major: dashboardAlarmItems.filter(item => item.level === 'major').length,
      minor: dashboardAlarmItems.filter(item => item.level === 'minor').length,
      hint: dashboardAlarmItems.filter(item => item.level === 'hint').length
    },
    items: filteredItems
  }
}

function buildDashboardWeatherPayload(params = {}) {
  const station = getDashboardStation(params.stationId)
  const profile = dashboardWeatherProfiles[station.id]

  return {
    stationId: station.id,
    stationName: profile.stationName,
    current: profile.current,
    forecast: profile.forecast
  }
}

const summaryCards = [
  {
    key: 'generation',
    title: '今日发电量',
    value: '128.6',
    unit: 'MWh',
    helper: '同比昨日 +8.2%',
    icon: 'el-icon-lightning',
    accent: 'teal'
  },
  {
    key: 'income',
    title: '本月收益',
    value: '86.4',
    unit: '万元',
    helper: '达成率 92%',
    icon: 'el-icon-coin',
    accent: 'blue'
  },
  {
    key: 'onlineRate',
    title: '设备在线率',
    value: '98.4',
    unit: '%',
    helper: '逆变器在线 486/494',
    icon: 'el-icon-cpu',
    accent: 'emerald'
  },
  {
    key: 'alarm',
    title: '待处理告警',
    value: '17',
    unit: '条',
    helper: '严重告警 2 条',
    icon: 'el-icon-warning-outline',
    accent: 'orange'
  }
]

const trendBars = [
  { label: '03-14', value: 88, rate: 56 },
  { label: '03-15', value: 96, rate: 61 },
  { label: '03-16', value: 118, rate: 75 },
  { label: '03-17', value: 124, rate: 79 },
  { label: '03-18', value: 133, rate: 85 },
  { label: '03-19', value: 126, rate: 80 },
  { label: '03-20', value: 129, rate: 82 }
]

const focusAlarms = [
  {
    id: 'ALM-20260320-01',
    title: '一号逆变器直流侧温升异常',
    level: '严重',
    station: '东区 30MW 电站',
    status: '待派单'
  },
  {
    id: 'ALM-20260320-02',
    title: '汇流箱 07 通讯中断',
    level: '重要',
    station: '北区屋顶电站',
    status: '处理中'
  },
  {
    id: 'ALM-20260320-03',
    title: '气象站辐照度采样波动',
    level: '一般',
    station: '西区 12MW 电站',
    status: '待确认'
  }
]

const stationRows = [
  {
    stationId: 'PV-001',
    stationName: '东区 30MW 电站',
    region: '华北示范园区',
    capacity: '30MW',
    gridStatus: '已并网',
    healthGrade: 'A',
    owner: '园区新能源中心',
    onlineRate: '99.1%',
    revenueRate: '96%'
  },
  {
    stationId: 'PV-002',
    stationName: '北区屋顶电站',
    region: '智慧制造园区',
    capacity: '8.4MW',
    gridStatus: '已并网',
    healthGrade: 'A-',
    owner: '运维一部',
    onlineRate: '98.3%',
    revenueRate: '93%'
  },
  {
    stationId: 'PV-003',
    stationName: '西区 12MW 电站',
    region: '物流园区',
    capacity: '12MW',
    gridStatus: '建设中',
    healthGrade: 'B+',
    owner: '工程建设组',
    onlineRate: '95.2%',
    revenueRate: '88%'
  },
  {
    stationId: 'PV-004',
    stationName: '研发基地车棚电站',
    region: '研发园区',
    capacity: '4.2MW',
    gridStatus: '已并网',
    healthGrade: 'A',
    owner: '运维二部',
    onlineRate: '99.6%',
    revenueRate: '97%'
  }
]

const deviceRows = [
  {
    deviceId: 'INV-001',
    deviceName: '一号逆变器',
    stationName: '东区 30MW 电站',
    type: '逆变器',
    status: '告警',
    loadRate: '86%',
    temperature: '67°C',
    lastReportAt: '2026-03-20 09:18:12'
  },
  {
    deviceId: 'INV-014',
    deviceName: '十四号逆变器',
    stationName: '东区 30MW 电站',
    type: '逆变器',
    status: '在线',
    loadRate: '78%',
    temperature: '54°C',
    lastReportAt: '2026-03-20 09:18:20'
  },
  {
    deviceId: 'CMB-007',
    deviceName: '汇流箱 07',
    stationName: '北区屋顶电站',
    type: '汇流箱',
    status: '离线',
    loadRate: '--',
    temperature: '--',
    lastReportAt: '2026-03-20 08:53:01'
  },
  {
    deviceId: 'ENV-003',
    deviceName: '气象站 03',
    stationName: '西区 12MW 电站',
    type: '气象站',
    status: '在线',
    loadRate: '100%',
    temperature: '31°C',
    lastReportAt: '2026-03-20 09:18:28'
  }
]

const alarmRows = [
  {
    alarmId: 'ALM-20260320-01',
    stationName: '东区 30MW 电站',
    deviceName: '一号逆变器',
    level: '严重',
    status: '待派单',
    category: '温度异常',
    happenedAt: '2026-03-20 09:06:18',
    owner: '值班长'
  },
  {
    alarmId: 'ALM-20260320-02',
    stationName: '北区屋顶电站',
    deviceName: '汇流箱 07',
    level: '重要',
    status: '处理中',
    category: '通讯中断',
    happenedAt: '2026-03-20 08:47:02',
    owner: '运维二部'
  },
  {
    alarmId: 'ALM-20260320-03',
    stationName: '西区 12MW 电站',
    deviceName: '气象站 03',
    level: '一般',
    status: '待确认',
    category: '采样波动',
    happenedAt: '2026-03-20 08:35:11',
    owner: '调度中心'
  },
  {
    alarmId: 'ALM-20260319-21',
    stationName: '研发基地车棚电站',
    deviceName: '采集器 A2',
    level: '提示',
    status: '已关闭',
    category: '网络抖动',
    happenedAt: '2026-03-19 21:12:42',
    owner: '平台值班'
  }
]

function filterByKeyword(rows, keyword, fields) {
  if (!keyword) {
    return rows
  }

  const normalized = keyword.trim().toLowerCase()
  return rows.filter(row =>
    fields.some(field =>
      String(row[field] || '')
        .toLowerCase()
        .includes(normalized)
    )
  )
}

export const mockHandlers = {
  ...stationMonitoringMockHandlers,
  ...stationArchiveMockHandlers,
  ...forecastMockHandlers,
  '/pvms/dashboard/stations-geo': ({ params = {} } = {}) => {
    const stations = filterDashboardStations(params)

    return buildSuccess({
      filters: buildDashboardFilterOptions(),
      summary: buildDashboardMapSummary(stations),
      stations: stations.map(item => ({
        id: item.id,
        name: item.name,
        region: item.region,
        longitude: item.longitude,
        latitude: item.latitude,
        resourceUnitId: item.resourceUnitId,
        resourceUnitName: item.resourceUnitName,
        status: item.status,
        statusLabel: dashboardStatusMeta[item.status].label,
        statusColor: dashboardStatusMeta[item.status].color,
        capacityKwp: item.capacityKwp,
        realtimePowerKw: item.realtimePowerKw,
        todayEnergyKwh: item.todayEnergyKwh,
        todayRevenueCny: item.todayRevenueCny,
        healthGrade: item.healthGrade,
        availability: item.availability,
        address: item.address
      }))
    })
  },
  '/pvms/dashboard/kpi-summary': ({ params = {} } = {}) =>
    buildSuccess(buildDashboardKpiPayload(params.stationId)),
  '/pvms/dashboard/power-curve': ({ params = {} } = {}) =>
    buildSuccess(
      buildDashboardPowerCurvePayload({
        stationId: params.stationId,
        date: params.date
      })
    ),
  '/pvms/dashboard/station-ranking': ({ params = {} } = {}) =>
    buildSuccess(
      buildDashboardRankingPayload({
        metric: params.metric
      })
    ),
  '/pvms/alarms/recent': ({ params = {} } = {}) =>
    buildSuccess(
      buildDashboardAlarmFeedPayload({
        level: params.level,
        stationId: params.stationId
      })
    ),
  '/pvms/weather/current': ({ params = {} } = {}) =>
    buildSuccess(
      buildDashboardWeatherPayload({
        stationId: params.stationId
      })
    ),
  '/pvms/dashboard/overview': () =>
    buildSuccess({
      summaryCards,
      trendBars,
      focusAlarms,
      stations: stationRows
    }),
  '/pvms/stations/archive': ({ params = {} } = {}) => {
    let rows = filterByKeyword(stationRows, params.keyword, [
      'stationName',
      'region',
      'owner'
    ])

    if (params.gridStatus) {
      rows = rows.filter(item => item.gridStatus === params.gridStatus)
    }

    return buildSuccess({
      statistics: [
        { label: '站点总数', value: stationRows.length, unit: '座' },
        { label: '并网容量', value: '54.6', unit: 'MW' },
        { label: 'A级及以上', value: '3', unit: '座' }
      ],
      rows,
      total: rows.length
    })
  },
  '/pvms/devices/monitor': ({ params = {} } = {}) => {
    let rows = filterByKeyword(deviceRows, params.keyword, [
      'deviceName',
      'stationName',
      'type'
    ])

    if (params.status) {
      rows = rows.filter(item => item.status === params.status)
    }

    return buildSuccess({
      summaryCards: [
        { label: '设备总数', value: 612, unit: '台' },
        { label: '在线设备', value: 598, unit: '台' },
        { label: '离线设备', value: 8, unit: '台' },
        { label: '告警设备', value: 6, unit: '台' }
      ],
      deviceGroups: [
        { label: '逆变器', ratio: 72 },
        { label: '汇流箱', ratio: 18 },
        { label: '气象站', ratio: 6 },
        { label: '采集器', ratio: 4 }
      ],
      maintenanceTips: [
        '东区 30MW 电站建议在 10:30 前完成一号逆变器复核。',
        '北区屋顶电站需排查汇流箱 07 通讯链路。',
        '气象站 03 建议对辐照度采样做校准复核。'
      ],
      rows,
      total: rows.length
    })
  },
  '/pvms/adjustable-capacity/realtime': () =>
    buildSuccess({
      totalDeferrableLoad: 4820,
      maxUpCapacity: 1200,
      maxDownCapacity: 3620,
      maxUpRate: 150,
      maxDownRate: 280,
      essSOC: 62.5,
      stationDetails: rawDashboardStations.map(s => ({
        id: s.id,
        name: s.name,
        deferrableLoad: Math.round(s.realtimePowerKw * 0.35),
        maxUpCapacity: Math.round(s.capacityKwp * 0.08),
        maxDownCapacity: Math.round(s.realtimePowerKw * 0.6),
        ratedPower: s.capacityKwp,
        operatingCapacity: s.realtimePowerKw
      })),
      timeSeries: Array.from({ length: 96 }, (_, i) => {
        const h = String(Math.floor(i / 4)).padStart(2, '0')
        const m = String((i % 4) * 15).padStart(2, '0')
        const isDaytime = i >= 24 && i <= 72
        const base = isDaytime ? 2800 + Math.sin((i - 24) / 48 * Math.PI) * 1800 : 400
        return {
          time: `${h}:${m}`,
          maxUpCapacity: Math.round(isDaytime ? 1200 : 800),
          maxDownCapacity: Math.round(base * 0.6)
        }
      })
    }),
  '/pvms/dashboard/vpp-node-status': () =>
    buildSuccess({
      totalDeferrableLoad: 4820,
      todayResponseCount: 3,
      todayResponseEnergyKwh: 2460,
      strategyExecutionRate: 94.2,
      activeStrategyName: '华南园区调峰策略',
      lastReportTime: new Date().toISOString().slice(0, 16).replace('T', ' ')
    }),
  '/pvms/alarms/center': ({ params = {} } = {}) => {
    let rows = filterByKeyword(alarmRows, params.keyword, [
      'alarmId',
      'stationName',
      'deviceName',
      'category'
    ])

    if (params.level) {
      rows = rows.filter(item => item.level === params.level)
    }

    if (params.status) {
      rows = rows.filter(item => item.status === params.status)
    }

    return buildSuccess({
      summaryCards: [
        { label: '今日新增', value: 24, unit: '条' },
        { label: '待处理', value: 17, unit: '条' },
        { label: '处理中', value: 9, unit: '条' },
        { label: '闭环率', value: '86', unit: '%' }
      ],
      processBoard: [
        '严重告警需 15 分钟内派单。',
        '通讯类告警优先核查链路与采集器状态。',
        '同一站点重复告警应合并处理。'
      ],
      rows,
      total: rows.length
    })
  }
}
