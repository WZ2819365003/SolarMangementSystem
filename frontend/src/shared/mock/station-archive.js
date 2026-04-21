/**
 * M02 Station Archive — mock data (V4.0)
 * 3-tier tree: Company → Station(=Resource) → Inverter
 * Metrics: adjustable space, PV output, load, forecast, strategy(readonly)
 */

function buildSuccess(data) { return { code: 0, message: 'success', data } }
function round(value, digits) { return Number(Number(value).toFixed(digits || 1)) }
function seededValue(seed, min, max) {
  var x = Math.sin(seed * 9301 + 49297) * 233280
  var r = x - Math.floor(x)
  return min + r * (max - min)
}

// ---------------------------------------------------------------------------
// Station raw data — each station IS a resource node
// ---------------------------------------------------------------------------

var stations = [
  { id: 'SZ-001', name: '深圳湾科创园 A 站', capacityKwp: 1800, status: 'normal', companyId: 'com-001', loadBaseKw: 2100 },
  { id: 'SZ-002', name: '南山智造中心 B 站', capacityKwp: 1400, status: 'normal', companyId: 'com-001', loadBaseKw: 1600 },
  { id: 'SZ-003', name: '前海冷站屋顶 C 站', capacityKwp: 1200, status: 'warning', companyId: 'com-001', loadBaseKw: 1500 },
  { id: 'SZ-004', name: '生态园 D 站', capacityKwp: 1000, status: 'normal', companyId: 'com-001', loadBaseKw: 1200 },
  { id: 'DG-001', name: '松山湖智造园 A 站', capacityKwp: 1900, status: 'normal', companyId: 'com-001', loadBaseKw: 2200 },
  { id: 'DG-002', name: '松山湖制造园 B 站', capacityKwp: 1600, status: 'warning', companyId: 'com-001', loadBaseKw: 1800 },
  { id: 'WH-001', name: '武汉物流园 A 站', capacityKwp: 1600, status: 'fault', companyId: 'com-002', loadBaseKw: 1900 },
  { id: 'WH-002', name: '武汉仓储园 B 站', capacityKwp: 1400, status: 'warning', companyId: 'com-002', loadBaseKw: 1700 },
  { id: 'WH-003', name: '鄂州协同园 C 站', capacityKwp: 1200, status: 'normal', companyId: 'com-002', loadBaseKw: 1400 },
  { id: 'HF-001', name: '合肥研发中心 A 站', capacityKwp: 1000, status: 'maintenance', companyId: 'com-003', loadBaseKw: 1100 },
  { id: 'HF-002', name: '合肥实证基地 B 站', capacityKwp: 900, status: 'normal', companyId: 'com-003', loadBaseKw: 1000 },
  { id: 'TJ-001', name: '天津港储能园 A 站', capacityKwp: 1200, status: 'offline', companyId: 'com-004', loadBaseKw: 1400 },
  { id: 'TJ-002', name: '天津港仓储园 B 站', capacityKwp: 1000, status: 'offline', companyId: 'com-004', loadBaseKw: 1200 },
  { id: 'CD-001', name: '成都西部基地 A 站', capacityKwp: 1700, status: 'normal', companyId: 'com-005', loadBaseKw: 2000 },
  { id: 'CD-002', name: '成都连廊产区 B 站', capacityKwp: 1500, status: 'normal', companyId: 'com-005', loadBaseKw: 1700 },
  { id: 'CD-003', name: '成都仓储园 C 站', capacityKwp: 1300, status: 'warning', companyId: 'com-005', loadBaseKw: 1500 }
]

var companies = [
  { id: 'com-001', name: '深圳市悦美颐华物业管理有限公司' },
  { id: 'com-002', name: '武汉恒实物业管理有限公司' },
  { id: 'com-003', name: '合肥盛景新能源有限公司' },
  { id: 'com-004', name: '天津港储能科技有限公司' },
  { id: 'com-005', name: '成都西部新能源有限公司' }
]

// ---------------------------------------------------------------------------
// Lookup helpers
// ---------------------------------------------------------------------------

function findStation(stationId) {
  for (var i = 0; i < stations.length; i++) {
    if (stations[i].id === stationId) return stations[i]
  }
  return stations[0]
}

function stationIndex(stationId) {
  for (var i = 0; i < stations.length; i++) {
    if (stations[i].id === stationId) return i
  }
  return 0
}

function stationsOfCompany(companyId) {
  var result = []
  for (var i = 0; i < stations.length; i++) {
    if (stations[i].companyId === companyId) result.push(stations[i])
  }
  return result
}

// ---------------------------------------------------------------------------
// Inverter builder
// ---------------------------------------------------------------------------

