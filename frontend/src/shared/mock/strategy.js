// ---------------------------------------------------------------------------
// M04 Strategy Management — Mock Data
// ---------------------------------------------------------------------------

var companies = [
  { id: 'com-001', name: '深圳市悦美颐华物业管理有限公司' },
  { id: 'com-002', name: '武汉恒实物业管理有限公司' },
  { id: 'com-003', name: '合肥盛景新能源有限公司' },
  { id: 'com-004', name: '天津港储能科技有限公司' },
  { id: 'com-005', name: '成都西部新能源有限公司' }
]

var stations = [
  { id: 'SZ-001', name: '深圳湾科创园 A 站', capacityKwp: 1800, companyId: 'com-001', loadBaseKw: 2100 },
  { id: 'SZ-002', name: '南山智造中心 B 站', capacityKwp: 1400, companyId: 'com-001', loadBaseKw: 1600 },
  { id: 'SZ-003', name: '前海冷站屋顶 C 站', capacityKwp: 1200, companyId: 'com-001', loadBaseKw: 1500 },
  { id: 'SZ-004', name: '生态园 D 站', capacityKwp: 1000, companyId: 'com-001', loadBaseKw: 1200 },
  { id: 'DG-001', name: '松山湖智造园 A 站', capacityKwp: 1900, companyId: 'com-001', loadBaseKw: 2200 },
  { id: 'DG-002', name: '松山湖制造园 B 站', capacityKwp: 1600, companyId: 'com-001', loadBaseKw: 1800 },
  { id: 'WH-001', name: '武汉物流园 A 站', capacityKwp: 1600, companyId: 'com-002', loadBaseKw: 1900 },
  { id: 'WH-002', name: '武汉仓储园 B 站', capacityKwp: 1400, companyId: 'com-002', loadBaseKw: 1700 },
  { id: 'WH-003', name: '鄂州协同园 C 站', capacityKwp: 1200, companyId: 'com-002', loadBaseKw: 1400 },
  { id: 'HF-001', name: '合肥研发中心 A 站', capacityKwp: 1000, companyId: 'com-003', loadBaseKw: 1100 },
  { id: 'HF-002', name: '合肥实证基地 B 站', capacityKwp: 900, companyId: 'com-003', loadBaseKw: 1000 },
  { id: 'TJ-001', name: '天津港储能园 A 站', capacityKwp: 1200, companyId: 'com-004', loadBaseKw: 1400 },
  { id: 'TJ-002', name: '天津港仓储园 B 站', capacityKwp: 1000, companyId: 'com-004', loadBaseKw: 1200 },
  { id: 'CD-001', name: '成都西部基地 A 站', capacityKwp: 1700, companyId: 'com-005', loadBaseKw: 2000 },
  { id: 'CD-002', name: '成都连廊产区 B 站', capacityKwp: 1500, companyId: 'com-005', loadBaseKw: 1700 },
  { id: 'CD-003', name: '成都仓储园 C 站', capacityKwp: 1300, companyId: 'com-005', loadBaseKw: 1500 }
]

// ---------------------------------------------------------------------------
// Strategy type / status enums
// ---------------------------------------------------------------------------

var STRATEGY_TYPES = [
  { label: '需求响应', value: 'demand-response' },
  { label: '调频辅助', value: 'frequency-regulation' },
  { label: '电网约束', value: 'grid-constraint' },
  { label: '削峰填谷', value: 'peak-shaving' }
]

var STRATEGY_STATUSES = [
  { label: '草稿', value: 'draft' },
  { label: '待执行', value: 'pending' },
  { label: '执行中', value: 'executing' },
  { label: '已完成', value: 'completed' },
  { label: '已终止', value: 'terminated' }
]

function typeLabel(val) {
  for (var i = 0; i < STRATEGY_TYPES.length; i++) {
    if (STRATEGY_TYPES[i].value === val) return STRATEGY_TYPES[i].label
  }
  return val
}

