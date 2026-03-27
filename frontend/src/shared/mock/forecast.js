/**
 * M03 Forecast & Analysis — mock data
 * 6 endpoints: meta, overview, comparison, deviation-heatmap, adjustable, accuracy
 */

function buildSuccess(data) { return { code: 0, message: 'success', data } }
function round(value, digits) { return Number(Number(value).toFixed(digits || 1)) }
function seededValue(seed, min, max) {
  var x = Math.sin(seed * 9301 + 49297) * 233280
  var r = x - Math.floor(x)
  return min + r * (max - min)
}

// ---------------------------------------------------------------------------
// Station raw data (same 16 stations as station-archive)
// ---------------------------------------------------------------------------

var stations = [
  { id: 'SZ-001', name: '深圳湾科创园 A 站', capacityKwp: 1800, status: 'normal', companyId: 'com-001' },
  { id: 'SZ-002', name: '南山智造中心 B 站', capacityKwp: 1400, status: 'normal', companyId: 'com-001' },
  { id: 'SZ-003', name: '前海冷站屋顶 C 站', capacityKwp: 1200, status: 'warning', companyId: 'com-001' },
  { id: 'SZ-004', name: '生态园 D 站', capacityKwp: 1000, status: 'normal', companyId: 'com-001' },
  { id: 'DG-001', name: '松山湖智造园 A 站', capacityKwp: 1900, status: 'normal', companyId: 'com-001' },
  { id: 'DG-002', name: '松山湖制造园 B 站', capacityKwp: 1600, status: 'warning', companyId: 'com-001' },
  { id: 'WH-001', name: '武汉物流园 A 站', capacityKwp: 1600, status: 'fault', companyId: 'com-002' },
  { id: 'WH-002', name: '武汉仓储园 B 站', capacityKwp: 1400, status: 'warning', companyId: 'com-002' },
  { id: 'WH-003', name: '鄂州协同园 C 站', capacityKwp: 1200, status: 'normal', companyId: 'com-002' },
  { id: 'HF-001', name: '合肥研发中心 A 站', capacityKwp: 1000, status: 'maintenance', companyId: 'com-003' },
  { id: 'HF-002', name: '合肥实证基地 B 站', capacityKwp: 900, status: 'normal', companyId: 'com-003' },
  { id: 'TJ-001', name: '天津港储能园 A 站', capacityKwp: 1200, status: 'offline', companyId: 'com-004' },
  { id: 'TJ-002', name: '天津港仓储园 B 站', capacityKwp: 1000, status: 'offline', companyId: 'com-004' },
  { id: 'CD-001', name: '成都西部基地 A 站', capacityKwp: 1700, status: 'normal', companyId: 'com-005' },
  { id: 'CD-002', name: '成都连廊产区 B 站', capacityKwp: 1500, status: 'normal', companyId: 'com-005' },
  { id: 'CD-003', name: '成都仓储园 C 站', capacityKwp: 1300, status: 'warning', companyId: 'com-005' }
]

var regions = ['华南', '华中', '华东', '西南']

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

// Generate 96 time labels at 15-min intervals: "00:00" .. "23:45"
function makeTimeLabels() {
  var labels = []
  for (var i = 0; i < 96; i++) {
    var h = Math.floor(i / 4)
    var m = (i % 4) * 15
    labels.push((h < 10 ? '0' : '') + h + ':' + (m < 10 ? '0' : '') + m)
  }
  return labels
}

// Bell-shaped PV curve value at slot index i (0-95)
// sunrise = slot 24 (06:00), sunset = slot 72 (18:00)
function pvBell(peakMW, i) {
  var sunrise = 24
  var sunset = 72
  if (i < sunrise || i >= sunset) return 0
  return peakMW * Math.sin(Math.PI * (i - sunrise) / (sunset - sunrise))
}

// ---------------------------------------------------------------------------
// 1. /pvms/forecast/meta
// ---------------------------------------------------------------------------
function handleMeta() {
  return buildSuccess({
    regions: regions,
    stations: stations.map(function (s) {
      return { id: s.id, name: s.name, capacityKwp: s.capacityKwp, status: s.status }
    })
  })
}