function buildInverters(station) {
  var count = station.capacityKwp > 1500 ? 3 : 2
  var unitKw = Math.round(station.capacityKwp / count)
  var result = []
  for (var i = 0; i < count; i++) {
    result.push({
      id: station.id + '-INV-' + String(i + 1).padStart(2, '0'),
      name: 'INV-' + String(i + 1).padStart(2, '0') + ' (' + unitKw + 'kW)',
      nodeType: 'inverter',
      statusTag: '逆变器',
      statusColor: '#e6a23c',
      extra: {
        ratedPowerKw: unitKw,
        realtimePowerKw: round(unitKw * seededValue(i + station.capacityKwp, 0.5, 0.9)),
        status: station.status === 'offline' ? 'offline' : 'normal'
      }
    })
  }
  return result
}

// ---------------------------------------------------------------------------
// Tree builder — Company → Station → Inverter
// ---------------------------------------------------------------------------

function buildFullTree() {
  return companies.map(function (company) {
    var companyStations = stationsOfCompany(company.id)
    return {
      id: company.id,
      label: company.name,
      nodeType: 'company',
      statusTag: '代理用户',
      statusColor: '#409eff',
      children: companyStations.map(function (sta) {
        return {
          id: sta.id,
          label: sta.name,
          nodeType: 'station',
          statusTag: '电站',
          statusColor: '#67c23a',
          extra: {
            capacityKwp: sta.capacityKwp,
            status: sta.status,
            loadKw: sta.loadBaseKw,
            pvOutputKw: round(sta.capacityKwp * 0.65),
            adjustableKw: round(sta.loadBaseKw - sta.capacityKwp * 0.65)
          },
          children: buildInverters(sta)
        }
      })
    }
  })
}

// ---------------------------------------------------------------------------
// Time axis helpers
// ---------------------------------------------------------------------------

function buildTimeAxis15m() {
  var result = []
  for (var m = 0; m < 24 * 60; m += 15) {
    var hh = String(Math.floor(m / 60)).padStart(2, '0')
    var mm = String(m % 60).padStart(2, '0')
    result.push(hh + ':' + mm)
  }
  return result
}

// ---------------------------------------------------------------------------
// Solar window helper — returns 0 at night, sine peak at noon
// ---------------------------------------------------------------------------

function solarFactor(minuteOfDay) {
  var decimalHour = minuteOfDay / 60
  return Math.max(0, Math.sin(((decimalHour - 6) / 12) * Math.PI))
}

// ---------------------------------------------------------------------------
// Load curve helper — flat-ish with morning/afternoon peaks
// ---------------------------------------------------------------------------

function loadFactor(minuteOfDay) {
  var h = minuteOfDay / 60
  // Base load 0.4, morning peak 9-11, afternoon peak 14-17
  var base = 0.4
  var morningPeak = Math.max(0, 0.35 * Math.exp(-0.5 * Math.pow((h - 10) / 1.5, 2)))
  var afternoonPeak = Math.max(0, 0.30 * Math.exp(-0.5 * Math.pow((h - 15.5) / 2, 2)))
  // Night reduction
  var nightDip = (h < 6 || h > 22) ? -0.15 : 0
  return Math.max(0.1, base + morningPeak + afternoonPeak + nightDip)
}

// ---------------------------------------------------------------------------
// Station realtime: Adjustable space
// ---------------------------------------------------------------------------