function statusLabel(val) {
  for (var i = 0; i < STRATEGY_STATUSES.length; i++) {
    if (STRATEGY_STATUSES[i].value === val) return STRATEGY_STATUSES[i].label
  }
  return val
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function round(v, d) {
  var factor = Math.pow(10, d || 0)
  return Math.round(v * factor) / factor
}

function findStation(id) {
  for (var i = 0; i < stations.length; i++) {
    if (stations[i].id === id) return stations[i]
  }
  return stations[0]
}

function stationsOfCompany(companyId) {
  var result = []
  for (var i = 0; i < stations.length; i++) {
    if (stations[i].companyId === companyId) result.push(stations[i])
  }
  return result
}

var _nextId = 100

function nextStrategyId() {
  _nextId++
  return 'STR-' + String(_nextId).padStart(4, '0')
}

// ---------------------------------------------------------------------------
// Electricity price template (9 periods)
// ---------------------------------------------------------------------------

var DEFAULT_PRICE_PERIODS = [
  { period: '00:00-06:00', type: 'valley', typeLabel: '谷', price: 0.25, action: '填谷' },
  { period: '06:00-08:00', type: 'flat', typeLabel: '平', price: 0.65, action: '正常运行' },
  { period: '08:00-11:00', type: 'peak', typeLabel: '峰', price: 1.11, action: '削峰' },
  { period: '11:00-13:00', type: 'flat', typeLabel: '平', price: 0.65, action: '正常运行' },
  { period: '13:00-15:00', type: 'peak', typeLabel: '峰', price: 1.11, action: '削峰' },
  { period: '15:00-17:00', type: 'flat', typeLabel: '平', price: 0.65, action: '正常运行' },
  { period: '17:00-19:00', type: 'peak', typeLabel: '峰', price: 1.11, action: '削峰' },
  { period: '19:00-22:00', type: 'flat', typeLabel: '平', price: 0.65, action: '正常运行' },
  { period: '22:00-24:00', type: 'valley', typeLabel: '谷', price: 0.25, action: '填谷' }
]

// ---------------------------------------------------------------------------
// Build strategy tree (company -> station, no inverter level)
// ---------------------------------------------------------------------------

function buildStrategyTree() {
  return companies.map(function (company) {
    var companyStations = stationsOfCompany(company.id)
    return {
      id: company.id,
      label: company.name,
      nodeType: 'company',
      statusTag: '代理用户',
      statusColor: '#409eff',
      children: companyStations.map(function (sta) {
        var activeCount = 0
        for (var i = 0; i < strategyRecords.length; i++) {
          if (strategyRecords[i].stationId === sta.id &&
            (strategyRecords[i].status === 'executing' || strategyRecords[i].status === 'pending')) {
            activeCount++
          }
        }
        return {
          id: sta.id,
          label: sta.name,
          nodeType: 'station',
          statusTag: '电站',
          statusColor: activeCount > 0 ? '#67c23a' : '#909399',
          extra: {
            capacityKwp: sta.capacityKwp,
            loadKw: sta.loadBaseKw,
            activeStrategyCount: activeCount
          }
        }
      })
    }
  })
}

// ---------------------------------------------------------------------------
// Pre-generated strategy records (25 strategies covering all types/statuses)
// ---------------------------------------------------------------------------

var strategyRecords = [
  // --- Executing ---
  { id: 'STR-0001', name: '华南园区调峰策略', type: 'demand-response', stationId: 'SZ-001', status: 'executing', executeDate: '2026-03-28', periods: [{ start: '08:00', end: '12:00' }, { start: '13:00', end: '18:00' }], targetPowerKw: 1200, powerUpperLimitKw: 1500, powerLowerLimitKw: 800, estimatedRevenue: 2480, actualRevenue: 2350, createdAt: '2026-03-27 09:00', remark: '配合电网调峰需求' },
  { id: 'STR-0002', name: '南山限功率策略', type: 'grid-constraint', stationId: 'SZ-002', status: 'executing', executeDate: '2026-03-28', periods: [{ start: '09:00', end: '17:00' }], targetPowerKw: 1000, powerUpperLimitKw: 1200, powerLowerLimitKw: 700, estimatedRevenue: 1860, actualRevenue: 1780, createdAt: '2026-03-27 09:30', remark: '电网约束限功率' },
  { id: 'STR-0003', name: '松山湖调频策略', type: 'frequency-regulation', stationId: 'DG-001', status: 'executing', executeDate: '2026-03-28', periods: [{ start: '10:00', end: '16:00' }], targetPowerKw: 1400, powerUpperLimitKw: 1700, powerLowerLimitKw: 1000, estimatedRevenue: 2880, actualRevenue: 2750, createdAt: '2026-03-27 10:00', remark: '参与AGC调频' },
  { id: 'STR-0004', name: '武汉谷时填充策略', type: 'peak-shaving', stationId: 'WH-001', status: 'executing', executeDate: '2026-03-28', periods: [{ start: '00:00', end: '06:00' }, { start: '22:00', end: '24:00' }], targetPowerKw: 900, powerUpperLimitKw: 1100, powerLowerLimitKw: 600, estimatedRevenue: 1520, actualRevenue: 1480, createdAt: '2026-03-27 08:00', remark: '谷时段增出力' },
  { id: 'STR-0005', name: '成都基地调峰策略', type: 'demand-response', stationId: 'CD-001', status: 'executing', executeDate: '2026-03-28', periods: [{ start: '08:00', end: '18:00' }], targetPowerKw: 1300, powerUpperLimitKw: 1600, powerLowerLimitKw: 900, estimatedRevenue: 2640, actualRevenue: 2580, createdAt: '2026-03-27 09:15', remark: '' },

  // --- Pending ---
  { id: 'STR-0006', name: '前海冷站削峰策略', type: 'peak-shaving', stationId: 'SZ-003', status: 'pending', executeDate: '2026-03-29', periods: [{ start: '08:00', end: '11:00' }, { start: '13:00', end: '15:00' }], targetPowerKw: 800, powerUpperLimitKw: 1000, powerLowerLimitKw: 600, estimatedRevenue: 1340, actualRevenue: null, createdAt: '2026-03-27 14:00', remark: '' },
  { id: 'STR-0007', name: '松山湖B站限功率', type: 'grid-constraint', stationId: 'DG-002', status: 'pending', executeDate: '2026-03-29', periods: [{ start: '09:00', end: '16:00' }], targetPowerKw: 1100, powerUpperLimitKw: 1400, powerLowerLimitKw: 800, estimatedRevenue: 2160, actualRevenue: null, createdAt: '2026-03-27 14:30', remark: '' },
  { id: 'STR-0008', name: '武汉仓储园调峰', type: 'demand-response', stationId: 'WH-002', status: 'pending', executeDate: '2026-03-29', periods: [{ start: '08:00', end: '12:00' }], targetPowerKw: 1000, powerUpperLimitKw: 1200, powerLowerLimitKw: 700, estimatedRevenue: 1680, actualRevenue: null, createdAt: '2026-03-27 15:00', remark: '' },
  { id: 'STR-0009', name: '合肥实证基地调频', type: 'frequency-regulation', stationId: 'HF-002', status: 'pending', executeDate: '2026-03-29', periods: [{ start: '10:00', end: '15:00' }], targetPowerKw: 600, powerUpperLimitKw: 800, powerLowerLimitKw: 400, estimatedRevenue: 980, actualRevenue: null, createdAt: '2026-03-27 15:30', remark: '' },
  { id: 'STR-0010', name: '成都连廊削峰策略', type: 'peak-shaving', stationId: 'CD-002', status: 'pending', executeDate: '2026-03-29', periods: [{ start: '08:00', end: '11:00' }, { start: '17:00', end: '19:00' }], targetPowerKw: 1100, powerUpperLimitKw: 1300, powerLowerLimitKw: 800, estimatedRevenue: 2100, actualRevenue: null, createdAt: '2026-03-27 16:00', remark: '' },

  // --- Completed ---
  { id: 'STR-0011', name: '华南调峰策略(03-26)', type: 'demand-response', stationId: 'SZ-001', status: 'completed', executeDate: '2026-03-26', periods: [{ start: '08:00', end: '18:00' }], targetPowerKw: 1200, powerUpperLimitKw: 1500, powerLowerLimitKw: 800, estimatedRevenue: 2480, actualRevenue: 2620, createdAt: '2026-03-25 10:00', remark: '' },
  { id: 'STR-0012', name: '南山限功率(03-26)', type: 'grid-constraint', stationId: 'SZ-002', status: 'completed', executeDate: '2026-03-26', periods: [{ start: '09:00', end: '17:00' }], targetPowerKw: 1000, powerUpperLimitKw: 1200, powerLowerLimitKw: 700, estimatedRevenue: 1860, actualRevenue: 1920, createdAt: '2026-03-25 10:30', remark: '' },
  { id: 'STR-0013', name: '松山湖调频(03-26)', type: 'frequency-regulation', stationId: 'DG-001', status: 'completed', executeDate: '2026-03-26', periods: [{ start: '10:00', end: '16:00' }], targetPowerKw: 1400, powerUpperLimitKw: 1700, powerLowerLimitKw: 1000, estimatedRevenue: 2880, actualRevenue: 2760, createdAt: '2026-03-25 11:00', remark: '' },
  { id: 'STR-0014', name: '武汉谷时填充(03-26)', type: 'peak-shaving', stationId: 'WH-001', status: 'completed', executeDate: '2026-03-26', periods: [{ start: '00:00', end: '06:00' }], targetPowerKw: 900, powerUpperLimitKw: 1100, powerLowerLimitKw: 600, estimatedRevenue: 1520, actualRevenue: 1490, createdAt: '2026-03-25 08:00', remark: '' },
  { id: 'STR-0015', name: '成都基地调峰(03-26)', type: 'demand-response', stationId: 'CD-001', status: 'completed', executeDate: '2026-03-26', periods: [{ start: '08:00', end: '18:00' }], targetPowerKw: 1300, powerUpperLimitKw: 1600, powerLowerLimitKw: 900, estimatedRevenue: 2640, actualRevenue: 2710, createdAt: '2026-03-25 09:00', remark: '' },
  { id: 'STR-0016', name: '前海冷站削峰(03-25)', type: 'peak-shaving', stationId: 'SZ-003', status: 'completed', executeDate: '2026-03-25', periods: [{ start: '08:00', end: '15:00' }], targetPowerKw: 800, powerUpperLimitKw: 1000, powerLowerLimitKw: 600, estimatedRevenue: 1200, actualRevenue: 1180, createdAt: '2026-03-24 09:00', remark: '' },
  { id: 'STR-0017', name: '鄂州协同园调频(03-25)', type: 'frequency-regulation', stationId: 'WH-003', status: 'completed', executeDate: '2026-03-25', periods: [{ start: '10:00', end: '16:00' }], targetPowerKw: 800, powerUpperLimitKw: 1000, powerLowerLimitKw: 600, estimatedRevenue: 1560, actualRevenue: 1520, createdAt: '2026-03-24 10:00', remark: '' },
  { id: 'STR-0018', name: '合肥研发中心调峰(03-25)', type: 'demand-response', stationId: 'HF-001', status: 'completed', executeDate: '2026-03-25', periods: [{ start: '08:00', end: '12:00' }], targetPowerKw: 700, powerUpperLimitKw: 900, powerLowerLimitKw: 500, estimatedRevenue: 1120, actualRevenue: 1060, createdAt: '2026-03-24 11:00', remark: '' },

  // --- Terminated ---
  { id: 'STR-0019', name: '天津港调峰(终止)', type: 'demand-response', stationId: 'TJ-001', status: 'terminated', executeDate: '2026-03-27', periods: [{ start: '08:00', end: '16:00' }], targetPowerKw: 800, powerUpperLimitKw: 1000, powerLowerLimitKw: 500, estimatedRevenue: 1440, actualRevenue: 320, createdAt: '2026-03-26 09:00', remark: '通信中断终止' },
  { id: 'STR-0020', name: '天津仓储园调频(终止)', type: 'frequency-regulation', stationId: 'TJ-002', status: 'terminated', executeDate: '2026-03-27', periods: [{ start: '10:00', end: '15:00' }], targetPowerKw: 700, powerUpperLimitKw: 900, powerLowerLimitKw: 500, estimatedRevenue: 1260, actualRevenue: 180, createdAt: '2026-03-26 09:30', remark: '设备离线终止' },

  // --- Draft ---
  { id: 'STR-0021', name: '生态园D站削峰(草稿)', type: 'peak-shaving', stationId: 'SZ-004', status: 'draft', executeDate: '2026-03-30', periods: [{ start: '08:00', end: '11:00' }], targetPowerKw: 700, powerUpperLimitKw: 900, powerLowerLimitKw: 500, estimatedRevenue: 1080, actualRevenue: null, createdAt: '2026-03-28 08:00', remark: '' },
  { id: 'STR-0022', name: '成都仓储园调峰(草稿)', type: 'demand-response', stationId: 'CD-003', status: 'draft', executeDate: '2026-03-30', periods: [{ start: '08:00', end: '18:00' }], targetPowerKw: 900, powerUpperLimitKw: 1100, powerLowerLimitKw: 600, estimatedRevenue: 1760, actualRevenue: null, createdAt: '2026-03-28 08:30', remark: '' },
  { id: 'STR-0023', name: '鄂州协同园削峰(草稿)', type: 'peak-shaving', stationId: 'WH-003', status: 'draft', executeDate: '2026-03-30', periods: [{ start: '08:00', end: '11:00' }, { start: '17:00', end: '19:00' }], targetPowerKw: 800, powerUpperLimitKw: 1000, powerLowerLimitKw: 600, estimatedRevenue: 1420, actualRevenue: null, createdAt: '2026-03-28 09:00', remark: '' },
  { id: 'STR-0024', name: '合肥实证基地限功率(草稿)', type: 'grid-constraint', stationId: 'HF-002', status: 'draft', executeDate: '2026-03-30', periods: [{ start: '09:00', end: '16:00' }], targetPowerKw: 600, powerUpperLimitKw: 800, powerLowerLimitKw: 400, estimatedRevenue: 920, actualRevenue: null, createdAt: '2026-03-28 09:30', remark: '' },
  { id: 'STR-0025', name: '深圳湾科创园调频(草稿)', type: 'frequency-regulation', stationId: 'SZ-001', status: 'draft', executeDate: '2026-03-30', periods: [{ start: '10:00', end: '16:00' }], targetPowerKw: 1300, powerUpperLimitKw: 1600, powerLowerLimitKw: 900, estimatedRevenue: 2560, actualRevenue: null, createdAt: '2026-03-28 10:00', remark: '' }
]

// ---------------------------------------------------------------------------
// Revenue simulation helper
// ---------------------------------------------------------------------------

function simulateRevenue(params) {
  var sta = findStation(params.stationId || 'SZ-001')
  var targetKw = params.targetPowerKw || 1000
  var totalHours = 0
  var periods = params.periods || [{ start: '08:00', end: '18:00' }]
  for (var i = 0; i < periods.length; i++) {
    var startH = parseInt(periods[i].start.split(':')[0], 10)
    var endH = parseInt(periods[i].end.split(':')[0], 10)
    totalHours += (endH - startH)
  }
  var peakHours = Math.min(totalHours * 0.4, 6)
  var flatHours = totalHours - peakHours
  var peakRevenue = round(targetKw * 1.11 * peakHours / 1000 * 380, 0)
  var flatRevenue = round(targetKw * 0.65 * flatHours / 1000 * 220, 0)
  var total = peakRevenue + flatRevenue
  var confidence = round(total * 0.13, 0)

  return {
    estimatedRevenue: total,
    confidenceRange: [total - confidence, total + confidence],
    breakdown: {
      peakSaving: peakRevenue,
      flatRevenue: flatRevenue
    },
    hourlyRevenue: buildHourlyRevenue(periods, targetKw),
    feasibility: {
      status: targetKw <= sta.loadBaseKw ? 'ok' : 'warning',
      warnings: targetKw > sta.loadBaseKw * 0.9
        ? [{ period: '14:00-15:00', message: '接近可调上限，建议降低目标功率' }]
        : []
    }
  }
}

function buildHourlyRevenue(periods, targetKw) {
  var result = []
  for (var p = 0; p < periods.length; p++) {
    var startH = parseInt(periods[p].start.split(':')[0], 10)
    var endH = parseInt(periods[p].end.split(':')[0], 10)
    for (var h = startH; h < endH; h++) {
      var hStr = String(h).padStart(2, '0') + ':00'
      var isPeak = (h >= 8 && h < 11) || (h >= 13 && h < 15) || (h >= 17 && h < 19)
      var price = isPeak ? 1.11 : 0.65
      result.push({
        hour: hStr,
        revenue: round(targetKw * price / 1000 * (180 + Math.random() * 80), 0)
      })
    }
  }
  return result
}

// ---------------------------------------------------------------------------
// Revenue summary builder (30-day trend)
// ---------------------------------------------------------------------------

function buildRevenueSummary() {
  var dates = []
  var dailyRevenue = []
  var cumulativeRevenue = []
  var cumulative = 0

  for (var d = 1; d <= 28; d++) {
    var dateStr = '2026-03-' + String(d).padStart(2, '0')
    var daily = round(1800 + Math.random() * 1200, 0)
    cumulative += daily
    dates.push(dateStr)
    dailyRevenue.push(daily)
    cumulativeRevenue.push(cumulative)
  }

  return {
    kpi: {
      monthlyTotal: cumulative,
      dailyAvg: round(cumulative / 28, 0),
      successRate: 94.2,
      monthOverMonth: 12.5
    },
    trend: {
      dates: dates,
      dailyRevenue: dailyRevenue,
      cumulativeRevenue: cumulativeRevenue
    },
    typeBreakdown: [
      { type: '需求响应', revenue: round(cumulative * 0.42, 0), ratio: 42 },
      { type: '调频辅助', revenue: round(cumulative * 0.24, 0), ratio: 24 },
      { type: '削峰填谷', revenue: round(cumulative * 0.22, 0), ratio: 22 },
      { type: '电网约束', revenue: round(cumulative * 0.12, 0), ratio: 12 }
    ]
  }
}

// ---------------------------------------------------------------------------
// Mock handler helpers
// ---------------------------------------------------------------------------

function buildSuccess(data) {
  return { code: 0, message: 'success', data: data }
}

function filterStrategies(params) {
  var list = strategyRecords.slice()

  if (params.status) {
    list = list.filter(function (s) { return s.status === params.status })
  }
  if (params.type) {
    list = list.filter(function (s) { return s.type === params.type })
  }
  if (params.stationId) {
    list = list.filter(function (s) { return s.stationId === params.stationId })
  }

  return list.map(function (s) {
    var sta = findStation(s.stationId)
    var periodStr = s.periods.map(function (p) { return p.start + '-' + p.end }).join(', ')
    return {
      id: s.id,
      name: s.name,
      type: s.type,
      typeLabel: typeLabel(s.type),
      stationId: s.stationId,
      stationName: sta.name,
      status: s.status,
      statusLabel: statusLabel(s.status),
      executeDate: s.executeDate,
      timePeriod: periodStr,
      periods: s.periods,
      targetPowerKw: s.targetPowerKw,
      powerUpperLimitKw: s.powerUpperLimitKw,
      powerLowerLimitKw: s.powerLowerLimitKw,
      estimatedRevenue: s.estimatedRevenue,
      actualRevenue: s.actualRevenue,
      createdAt: s.createdAt,
      remark: s.remark
    }
  })
}

// ---------------------------------------------------------------------------
// Exported mock handlers
// ---------------------------------------------------------------------------

export var strategyMockHandlers = {
  '/pvms/strategy/meta': function () {
    return buildSuccess({
      types: STRATEGY_TYPES,
      statuses: STRATEGY_STATUSES,
      stations: stations.map(function (s) { return { id: s.id, name: s.name } }),
      companies: companies.map(function (c) { return { id: c.id, name: c.name } }),
      pricePeriods: DEFAULT_PRICE_PERIODS
    })
  },

  '/pvms/strategy/tree': function () {
    return buildSuccess({ tree: buildStrategyTree() })
  },

  '/pvms/strategy/list': function (options) {
    var params = (options && options.params) || {}
    var list = filterStrategies(params)
    var page = parseInt(params.page, 10) || 1
    var pageSize = parseInt(params.pageSize, 10) || 50
    var start = (page - 1) * pageSize
    return buildSuccess({
      items: list.slice(start, start + pageSize),
      total: list.length,
      page: page,
      pageSize: pageSize
    })
  },

  '/pvms/strategy/detail': function (options) {
    var params = (options && options.params) || {}
    var id = params.id || 'STR-0001'
    var record = null
    for (var i = 0; i < strategyRecords.length; i++) {
      if (strategyRecords[i].id === id) { record = strategyRecords[i]; break }
    }
    if (!record) return buildSuccess(null)
    var sta = findStation(record.stationId)
    return buildSuccess({
      id: record.id,
      name: record.name,
      type: record.type,
      typeLabel: typeLabel(record.type),
      stationId: record.stationId,
      stationName: sta.name,
      status: record.status,
      statusLabel: statusLabel(record.status),
      executeDate: record.executeDate,
      periods: record.periods,
      targetPowerKw: record.targetPowerKw,
      powerUpperLimitKw: record.powerUpperLimitKw,
      powerLowerLimitKw: record.powerLowerLimitKw,
      estimatedRevenue: record.estimatedRevenue,
      actualRevenue: record.actualRevenue,
      pricePeriods: DEFAULT_PRICE_PERIODS,
      createdAt: record.createdAt,
      remark: record.remark,
      version: 1
    })
  },

  '/pvms/strategy/create': function (options) {
    var data = (options && options.data) || {}
    var id = nextStrategyId()
    var newRecord = {
      id: id,
      name: data.name || '新策略',
      type: data.type || 'demand-response',
      stationId: data.stationId || 'SZ-001',
      status: 'draft',
      executeDate: data.executeDate || '2026-03-30',
      periods: data.periods || [{ start: '08:00', end: '18:00' }],
      targetPowerKw: data.targetPowerKw || 1000,
      powerUpperLimitKw: data.powerUpperLimitKw || 1200,
      powerLowerLimitKw: data.powerLowerLimitKw || 700,
      estimatedRevenue: data.estimatedRevenue || 0,
      actualRevenue: null,
      createdAt: '2026-03-28 ' + String(new Date().getHours()).padStart(2, '0') + ':' + String(new Date().getMinutes()).padStart(2, '0'),
      remark: data.remark || ''
    }
    strategyRecords.unshift(newRecord)
    return buildSuccess({ id: id })
  },

  '/pvms/strategy/batch-create': function (options) {
    var data = (options && options.data) || {}
    var items = data.strategies || []
    var ids = []
    for (var i = 0; i < items.length; i++) {
      var id = nextStrategyId()
      ids.push(id)
      strategyRecords.unshift({
        id: id,
        name: items[i].name || '批量策略',
        type: items[i].type || 'demand-response',
        stationId: items[i].stationId,
        status: 'draft',
        executeDate: items[i].executeDate || '2026-03-30',
        periods: items[i].periods || [{ start: '08:00', end: '18:00' }],
        targetPowerKw: items[i].targetPowerKw || 1000,
        powerUpperLimitKw: items[i].powerUpperLimitKw || 1200,
        powerLowerLimitKw: items[i].powerLowerLimitKw || 700,
        estimatedRevenue: items[i].estimatedRevenue || 0,
        actualRevenue: null,
        createdAt: '2026-03-28 10:00',
        remark: ''
      })
    }
    return buildSuccess({ ids: ids })
  },

  '/pvms/strategy/submit': function (options) {
    var params = (options && options.params) || {}
    var id = params.id
    for (var i = 0; i < strategyRecords.length; i++) {
      if (strategyRecords[i].id === id) {
        strategyRecords[i].status = 'pending'
        break
      }
    }
    return buildSuccess({ success: true })
  },

  '/pvms/strategy/terminate': function (options) {
    var params = (options && options.params) || {}
    var id = params.id
    for (var i = 0; i < strategyRecords.length; i++) {
      if (strategyRecords[i].id === id) {
        strategyRecords[i].status = 'terminated'
        break
      }
    }
    return buildSuccess({ success: true })
  },

  '/pvms/strategy/batch-submit': function (options) {
    var data = (options && options.data) || {}
    var ids = data.ids || []
    for (var i = 0; i < strategyRecords.length; i++) {
      if (ids.indexOf(strategyRecords[i].id) >= 0) {
        strategyRecords[i].status = 'pending'
      }
    }
    return buildSuccess({ success: true })
  },

  '/pvms/strategy/batch-delete': function (options) {
    var data = (options && options.data) || {}
    var ids = data.ids || []
    for (var i = strategyRecords.length - 1; i >= 0; i--) {
      if (ids.indexOf(strategyRecords[i].id) >= 0 && strategyRecords[i].status === 'draft') {
        strategyRecords.splice(i, 1)
      }
    }
    return buildSuccess({ success: true })
  },

  '/pvms/strategy/electricity-price': function () {
    return buildSuccess({ periods: DEFAULT_PRICE_PERIODS })
  },

  '/pvms/strategy/simulate': function (options) {
    var data = (options && options.data) || {}
    return buildSuccess(simulateRevenue(data))
  },

  '/pvms/strategy/batch-simulate': function (options) {
    var data = (options && options.data) || {}
    var items = data.strategies || []
    var results = items.map(function (item) {
      var sim = simulateRevenue(item)
      return {
        stationId: item.stationId,
        stationName: findStation(item.stationId).name,
        estimatedRevenue: sim.estimatedRevenue,
        feasibility: sim.feasibility
      }
    })
    var totalRevenue = results.reduce(function (sum, r) { return sum + r.estimatedRevenue }, 0)
    return buildSuccess({ results: results, totalRevenue: totalRevenue })
  },

  '/pvms/strategy/revenue/summary': function () {
    return buildSuccess(buildRevenueSummary())
  },

  '/pvms/strategy/revenue/detail': function (options) {
    var params = (options && options.params) || {}
    var completed = strategyRecords.filter(function (s) {
      return s.status === 'completed' || (s.status === 'executing' && s.actualRevenue)
    })
    var items = completed.map(function (s) {
      var sta = findStation(s.stationId)
      var deviation = s.actualRevenue && s.estimatedRevenue
        ? round((s.actualRevenue - s.estimatedRevenue) / s.estimatedRevenue * 100, 1)
        : 0
      return {
        id: s.id,
        name: s.name,
        stationName: sta.name,
        type: s.type,
        typeLabel: typeLabel(s.type),
        timePeriod: s.periods.map(function (p) { return p.start + '-' + p.end }).join(', '),
        estimatedRevenue: s.estimatedRevenue,
        actualRevenue: s.actualRevenue || s.estimatedRevenue,
        deviationRate: deviation
      }
    })
    return buildSuccess({ items: items, total: items.length })
  },

  '/pvms/strategy/compare': function (options) {
    var params = (options && options.params) || {}
    var ids = (params.ids || '').split(',')
    var items = []
    for (var i = 0; i < ids.length; i++) {
      for (var j = 0; j < strategyRecords.length; j++) {
        if (strategyRecords[j].id === ids[i]) {
          var s = strategyRecords[j]
          var sta = findStation(s.stationId)
          items.push({
            id: s.id,
            name: s.name,
            stationName: sta.name,
            typeLabel: typeLabel(s.type),
            targetPowerKw: s.targetPowerKw,
            estimatedRevenue: s.estimatedRevenue,
            actualRevenue: s.actualRevenue || s.estimatedRevenue,
            deviationRate: s.actualRevenue
              ? round((s.actualRevenue - s.estimatedRevenue) / s.estimatedRevenue * 100, 1)
              : 0
          })
        }
      }
    }
    return buildSuccess({ items: items })
  },

  '/pvms/strategy/kpi': function () {
    var executing = strategyRecords.filter(function (s) { return s.status === 'executing' || s.status === 'pending' })
    var todayRevenue = 0
    for (var i = 0; i < strategyRecords.length; i++) {
      if (strategyRecords[i].status === 'executing' && strategyRecords[i].estimatedRevenue) {
        todayRevenue += strategyRecords[i].estimatedRevenue
      }
    }
    var drafts = strategyRecords.filter(function (s) { return s.status === 'draft' })
    return buildSuccess({
      activeCount: executing.length,
      todayRevenue: todayRevenue,
      successRate: 94.2,
      pendingCount: drafts.length
    })
  }
}