// ---------------------------------------------------------------------------
// 2. /pvms/forecast/overview
// ---------------------------------------------------------------------------
function handleOverview() {
  var kpis = {
    dayAheadAccuracy: round(seededValue(101, 90, 94), 2),
    ultraShortAccuracy: round(seededValue(102, 93, 97), 2),
    dailyDeviation: round(seededValue(103, 0.8, 1.6), 2),
    qualifiedRate: round(seededValue(104, 86, 91), 2)
  }

  var stationTable = stations.map(function (s, idx) {
    var capacityMW = s.capacityKwp / 1000
    var predicted = round(seededValue(200 + idx, capacityMW * 0.5, capacityMW * 0.85), 2)
    var deviation = round(seededValue(300 + idx, -0.15, 0.15), 2)
    var actual = round(predicted - deviation, 2)
    var accuracy = round(100 - Math.abs(deviation) / Math.max(actual, 0.01) * 100, 2)
    if (accuracy > 100) accuracy = 99.5
    if (accuracy < 80) accuracy = round(seededValue(400 + idx, 85, 95), 2)
    return {
      name: s.name,
      predicted: predicted,
      actual: actual,
      deviation: deviation,
      accuracy: accuracy
    }
  })

  return buildSuccess({
    kpis: kpis,
    stationTable: stationTable
  })
}

// ---------------------------------------------------------------------------
// 3. /pvms/forecast/comparison
// ---------------------------------------------------------------------------
function handleComparison() {
  var timeLabels = makeTimeLabels()
  var peakMW = 17.5 // total system peak around 17.5 MW

  var actual = []
  var dayAhead = []
  var ultraShort = []

  for (var i = 0; i < 96; i++) {
    var base = round(pvBell(peakMW, i), 2)
    actual.push(base)
    var daOffset = round(seededValue(500 + i, -0.3, 0.3), 2)
    var usOffset = round(seededValue(600 + i, -0.15, 0.15), 2)
    dayAhead.push(round(Math.max(0, base + daOffset), 2))
    ultraShort.push(round(Math.max(0, base + usOffset), 2))
  }

  return buildSuccess({
    timeLabels: timeLabels,
    series: {
      dayAhead: dayAhead,
      ultraShort: ultraShort,
      actual: actual
    }
  })
}

// ---------------------------------------------------------------------------
// 4. /pvms/forecast/deviation-heatmap
// ---------------------------------------------------------------------------
function handleDeviationHeatmap() {
  var hours = []
  for (var h = 0; h < 24; h++) hours.push(h)

  var stationNames = stations.map(function (s) { return s.name })

  var data = []
  for (var si = 0; si < stations.length; si++) {
    var row = []
    for (var hi = 0; hi < 24; hi++) {
      if (hi < 6 || hi >= 19) {
        row.push(0)
      } else {
        // Higher deviation near solar noon (11-14)
        var baseDeviation = (hi >= 10 && hi <= 14) ? 8 : 4
        var val = round(seededValue(700 + si * 24 + hi, baseDeviation * 0.3, baseDeviation * 1.8), 2)
        if (val > 15) val = 15
        row.push(val)
      }
    }
    data.push(row)
  }

  return buildSuccess({
    hours: hours,
    stations: stationNames,
    data: data
  })
}

// ---------------------------------------------------------------------------
// 5. /pvms/forecast/adjustable
// ---------------------------------------------------------------------------
function handleAdjustable() {
  var kpis = {
    totalAdjustable: round(seededValue(801, 7.5, 9.5), 2),
    upAdjustable: round(seededValue(802, 4.5, 6.0), 2),
    downAdjustable: round(seededValue(803, 2.5, 4.0), 2),
    max24h: round(seededValue(804, 11.0, 13.0), 2)
  }

  var timeLabels = makeTimeLabels()
  var predicted = []
  var upperBound = []
  var lowerBound = []
  var adjPeak = 10.0

  for (var i = 0; i < 96; i++) {
    var base = round(pvBell(adjPeak, i), 2)
    predicted.push(base)
    var upMargin = round(seededValue(900 + i, 1.0, 3.0), 2)
    var downMargin = round(seededValue(1000 + i, 1.0, 2.0), 2)
    upperBound.push(round(base + upMargin, 2))
    lowerBound.push(round(Math.max(0, base - downMargin), 2))
  }

  var capacityCurve = {
    timeLabels: timeLabels,
    predicted: predicted,
    upperBound: upperBound,
    lowerBound: lowerBound
  }

  // Timeline windows for each station
  var windowTemplates = [
    [{ start: '06:00', end: '11:30', status: 'available' }, { start: '13:00', end: '18:00', status: 'dispatching' }],
    [{ start: '07:00', end: '12:00', status: 'dispatching' }, { start: '13:30', end: '17:30', status: 'available' }],
    [{ start: '06:30', end: '10:00', status: 'available' }, { start: '10:30', end: '14:00', status: 'dispatching' }, { start: '14:30', end: '17:45', status: 'available' }],
    [{ start: '08:00', end: '12:00', status: 'available' }, { start: '13:00', end: '16:00', status: 'unavailable' }],
    [{ start: '06:00', end: '09:30', status: 'dispatching' }, { start: '10:00', end: '14:30', status: 'available' }, { start: '15:00', end: '18:00', status: 'dispatching' }]
  ]

  var timeline = stations.map(function (s, idx) {
    return {
      name: s.name,
      windows: windowTemplates[idx % windowTemplates.length]
    }
  })

  // Station adjustable table
  var statusOptions = [
    { label: '可调', type: 'success' },
    { label: '正常', type: '' },
    { label: '受限', type: 'warning' },
    { label: '不可用', type: 'danger' }
  ]

  var stationTable = stations.map(function (s, idx) {
    var capacityMW = s.capacityKwp / 1000
    var statusPick = statusOptions[idx % statusOptions.length]
    return {
      name: s.name,
      currentAdj: round(seededValue(1100 + idx, 0.2, capacityMW * 0.6), 2),
      predicted4h: round(seededValue(1200 + idx, 0.3, capacityMW * 0.7), 2),
      maxUp: round(seededValue(1300 + idx, 0.3, capacityMW * 0.5), 2),
      maxDown: round(seededValue(1400 + idx, 0.1, capacityMW * 0.3), 2),
      statusLabel: statusPick.label,
      statusType: statusPick.type
    }
  })

  return buildSuccess({
    kpis: kpis,
    capacityCurve: capacityCurve,
    timeline: timeline,
    stationTable: stationTable
  })
}