function buildStationAdjustable(stationId) {
  var station = findStation(stationId)
  var sIdx = stationIndex(stationId)
  var times = buildTimeAxis15m()
  var cap = station.capacityKwp
  var loadBase = station.loadBaseKw

  var loadData = []
  var pvOutputData = []
  var adjustableData = []
  var upperLimit = []
  var lowerLimit = []

  var peakAdjustable = 0
  var currentAdjustable = 0

  for (var i = 0; i < times.length; i++) {
    var minuteOfDay = i * 15
    var sf = solarFactor(minuteOfDay)
    var lf = loadFactor(minuteOfDay)

    var noiseL = seededValue(sIdx * 1000 + i, -0.03, 0.03)
    var noiseP = seededValue(sIdx * 2000 + i, -0.04, 0.04)

    var load = round(loadBase * (lf + noiseL), 1)
    var pvOut = round(Math.max(0, cap * sf * (0.78 + noiseP)), 1)
    var adj = round(load - pvOut, 1)
    var upper = round(load * 1.1, 1)
    var lower = round(Math.max(0, load * 0.3), 1)

    loadData.push(load)
    pvOutputData.push(pvOut)
    adjustableData.push(adj)
    upperLimit.push(upper)
    lowerLimit.push(lower)

    if (Math.abs(adj) > Math.abs(peakAdjustable)) peakAdjustable = adj
    // Use i=56 (14:00) as "current" for display
    if (i === 56) currentAdjustable = adj
  }

  var utilizationRate = peakAdjustable !== 0 ? round(Math.abs(currentAdjustable) / Math.abs(peakAdjustable) * 100, 1) : 0

  // Revenue mocks
  var todayPvRevenue = round(cap * seededValue(sIdx * 800, 0.18, 0.35), 0)
  var todayDispatchRevenue = round(currentAdjustable * seededValue(sIdx * 900, 0.08, 0.22), 0)
  var monthlyPvRevenue = round(todayPvRevenue * seededValue(sIdx * 810, 22, 28), 0)
  var monthlyDispatchRevenue = round(todayDispatchRevenue * seededValue(sIdx * 910, 20, 26), 0)
  var cumulativePvRevenue = round(monthlyPvRevenue * seededValue(sIdx * 820, 3.5, 6.0), 0)
  var cumulativeDispatchRevenue = round(monthlyDispatchRevenue * seededValue(sIdx * 920, 3.0, 5.5), 0)

  // Monthly VPP stats
  var monthlyStats = {
    responseCount: Math.round(seededValue(sIdx * 1100, 18, 45)),
    responseSuccessRate: round(seededValue(sIdx * 1200, 88, 99), 1),
    totalResponseEnergy: round(cap * seededValue(sIdx * 1300, 2.5, 5.5), 0),
    avgDeviationRate: round(seededValue(sIdx * 1400, 0.8, 4.5), 1),
    peakShavingCount: Math.round(seededValue(sIdx * 1500, 8, 20)),
    frequencyRegCount: Math.round(seededValue(sIdx * 1600, 5, 15))
  }

  return {
    times: times,
    series: [
      { name: '负荷', data: loadData, type: 'line' },
      { name: '光伏出力', data: pvOutputData, type: 'line' },
      { name: '可调空间', data: adjustableData, type: 'area' },
      { name: '上限', data: upperLimit, type: 'line' },
      { name: '下限', data: lowerLimit, type: 'line' }
    ],
    kpis: [
      { key: 'currentAdjustable', title: '当前可调空间', value: round(currentAdjustable, 1), unit: 'kW' },
      { key: 'peakAdjustable', title: '峰值可调空间', value: round(peakAdjustable, 1), unit: 'kW' },
      { key: 'utilizationRate', title: '可调利用率', value: utilizationRate, unit: '%' }
    ],
    stationKpis: {
      adjustableCapacityKw: round(currentAdjustable, 1),
      todayPvRevenue: todayPvRevenue,
      todayDispatchRevenue: todayDispatchRevenue,
      monthlyPvRevenue: monthlyPvRevenue,
      monthlyDispatchRevenue: monthlyDispatchRevenue,
      cumulativePvRevenue: cumulativePvRevenue,
      cumulativeDispatchRevenue: cumulativeDispatchRevenue
    },
    monthlyStats: monthlyStats,
    fields: {
      deferrableLoad: round(loadBase * 0.35, 1),
      maxUpCapacity: round(cap * 0.08, 1),
      maxDownCapacity: round(currentAdjustable * 0.6, 1),
      maxUpRate: round(seededValue(sIdx * 2100, 20, 60), 1),
      maxDownRate: round(seededValue(sIdx * 2200, 30, 80), 1),
      maxUpTime: Math.round(seededValue(sIdx * 2300, 15, 60)),
      maxDownTime: Math.round(seededValue(sIdx * 2400, 15, 60)),
      ratedPower: cap,
      operatingCapacity: round(cap * seededValue(sIdx * 2500, 0.65, 0.85), 1),
      essSOC: round(seededValue(sIdx * 2600, 30, 85), 1)
    }
  }
}

// ---------------------------------------------------------------------------
// Station realtime: PV Output (per inverter)
// ---------------------------------------------------------------------------

function buildStationPvOutput(stationId) {
  var station = findStation(stationId)
  var sIdx = stationIndex(stationId)
  var times = buildTimeAxis15m()
  var cap = station.capacityKwp
  var invCount = cap > 1500 ? 3 : 2
  var unitKw = Math.round(cap / invCount)

  var totalOutput = []
  var inverterSeries = []
  var colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c']

  // Initialize inverter arrays
  for (var inv = 0; inv < invCount; inv++) {
    inverterSeries.push({
      name: 'INV-' + String(inv + 1).padStart(2, '0'),
      data: [],
      color: colors[inv]
    })
  }

  for (var i = 0; i < times.length; i++) {
    var minuteOfDay = i * 15
    var sf = solarFactor(minuteOfDay)
    var total = 0

    for (var j = 0; j < invCount; j++) {
      var noise = seededValue(sIdx * 3000 + j * 100 + i, -0.05, 0.05)
      var invPower = round(Math.max(0, unitKw * sf * (0.78 + noise)), 1)
      inverterSeries[j].data.push(invPower)
      total += invPower
    }

    totalOutput.push(round(total, 1))
  }

  var series = [{ name: '总出力', data: totalOutput, type: 'line' }]
  for (var k = 0; k < inverterSeries.length; k++) {
    inverterSeries[k].type = 'line'
    series.push(inverterSeries[k])
  }

  return {
    times: times,
    series: series
  }
}

