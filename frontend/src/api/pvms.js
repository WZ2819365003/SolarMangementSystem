import { request } from '@/shared/host/bridge'

export function fetchOverview() {
  return request('/pvms/dashboard/overview')
}

export function fetchDashboardStationsGeo(params) {
  return request('/pvms/dashboard/stations-geo', {
    method: 'get',
    params
  })
}

export function fetchDashboardKpiSummary(params) {
  return request('/pvms/dashboard/kpi-summary', {
    method: 'get',
    params
  })
}

export function fetchDashboardPowerCurve(params) {
  return request('/pvms/dashboard/power-curve', {
    method: 'get',
    params
  })
}

export function fetchDashboardStationRanking(params) {
  return request('/pvms/dashboard/station-ranking', {
    method: 'get',
    params
  })
}

export function fetchDashboardAlarmFeed(params) {
  return request('/pvms/alarms/recent', {
    method: 'get',
    params
  })
}

export function fetchDashboardWeather(params) {
  return request('/pvms/weather/current', {
    method: 'get',
    params
  })
}

export function fetchStationArchive(params) {
  return request('/pvms/stations/archive', {
    method: 'get',
    params
  })
}

export function fetchDeviceMonitor(params) {
  return request('/pvms/devices/monitor', {
    method: 'get',
    params
  })
}

export function fetchAlarmCenter(params) {
  return request('/pvms/alarms/center', {
    method: 'get',
    params
  })
}

export function fetchProductionMonitorMeta(params) {
  return request('/pvms/production-monitor/meta', {
    method: 'get',
    params
  })
}

export function fetchProductionMonitorOverview(params) {
  return request('/pvms/production-monitor/overview', {
    method: 'get',
    params
  })
}

export function fetchProductionMonitorOutput(params) {
  return request('/pvms/production-monitor/output', {
    method: 'get',
    params
  })
}

export function fetchProductionMonitorDispatch(params) {
  return request('/pvms/production-monitor/dispatch', {
    method: 'get',
    params
  })
}

export function fetchProductionMonitorLoad(params) {
  return request('/pvms/production-monitor/load', {
    method: 'get',
    params
  })
}

export function fetchProductionMonitorWeather(params) {
  return request('/pvms/production-monitor/weather', {
    method: 'get',
    params
  })
}

export function fetchResourceUnitList(params) {
  return request('/pvms/resource-units/list', {
    method: 'get',
    params
  })
}

export function fetchResourceUnitOverview(resourceUnitId) {
  return request(`/pvms/resource-units/${resourceUnitId}/overview`, {
    method: 'get'
  })
}

export function fetchResourceUnitPowerCurve(resourceUnitId, params) {
  return request(`/pvms/resource-units/${resourceUnitId}/power-curve`, {
    method: 'get',
    params
  })
}

export function fetchAdjustableCapacity(params) {
  return request('/pvms/adjustable-capacity/realtime', {
    method: 'get',
    params
  })
}

export function fetchVppNodeStatus(params) {
  return request('/pvms/dashboard/vpp-node-status', {
    method: 'get',
    params
  })
}

export function fetchStationTree(params) {
  return request('/pvms/station-tree', {
    method: 'get',
    params
  })
}

export function fetchStationArchiveCompanyOverview(params) {
  return request('/pvms/station-archive/company-overview', {
    method: 'get',
    params
  })
}

export function fetchStationArchiveResourceOverview(params) {
  return request('/pvms/station-archive/resource-overview', {
    method: 'get',
    params
  })
}

export function fetchStationArchiveRealtime(params) {
  return request('/pvms/station-archive/station-realtime', {
    method: 'get',
    params
  })
}

export function fetchStationArchiveAdjustable(params) {
  return request('/pvms/station-archive/station-adjustable', {
    method: 'get',
    params
  })
}

export function fetchStationArchiveStrategy(params) {
  return request('/pvms/station-archive/station-strategy', {
    method: 'get',
    params
  })
}

export function fetchStationArchiveInverterRealtime(params) {
  return request('/pvms/station-archive/inverter-realtime', {
    method: 'get',
    params
  })
}

export const fetchStationList = fetchResourceUnitList
export const fetchStationOverview = fetchResourceUnitOverview
export const fetchStationPowerCurve = fetchResourceUnitPowerCurve

// M03 Forecast & Analysis
export function fetchForecastMeta() {
  return request('/pvms/forecast/meta', { method: 'get' })
}
export function fetchForecastOverview(params) {
  return request('/pvms/forecast/overview', { method: 'get', params })
}
export function fetchForecastAdjustable(params) {
  return request('/pvms/forecast/adjustable', { method: 'get', params })
}
export function fetchForecastAccuracy(params) {
  return request('/pvms/forecast/accuracy', { method: 'get', params })
}
export function fetchForecastComparison(params) {
  return request('/pvms/forecast/comparison', { method: 'get', params })
}
export function fetchForecastDeviationHeatmap(params) {
  return request('/pvms/forecast/deviation-heatmap', { method: 'get', params })
}
