<template>
  <div
    class="dashboard-map-insight-panel"
    data-testid="dashboard-map-insight-panel"
  >
    <dashboard-focus-station-list
      :items="focusStations"
      :selected-station-id="selectedStationId"
      @select="$emit('station-select', $event)"
    />
    <dashboard-vpp-node-status
      :payload="vppNodePayload"
    />
    <dashboard-region-summary :items="regionSummary" />
  </div>
</template>

<script>
import DashboardFocusStationList from './DashboardFocusStationList.vue'
import DashboardRegionSummary from './DashboardRegionSummary.vue'
import DashboardVppNodeStatus from './DashboardVppNodeStatus.vue'

const alarmLevelRank = {
  critical: 4,
  major: 3,
  minor: 2,
  hint: 1
}

const statusRank = {
  fault: 5,
  offline: 4,
  warning: 3,
  maintenance: 2,
  normal: 1
}

function roundNumber(value, digits = 1) {
  return Number(Number(value || 0).toFixed(digits))
}

function buildAlarmMeta(alarms) {
  return alarms.reduce((accumulator, item) => {
    const current = accumulator[item.stationId] || {
      count: 0,
      highestLevel: '',
      highestLevelLabel: '',
      deviceName: ''
    }
    const nextRank = alarmLevelRank[item.level] || 0
    const currentRank = alarmLevelRank[current.highestLevel] || 0

    accumulator[item.stationId] = {
      count: current.count + 1,
      highestLevel: nextRank > currentRank ? item.level : current.highestLevel,
      highestLevelLabel:
        nextRank > currentRank ? item.levelLabel : current.highestLevelLabel,
      deviceName: nextRank > currentRank ? item.deviceName : current.deviceName
    }

    return accumulator
  }, {})
}

function resolveIssueLabel(station, alarmMeta) {
  if (alarmMeta.count) {
    return `${alarmMeta.highestLevelLabel} · ${alarmMeta.deviceName}`
  }

  switch (station.status) {
    case 'fault':
      return '故障待排查'
    case 'offline':
      return '离线待恢复'
    case 'maintenance':
      return '检修窗口中'
    case 'warning':
      return '性能波动待确认'
    default:
      return '运行平稳'
  }
}

function resolveStationMetric(station) {
  if (station.status === 'offline') {
    return '当前功率 0 kW'
  }

  return `当前功率 ${station.realtimePowerKw} kW`
}

function buildRiskStation(station, alarmMeta, selectedStationId) {
  const todayEnergyMwh = roundNumber(Number(station.todayEnergyKwh || 0) / 1000, 1)
  return {
    id: station.id,
    name: station.name,
    region: station.region,
    statusLabel: station.statusLabel,
    statusColor: station.statusColor,
    issueLabel: resolveIssueLabel(station, alarmMeta),
    metricText: resolveStationMetric(station),
    metaText: `可用率 ${roundNumber(station.availability, 1)}%`,
    badges: [
      {
        key: 'alarms',
        label: `告警 ${alarmMeta.count || 0} 条`
      },
      {
        key: 'health',
        label: `健康 ${station.healthGrade || '--'}`
      },
      {
        key: 'energy',
        label: `日发 ${todayEnergyMwh} MWh`
      }
    ],
    riskScore:
      (statusRank[station.status] || 0) * 10 +
      (alarmLevelRank[alarmMeta.highestLevel] || 0) * 3 +
      (100 - Number(station.availability || 0)) / 10 +
      (station.id === selectedStationId ? 100 : 0)
  }
}

function buildFocusStations(stations, alarms, selectedStationId) {
  const alarmMetaMap = buildAlarmMeta(alarms)
  const riskStations = stations.map(station =>
    buildRiskStation(
      station,
      alarmMetaMap[station.id] || {},
      selectedStationId
    )
  )
  const selectedStation = riskStations.find(item => item.id === selectedStationId)
  const ordered = riskStations
    .slice()
    .sort((prev, next) => next.riskScore - prev.riskScore)

  const result = []
  if (selectedStation) {
    result.push(selectedStation)
  }

  ordered.forEach(item => {
    if (result.some(current => current.id === item.id) || result.length >= 4) {
      return
    }
    result.push(item)
  })

  return result.map(item =>
    Object.assign({}, item, {
      isSelected: item.id === selectedStationId
    })
  )
}

function buildRegionSummary(stations) {
  const regionMap = stations.reduce((accumulator, item) => {
    if (!accumulator[item.region]) {
      accumulator[item.region] = {
        regionName: item.region,
        stationCount: 0,
        capacityKwp: 0,
        availabilitySum: 0,
        riskCount: 0
      }
    }

    accumulator[item.region].stationCount += 1
    accumulator[item.region].capacityKwp += Number(item.capacityKwp || 0)
    accumulator[item.region].availabilitySum += Number(item.availability || 0)
    if (item.status !== 'normal') {
      accumulator[item.region].riskCount += 1
    }

    return accumulator
  }, {})

  return Object.keys(regionMap)
    .map(key => {
      const item = regionMap[key]
      return {
        regionName: item.regionName,
        stationCount: item.stationCount,
        capacityMw: roundNumber(item.capacityKwp / 1000, 1),
        onlineRate: roundNumber(item.availabilitySum / item.stationCount, 1),
        riskCount: item.riskCount
      }
    })
    .sort((prev, next) => next.capacityMw - prev.capacityMw)
    .slice(0, 4)
}

export default {
  name: 'DashboardMapInsightPanel',
  components: {
    DashboardFocusStationList,
    DashboardRegionSummary,
    DashboardVppNodeStatus
  },
  props: {
    stations: {
      type: Array,
      default: () => []
    },
    alarms: {
      type: Array,
      default: () => []
    },
    selectedStationId: {
      type: String,
      default: ''
    },
    vppNodePayload: {
      type: Object,
      default: () => ({})
    }
  },
  computed: {
    focusStations() {
      return buildFocusStations(
        this.stations,
        this.alarms,
        this.selectedStationId
      )
    },
    regionSummary() {
      return buildRegionSummary(this.stations)
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-map-insight-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.9fr) minmax(0, 1fr);
  gap: 14px;
  flex: 1;
  min-height: 220px;
}

@media (max-width: 1280px) {
  .dashboard-map-insight-panel {
    grid-template-columns: 1fr;
  }
}
</style>