// ---------------------------------------------------------------------------
// Station realtime: Load
// ---------------------------------------------------------------------------

function buildStationLoad(stationId) {
  var station = findStation(stationId)
  var sIdx = stationIndex(stationId)
  var times = buildTimeAxis15m()
  var loadBase = station.loadBaseKw

  var totalLoad = []
  var acData = []
  var lightingData = []
  var powerEquipData = []

  for (var i = 0; i < times.length; i++) {
    var minuteOfDay = i * 15
    var lf = loadFactor(minuteOfDay)
    var noiseT = seededValue(sIdx * 4000 + i, -0.02, 0.02)

    var total = round(loadBase * (lf + noiseT), 1)
    // Split load into subcategories: AC ~40%, lighting ~25%, power equipment ~35%
    var noiseAc = seededValue(sIdx * 4100 + i, -0.03, 0.03)
    var noiseLt = seededValue(sIdx * 4200 + i, -0.02, 0.02)

    var ac = round(total * (0.40 + noiseAc), 1)
    var lighting = round(total * (0.25 + noiseLt), 1)
    var powerEquip = round(Math.max(0, total - ac - lighting), 1)

    totalLoad.push(total)
    acData.push(ac)
    lightingData.push(lighting)
    powerEquipData.push(powerEquip)
  }

  return {
    times: times,
    series: [
      { name: '总负荷', data: totalLoad, type: 'line' },
      { name: '空调', data: acData, type: 'area' },
      { name: '照明', data: lightingData, type: 'area' },
      { name: '动力设备', data: powerEquipData, type: 'area' }
    ]
  }
}

// ---------------------------------------------------------------------------
// Station realtime: Forecast comparison
// ---------------------------------------------------------------------------

function buildStationForecast(stationId) {
  var station = findStation(stationId)
  var sIdx = stationIndex(stationId)
  var times = buildTimeAxis15m()
  var cap = station.capacityKwp

  var dayAhead = []
  var ultraShort = []
  var actual = []

  for (var i = 0; i < times.length; i++) {
    var minuteOfDay = i * 15
    var sf = solarFactor(minuteOfDay)
    var baseOutput = cap * sf * 0.78

    // Actual with small noise
    var noiseA = seededValue(sIdx * 5000 + i, -0.04, 0.04)
    var actualVal = round(Math.max(0, baseOutput * (1 + noiseA)), 1)

    // Day-ahead forecast: +-10-15% deviation from actual
    var noiseDa = seededValue(sIdx * 5100 + i, -0.15, 0.15)
    var daVal = round(Math.max(0, baseOutput * (1 + noiseDa)), 1)

    // Ultra-short-term forecast: +-5-8% deviation from actual
    var noiseUs = seededValue(sIdx * 5200 + i, -0.08, 0.08)
    var usVal = round(Math.max(0, baseOutput * (1 + noiseUs)), 1)

    actual.push(actualVal)
    dayAhead.push(daVal)
    ultraShort.push(usVal)
  }

  return {
    times: times,
    series: [
      { name: '日前预测', data: dayAhead, type: 'line' },
      { name: '超短期预测', data: ultraShort, type: 'line' },
      { name: '实际出力', data: actual, type: 'line' }
    ]
  }
}

// ---------------------------------------------------------------------------
// Strategy data (read-only, same structure as before)
// ---------------------------------------------------------------------------

