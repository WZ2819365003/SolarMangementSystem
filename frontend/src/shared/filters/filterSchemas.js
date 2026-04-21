export const FILTER_SCHEMA_DEFAULTS = {
  granularityOptions: [
    { label: '15分钟', value: '15m' },
    { label: '30分钟', value: '30m' },
    { label: '60分钟', value: '60m' }
  ],
  forecastTypeOptions: [
    { label: '日前预测', value: 'day-ahead' },
    { label: '超短期预测', value: 'ultra-short' }
  ]
}

export const filterSchemas = {
  'production-monitor': {
    columns: 5,
    defaults: {
      resourceUnitId: '',
      stationId: '',
      date: new Date().toISOString().slice(0, 10),
      granularity: '15m'
    },
    fields: [
      { key: 'resourceUnitId', label: '资源单元', type: 'hier-resource-unit' },
      { key: 'stationId', label: '电站', type: 'hier-station' },
      { key: 'date', label: '日期', type: 'date' },
      {
        key: 'granularity',
        label: '粒度',
        type: 'select',
        clearable: false,
        options: FILTER_SCHEMA_DEFAULTS.granularityOptions
      }
    ]
  },
  forecast: {
    columns: 5,
    defaults: {
      resourceUnitId: '',
      stationId: '',
      dateRange: [],
      forecastType: 'day-ahead'
    },
    fields: [
      { key: 'resourceUnitId', label: '资源单元', type: 'hier-resource-unit' },
      { key: 'stationId', label: '电站', type: 'hier-station' },
      { key: 'dateRange', label: '日期范围', type: 'date-range', span: 2 },
      {
        key: 'forecastType',
        label: '预测类型',
        type: 'radio',
        default: 'day-ahead',
        options: FILTER_SCHEMA_DEFAULTS.forecastTypeOptions
      }
    ]
  },
  'strategy:list': {
    columns: 5,
    defaults: {
      resourceUnitId: '',
      stationId: '',
      type: '',
      status: '',
      keyword: ''
    },
    fields: [
      { key: 'resourceUnitId', label: '资源单元', type: 'hier-resource-unit' },
      { key: 'stationId', label: '电站', type: 'hier-station' },
      { key: 'type', label: '策略类型', type: 'select', optionSource: 'strategyTypes' },
      { key: 'status', label: '状态', type: 'select', optionSource: 'strategyStatuses' },
      { key: 'keyword', label: '关键词', type: 'input', placeholder: '策略名称或电站名称' }
    ]
  },
  'strategy:config': {
    columns: 5,
    defaults: {
      resourceUnitId: '',
      stationId: '',
      type: '',
      keyword: ''
    },
    fields: [
      { key: 'resourceUnitId', label: '资源单元', type: 'hier-resource-unit' },
      { key: 'stationId', label: '电站', type: 'hier-station' },
      { key: 'type', label: '策略类型', type: 'select', optionSource: 'strategyTypes' },
      { key: 'keyword', label: '关键词', type: 'input', placeholder: '策略名称或电站名称' }
    ]
  },
  'strategy:revenue': {
    columns: 5,
    defaults: {
      resourceUnitId: '',
      stationId: '',
      type: '',
      dateRange: []
    },
    fields: [
      { key: 'resourceUnitId', label: '资源单元', type: 'hier-resource-unit' },
      { key: 'stationId', label: '电站', type: 'hier-station' },
      { key: 'type', label: '策略类型', type: 'select', optionSource: 'strategyTypes' },
      { key: 'dateRange', label: '日期范围', type: 'date-range', span: 2 }
    ]
  }
}

export function getFilterSchema(filterKey) {
  return filterSchemas[filterKey] || null
}