// ---------------------------------------------------------------------------
// 6. /pvms/forecast/accuracy
// ---------------------------------------------------------------------------
function handleAccuracy() {
  var kpis = {
    mae: round(seededValue(1501, 0.6, 1.0), 2),
    rmse: round(seededValue(1502, 0.9, 1.4), 2),
    qualifiedRate: round(seededValue(1503, 89, 93), 2),
    monthlyAvgAccuracy: round(seededValue(1504, 92, 95), 2)
  }

  // 30-day trend
  var dates = []
  var dayAheadAccuracy = []
  var ultraShortAccuracy = []
  for (var d = 0; d < 30; d++) {
    var month = d < 3 ? '02' : '03'
    var day = d < 3 ? (25 + d) : (d - 2)
    dates.push('2026-' + month + '-' + (day < 10 ? '0' : '') + day)
    dayAheadAccuracy.push(round(seededValue(1600 + d, 85, 98), 2))
    ultraShortAccuracy.push(round(seededValue(1700 + d, 88, 99), 2))
  }

  var trend = {
    dates: dates,
    dayAheadAccuracy: dayAheadAccuracy,
    ultraShortAccuracy: ultraShortAccuracy
  }

  // Error distribution (bell-shaped, 10 bins)
  var bins = ['-5~-4', '-4~-3', '-3~-2', '-2~-1', '-1~0', '0~1', '1~2', '2~3', '3~4', '4~5']
  var counts = [3, 8, 18, 35, 52, 48, 30, 15, 7, 4]

  var distribution = {
    bins: bins,
    counts: counts
  }

  // Station ranking sorted by accuracy descending
  var stationRanking = stations.map(function (s, idx) {
    return {
      name: s.name,
      accuracy: round(seededValue(1800 + idx, 85, 98), 2)
    }
  })
  stationRanking.sort(function (a, b) { return b.accuracy - a.accuracy })

  // Monthly table (2025-10 to 2026-03)
  var monthlyTable = []
  var monthLabels = ['2025-10', '2025-11', '2025-12', '2026-01', '2026-02', '2026-03']
  for (var m = 0; m < 6; m++) {
    var mae = round(seededValue(1900 + m, 0.6, 1.2), 2)
    var rmse = round(seededValue(2000 + m, 0.9, 1.5), 2)
    var accuracy = round(seededValue(2100 + m, 89, 96), 2)
    var improvement = m === 0 ? 0 : round(seededValue(2200 + m, -1.5, 3.0), 2)
    monthlyTable.push({
      month: monthLabels[m],
      mae: mae,
      rmse: rmse,
      accuracy: accuracy,
      improvement: improvement
    })
  }

  return buildSuccess({
    kpis: kpis,
    trend: trend,
    distribution: distribution,
    stationRanking: stationRanking,
    monthlyTable: monthlyTable
  })
}

// ---------------------------------------------------------------------------
// Export
// ---------------------------------------------------------------------------

export var forecastMockHandlers = {
  '/pvms/forecast/meta': handleMeta,
  '/pvms/forecast/overview': handleOverview,
  '/pvms/forecast/comparison': handleComparison,
  '/pvms/forecast/deviation-heatmap': handleDeviationHeatmap,
  '/pvms/forecast/adjustable': handleAdjustable,
  '/pvms/forecast/accuracy': handleAccuracy
}