var strategyByStation = {
  'SZ-001': {
    currentStrategy: {
      name: '华南园区调峰策略',
      type: '需求响应',
      status: '执行中',
      startTime: '2026-03-26 06:00',
      endTime: '2026-03-26 22:00',
      targetPowerKw: 1200,
      estimatedRevenueCny: 580
    },
    executionLogs: [
      { time: '14:00', action: '限功率至 1200kW', result: '成功', deviationRate: 1.2 },
      { time: '13:45', action: '接收调度指令', result: '成功', deviationRate: 0 },
      { time: '10:30', action: '限功率至 1500kW', result: '成功', deviationRate: 2.1 }
    ]
  },
  'SZ-002': {
    currentStrategy: {
      name: '南山区域调频策略',
      type: '调频辅助',
      status: '执行中',
      startTime: '2026-03-26 07:00',
      endTime: '2026-03-26 20:00',
      targetPowerKw: 1000,
      estimatedRevenueCny: 420
    },
    executionLogs: [
      { time: '13:30', action: '限功率至 1000kW', result: '成功', deviationRate: 0.8 },
      { time: '11:00', action: '接收调度指令', result: '成功', deviationRate: 0 },
      { time: '09:15', action: '限功率至 1200kW', result: '成功', deviationRate: 1.5 }
    ]
  },
  'SZ-003': {
    currentStrategy: {
      name: '前海分布式限出策略',
      type: '电网约束',
      status: '告警中',
      startTime: '2026-03-26 08:00',
      endTime: '2026-03-26 18:00',
      targetPowerKw: 800,
      estimatedRevenueCny: 310
    },
    executionLogs: [
      { time: '13:00', action: '限功率至 800kW', result: '偏差', deviationRate: 5.6 },
      { time: '12:00', action: '接收限出指令', result: '成功', deviationRate: 0 },
      { time: '10:00', action: '限功率至 1000kW', result: '成功', deviationRate: 1.8 }
    ]
  },
  'SZ-004': {
    currentStrategy: {
      name: '生态园自发自用策略',
      type: '自发自用',
      status: '执行中',
      startTime: '2026-03-26 06:30',
      endTime: '2026-03-26 19:00',
      targetPowerKw: 700,
      estimatedRevenueCny: 260
    },
    executionLogs: [
      { time: '12:30', action: '调整至 700kW', result: '成功', deviationRate: 0.9 },
      { time: '10:00', action: '接收调度指令', result: '成功', deviationRate: 0 }
    ]
  },
  'DG-001': {
    currentStrategy: {
      name: '松山湖调峰策略',
      type: '需求响应',
      status: '执行中',
      startTime: '2026-03-26 06:00',
      endTime: '2026-03-26 21:00',
      targetPowerKw: 1500,
      estimatedRevenueCny: 720
    },
    executionLogs: [
      { time: '14:15', action: '限功率至 1500kW', result: '成功', deviationRate: 0.6 },
      { time: '13:00', action: '接收调度指令', result: '成功', deviationRate: 0 },
      { time: '10:00', action: '限功率至 1700kW', result: '成功', deviationRate: 1.4 }
    ]
  },
  'DG-002': {
    currentStrategy: {
      name: '松山湖制造园限出策略',
      type: '电网约束',
      status: '告警中',
      startTime: '2026-03-26 07:30',
      endTime: '2026-03-26 19:30',
      targetPowerKw: 1100,
      estimatedRevenueCny: 480
    },
    executionLogs: [
      { time: '14:00', action: '限功率至 1100kW', result: '偏差', deviationRate: 4.2 },
      { time: '12:30', action: '接收限出指令', result: '成功', deviationRate: 0 },
      { time: '09:00', action: '限功率至 1400kW', result: '成功', deviationRate: 2.0 }
    ]
  },
  'WH-001': {
    currentStrategy: {
      name: '武汉物流园故障降额策略',
      type: '故障响应',
      status: '异常',
      startTime: '2026-03-26 05:00',
      endTime: '2026-03-26 23:00',
      targetPowerKw: 600,
      estimatedRevenueCny: 180
    },
    executionLogs: [
      { time: '14:00', action: '降额至 600kW', result: '失败', deviationRate: 12.4 },
      { time: '12:00', action: '接收故障降额指令', result: '成功', deviationRate: 0 },
      { time: '08:00', action: '降额至 1000kW', result: '偏差', deviationRate: 8.6 }
    ]
  },
  'WH-002': {
    currentStrategy: {
      name: '武汉仓储园限出策略',
      type: '电网约束',
      status: '告警中',
      startTime: '2026-03-26 07:00',
      endTime: '2026-03-26 20:00',
      targetPowerKw: 1000,
      estimatedRevenueCny: 380
    },
    executionLogs: [
      { time: '13:30', action: '限功率至 1000kW', result: '偏差', deviationRate: 3.4 },
      { time: '11:00', action: '接收限出指令', result: '成功', deviationRate: 0 }
    ]
  },
  'WH-003': {
    currentStrategy: {
      name: '鄂州协同园调峰策略',
      type: '需求响应',
      status: '执行中',
      startTime: '2026-03-26 06:30',
      endTime: '2026-03-26 21:30',
      targetPowerKw: 900,
      estimatedRevenueCny: 410
    },
    executionLogs: [
      { time: '14:00', action: '限功率至 900kW', result: '成功', deviationRate: 1.0 },
      { time: '12:00', action: '接收调度指令', result: '成功', deviationRate: 0 },
      { time: '09:00', action: '限功率至 1100kW', result: '成功', deviationRate: 1.6 }
    ]
  },
  'HF-001': {
    currentStrategy: {
      name: '合肥研发中心检修策略',
      type: '检修窗口',
      status: '检修中',
      startTime: '2026-03-26 06:00',
      endTime: '2026-03-26 18:00',
      targetPowerKw: 400,
      estimatedRevenueCny: 120
    },
    executionLogs: [
      { time: '12:00', action: '维持检修限额 400kW', result: '成功', deviationRate: 2.0 },
      { time: '08:00', action: '进入检修模式', result: '成功', deviationRate: 0 }
    ]
  },
  'HF-002': {
    currentStrategy: {
      name: '合肥实证基地调峰策略',
      type: '需求响应',
      status: '执行中',
      startTime: '2026-03-26 07:00',
      endTime: '2026-03-26 20:00',
      targetPowerKw: 650,
      estimatedRevenueCny: 280
    },
    executionLogs: [
      { time: '13:30', action: '限功率至 650kW', result: '成功', deviationRate: 0.7 },
      { time: '10:00', action: '接收调度指令', result: '成功', deviationRate: 0 }
    ]
  },
  'TJ-001': {
    currentStrategy: {
      name: '天津港储能园离线恢复策略',
      type: '故障响应',
      status: '离线',
      startTime: '2026-03-26 00:00',
      endTime: '2026-03-26 23:59',
      targetPowerKw: 0,
      estimatedRevenueCny: 0
    },
    executionLogs: [
      { time: '10:00', action: '尝试通信恢复', result: '失败', deviationRate: 0 },
      { time: '08:00', action: '检测到通信中断', result: '告警', deviationRate: 0 }
    ]
  },
  'TJ-002': {
    currentStrategy: {
      name: '天津港仓储园离线恢复策略',
      type: '故障响应',
      status: '离线',
      startTime: '2026-03-26 00:00',
      endTime: '2026-03-26 23:59',
      targetPowerKw: 0,
      estimatedRevenueCny: 0
    },
    executionLogs: [
      { time: '10:30', action: '尝试通信恢复', result: '失败', deviationRate: 0 },
      { time: '08:15', action: '检测到通信中断', result: '告警', deviationRate: 0 }
    ]
  },
  'CD-001': {
    currentStrategy: {
      name: '成都西部基地调峰策略',
      type: '需求响应',
      status: '执行中',
      startTime: '2026-03-26 06:00',
      endTime: '2026-03-26 22:00',
      targetPowerKw: 1400,
      estimatedRevenueCny: 680
    },
    executionLogs: [
      { time: '14:00', action: '限功率至 1400kW', result: '成功', deviationRate: 0.9 },
      { time: '13:00', action: '接收调度指令', result: '成功', deviationRate: 0 },
      { time: '10:00', action: '限功率至 1600kW', result: '成功', deviationRate: 1.3 }
    ]
  },
  'CD-002': {
    currentStrategy: {
      name: '成都连廊产区调频策略',
      type: '调频辅助',
      status: '执行中',
      startTime: '2026-03-26 07:00',
      endTime: '2026-03-26 21:00',
      targetPowerKw: 1200,
      estimatedRevenueCny: 550
    },
    executionLogs: [
      { time: '13:30', action: '限功率至 1200kW', result: '成功', deviationRate: 0.5 },
      { time: '11:00', action: '接收调度指令', result: '成功', deviationRate: 0 },
      { time: '08:30', action: '限功率至 1400kW', result: '成功', deviationRate: 1.1 }
    ]
  },
  'CD-003': {
    currentStrategy: {
      name: '成都仓储园限出策略',
      type: '电网约束',
      status: '告警中',
      startTime: '2026-03-26 08:00',
      endTime: '2026-03-26 19:00',
      targetPowerKw: 900,
      estimatedRevenueCny: 340
    },
    executionLogs: [
      { time: '13:00', action: '限功率至 900kW', result: '偏差', deviationRate: 3.2 },
      { time: '11:00', action: '接收限出指令', result: '成功', deviationRate: 0 }
    ]
  }
}

function buildStationStrategy(stationId) {
  return strategyByStation[stationId] || strategyByStation['SZ-001']
}

// ---------------------------------------------------------------------------
// Status factor for power calculation
// ---------------------------------------------------------------------------

var statusFactor = {
  normal: 1,
  warning: 0.88,
  fault: 0.52,
  maintenance: 0.72,
  offline: 0.18
}

// ---------------------------------------------------------------------------
// Company overview — KPIs + station table
// ---------------------------------------------------------------------------

function buildCompanyOverview(companyId) {
  var company = null
  for (var ci = 0; ci < companies.length; ci++) {
    if (companies[ci].id === companyId) { company = companies[ci]; break }
  }
  if (!company) company = companies[0]

  var companyStations = stationsOfCompany(company.id)

  var totalCapacityKwp = 0
  var totalStations = companyStations.length
  var onlineStations = 0
  var totalLoadKw = 0
  var totalPvOutputKw = 0

  var stationDetails = []

  for (var si = 0; si < companyStations.length; si++) {
    var s = companyStations[si]
    totalCapacityKwp += s.capacityKwp
    totalLoadKw += s.loadBaseKw
    if (s.status !== 'offline') onlineStations++

    var sI = stationIndex(s.id)
    var sFactor = statusFactor[s.status] || 1
    var realtimeFactor = seededValue(sI * 300, 0.65, 0.85) * sFactor
    var realtimePowerKw = round(s.capacityKwp * realtimeFactor, 1)
    var pvOut = round(s.capacityKwp * 0.65 * sFactor, 1)
    var adjKw = round(s.loadBaseKw - pvOut, 1)

    totalPvOutputKw += pvOut

    stationDetails.push({
      id: s.id,
      name: s.name,
      capacityKwp: s.capacityKwp,
      realtimePowerKw: realtimePowerKw,
      loadKw: s.loadBaseKw,
      adjustableKw: adjKw,
      status: s.status
    })
  }

  var totalCapacityMw = round(totalCapacityKwp / 1000, 1)
  var sIdx0 = stationIndex(companyStations[0].id)
  var powerFactor = seededValue(sIdx0 * 400, 0.60, 0.80)
  var realtimePowerMw = round(totalCapacityMw * powerFactor, 2)
  var energyFactor = seededValue(sIdx0 * 500, 3.0, 5.0)
  var todayEnergyMwh = round(totalCapacityMw * energyFactor, 1)
  var onlineRate = totalStations > 0 ? round(onlineStations / totalStations * 100, 1) : 0
  var totalAdjustableKw = round(totalLoadKw - totalPvOutputKw, 1)

  // Revenue mock data
  var pvRevenueCny = round(todayEnergyMwh * seededValue(sIdx0 * 600, 380, 520), 0)
  var dispatchRevenueCny = round(totalAdjustableKw * seededValue(sIdx0 * 700, 0.12, 0.28), 0)
  var monthlyPvRevenueCny = round(pvRevenueCny * seededValue(sIdx0 * 800, 22, 28), 0)
  var monthlyDispatchRevenueCny = round(dispatchRevenueCny * seededValue(sIdx0 * 900, 20, 26), 0)

  return {
    name: company.name,
    kpis: [
      { key: 'capacity', title: '总装机', value: totalCapacityMw, unit: 'MWp', icon: 'el-icon-office-building', accent: 'teal' },
      { key: 'power', title: '实时功率', value: realtimePowerMw, unit: 'MW', icon: 'el-icon-lightning', accent: 'blue' },
      { key: 'energy', title: '当日发电', value: todayEnergyMwh, unit: 'MWh', icon: 'el-icon-data-analysis', accent: 'emerald' },
      { key: 'adjustable', title: '总可调空间', value: round(totalAdjustableKw / 1000, 2), unit: 'MW', icon: 'el-icon-sort', accent: 'orange' },
      { key: 'pvRevenue', title: '光伏收益(今日)', value: pvRevenueCny, unit: '元', icon: 'el-icon-coin', accent: 'emerald' },
      { key: 'dispatchRevenue', title: '调控收益(今日)', value: dispatchRevenueCny, unit: '元', icon: 'el-icon-money', accent: 'orange' },
      { key: 'monthlyPvRevenue', title: '本月光伏收益', value: monthlyPvRevenueCny, unit: '元', icon: 'el-icon-coin', accent: 'teal' },
      { key: 'monthlyDispatchRevenue', title: '本月调控收益', value: monthlyDispatchRevenueCny, unit: '元', icon: 'el-icon-money', accent: 'blue' },
      { key: 'online', title: '在线率', value: onlineRate, unit: '%', icon: 'el-icon-success', accent: 'emerald' }
    ],
    stations: stationDetails
  }
}

// ---------------------------------------------------------------------------
// Inverter realtime data
// ---------------------------------------------------------------------------

function buildInverterRealtime(inverterId) {
  // Parse station id and inverter index from inverterId like "SZ-001-INV-01"
  var parts = inverterId.split('-INV-')
  var stationId = parts[0] || 'SZ-001'
  var invIdx = parts[1] ? parseInt(parts[1], 10) - 1 : 0

  var station = findStation(stationId)
  var sIdx = stationIndex(stationId)
  var invCount = station.capacityKwp > 1500 ? 3 : 2
  var unitKw = Math.round(station.capacityKwp / invCount)

  var efficiency = round(seededValue(sIdx * 6000 + invIdx, 95, 99), 1)
  var dailyEnergy = round(unitKw * seededValue(sIdx * 6100 + invIdx, 3.5, 5.5) / 1000, 2)
  var realtimePower = round(unitKw * seededValue(sIdx * 6200 + invIdx, 0.5, 0.9), 1)
  var invStatus = station.status === 'offline' ? 'offline' : 'normal'

  // Small power curve (96 points)
  var times = buildTimeAxis15m()
  var powerCurve = []
  for (var i = 0; i < times.length; i++) {
    var minuteOfDay = i * 15
    var sf = solarFactor(minuteOfDay)
    var noise = seededValue(sIdx * 7000 + invIdx * 100 + i, -0.05, 0.05)
    var power = round(Math.max(0, unitKw * sf * (0.78 + noise)), 1)
    powerCurve.push(power)
  }

  // Topology structure
  var stringCount = Math.round(seededValue(sIdx * 7100 + invIdx, 8, 16))
  var panelsPerString = Math.round(seededValue(sIdx * 7200 + invIdx, 18, 28))
  var strings = []
  for (var si = 0; si < stringCount; si++) {
    var stringStatus = seededValue(sIdx * 7300 + invIdx * 100 + si, 0, 1) > 0.9 ? 'fault' : 'normal'
    strings.push({
      id: 'STR-' + String(si + 1).padStart(2, '0'),
      name: '组串 ' + (si + 1),
      panelCount: panelsPerString,
      currentA: round(seededValue(sIdx * 7400 + invIdx * 100 + si, 6, 11), 2),
      voltageV: round(seededValue(sIdx * 7500 + invIdx * 100 + si, 550, 720), 1),
      status: stringStatus
    })
  }

  // Device alarms
  var alarmTemplates = [
    { type: '通信异常', level: '告警', desc: '与采集器通信超时 > 30s' },
    { type: 'MPPT偏低', level: '告警', desc: 'MPPT效率低于阈值 92%' },
    { type: '过温保护', level: '严重', desc: '模块温度超过 75°C' },
    { type: '绝缘阻抗低', level: '告警', desc: '绝缘阻抗低于 500kΩ' },
    { type: '组串离线', level: '故障', desc: '组串未检测到电流' }
  ]
  var alarmCount = Math.round(seededValue(sIdx * 7600 + invIdx, 0, 4))
  var alarms = []
  for (var ai = 0; ai < alarmCount; ai++) {
    var tmpl = alarmTemplates[ai % alarmTemplates.length]
    alarms.push({
      id: 'ALM-' + inverterId + '-' + String(ai + 1).padStart(2, '0'),
      type: tmpl.type,
      level: tmpl.level,
      description: tmpl.desc,
      time: '2026-03-26 ' + String(8 + ai * 2).padStart(2, '0') + ':' + String(Math.round(seededValue(ai * 99, 0, 59))).padStart(2, '0'),
      status: ai === 0 ? '未处理' : '已确认'
    })
  }

  return {
    inverterId: inverterId,
    stationId: stationId,
    ratedPowerKw: unitKw,
    realtimePowerKw: realtimePower,
    dailyEnergyMwh: dailyEnergy,
    status: invStatus,
    efficiency: efficiency,
    powerCurve: {
      times: times,
      series: [{ name: '逆变器功率', data: powerCurve, type: 'line' }]
    },
    topology: {
      inverterName: 'INV-' + String(invIdx + 1).padStart(2, '0'),
      ratedPowerKw: unitKw,
      stringCount: stringCount,
      panelsPerString: panelsPerString,
      totalPanels: stringCount * panelsPerString,
      strings: strings
    },
    alarms: alarms,
    deviceInfo: {
      model: 'SUN2000-' + unitKw + 'KTL',
      manufacturer: '华为',
      sn: 'HW' + stationId.replace(/-/g, '') + String(invIdx + 1).padStart(2, '0'),
      firmwareVersion: 'V300R001C10SPC230',
      installDate: '2024-06-15',
      mpptChannels: Math.round(seededValue(sIdx * 7800 + invIdx, 4, 8)),
      dcInputVoltage: round(seededValue(sIdx * 7900 + invIdx, 580, 720), 1) + ' V',
      acOutputVoltage: '380 V',
      gridFrequency: '50.0 Hz',
      moduleTemperature: round(seededValue(sIdx * 8000 + invIdx, 35, 65), 1) + ' °C',
      ambientTemperature: round(seededValue(sIdx * 8100 + invIdx, 22, 35), 1) + ' °C'
    }
  }
}

// ---------------------------------------------------------------------------
// Mock handler map
// ---------------------------------------------------------------------------

var stationArchiveMockHandlers = {
  '/pvms/station-tree': function () {
    return buildSuccess({ tree: buildFullTree() })
  },
  '/pvms/station-archive/company-overview': function (options) {
    var companyId = (options.params && options.params.companyId) || 'com-001'
    return buildSuccess(buildCompanyOverview(companyId))
  },
  '/pvms/station-archive/station-realtime': function (options) {
    var params = options.params || {}
    var stationId = params.stationId || 'SZ-001'
    var metric = params.metric || 'adjustable'
    if (metric === 'adjustable') return buildSuccess(buildStationAdjustable(stationId))
    if (metric === 'pv-output') return buildSuccess(buildStationPvOutput(stationId))
    if (metric === 'load') return buildSuccess(buildStationLoad(stationId))
    if (metric === 'forecast') return buildSuccess(buildStationForecast(stationId))
    return buildSuccess(buildStationAdjustable(stationId))
  },
  '/pvms/station-archive/station-strategy': function (options) {
    var stationId = (options.params && options.params.stationId) || 'SZ-001'
    return buildSuccess(buildStationStrategy(stationId))
  },
  '/pvms/station-archive/inverter-realtime': function (options) {
    var inverterId = (options.params && options.params.inverterId) || 'SZ-001-INV-01'
    return buildSuccess(buildInverterRealtime(inverterId))
  }
}

module.exports = { stationArchiveMockHandlers: stationArchiveMockHandlers }
