<template>
  <div class="station-monitor-view">
    <div ref="chart" class="station-monitor-view__chart" />
    <!-- For adjustable metric, show additional fields card -->
    <div v-if="metric === 'adjustable' && data && data.fields" class="station-monitor-view__fields">
      <div class="station-monitor-view__fields-title">VPP 可调能力字段</div>
      <div class="station-monitor-view__fields-grid">
        <div v-for="field in adjustableFields" :key="field.key" class="station-monitor-view__field-item">
          <span class="station-monitor-view__field-label">{{ field.label }}</span>
          <span class="station-monitor-view__field-value">
            {{ field.value !== null ? field.value : '--' }}
            <span class="station-monitor-view__field-unit">{{ field.unit }}</span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'StationMonitorView',
  props: {
    data: { type: Object, default: () => ({}) },
    metric: { type: String, default: 'active-power' }
  },
  computed: {
    adjustableFields() {
      if (!this.data || !this.data.fields) return []
      const f = this.data.fields
      return [
        { key: 'deferrableLoad', label: '可延迟负荷', value: f.deferrableLoad, unit: 'kW' },
        { key: 'maxUpCapacity', label: '最大上调容量', value: f.maxUpCapacity, unit: 'kW' },
        { key: 'maxDownCapacity', label: '最大下调容量', value: f.maxDownCapacity, unit: 'kW' },
        { key: 'maxUpRate', label: '上调速率', value: f.maxUpRate, unit: 'kW/min' },
        { key: 'maxDownRate', label: '下调速率', value: f.maxDownRate, unit: 'kW/min' },
        { key: 'maxUpTime', label: '上调持续时长', value: f.maxUpTime, unit: 'min' },
        { key: 'maxDownTime', label: '下调持续时长', value: f.maxDownTime, unit: 'min' },
        { key: 'ratedPower', label: '额定功率', value: f.ratedPower, unit: 'kW' },
        { key: 'operatingCapacity', label: '运行容量', value: f.operatingCapacity, unit: 'kW' },
        { key: 'essSOC', label: '储能 SOC', value: f.essSOC, unit: '%' }
      ]
    }
  },
  watch: {
    data: { handler: 'renderChart', deep: true },
    metric: 'renderChart'
  },
  mounted() {
    this.chart = null
    this.renderChart()
    this._resizeHandler = () => { if (this.chart) this.chart.resize() }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    renderChart() {
      if (!this.$refs.chart || !this.data || !this.data.times) return

      if (!this.chart) {
        this.chart = echarts.init(this.$refs.chart)
      }

      const option = this.buildChartOption()
      this.chart.setOption(option, true)
    },
    buildChartOption() {
      const { times, series } = this.data
      if (!times || !series) return {}

      // Unit labels per metric
      var unitMap = {
        'adjustable': 'kW',
        'pv-output': 'kW',
        'load': 'kW',
        'forecast': 'kW',
        'active-power': 'kW',
        'energy': 'kWh'
      }

      // Color palette — consistent with DashboardPowerCurve
      const colors = ['#1a8dff', '#06d6a0', '#ffd166', '#ef476f', '#118ab2', '#073b4c', '#ff6b6b', '#4ecdc4']

      const yAxisLabel = unitMap[this.metric] || ''

      const legendData = series.map(s => s.name)

      const chartSeries = series.map((s, idx) => {
        const isArea = s.type === 'area'
        const isBar = s.type === 'bar'
        return {
          name: s.name,
          type: isBar ? 'bar' : 'line',
          data: s.data,
          smooth: !isBar,
          symbol: 'none',
          lineStyle: isArea ? { width: 1 } : { width: 2 },
          areaStyle: isArea ? { opacity: 0.15 } : undefined,
          itemStyle: { color: colors[idx % colors.length] },
          barMaxWidth: isBar ? 20 : undefined
        }
      })

      return {
        backgroundColor: 'transparent',
        grid: { left: 60, right: 30, top: 50, bottom: 80 },
        legend: {
          data: legendData,
          bottom: 10,
          textStyle: { color: 'rgba(255,255,255,0.65)', fontSize: 12 },
          icon: 'roundRect',
          itemWidth: 14,
          itemHeight: 3
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(6, 20, 48, 0.95)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: { color: '#fff', fontSize: 12 }
        },
        xAxis: {
          type: 'category',
          data: times,
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
          axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          splitLine: { show: false }
        },
        yAxis: {
          type: 'value',
          name: yAxisLabel,
          nameTextStyle: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          axisLine: { show: false },
          axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
        },
        dataZoom: [
          {
            type: 'slider',
            bottom: 35,
            height: 20,
            borderColor: 'rgba(255,255,255,0.1)',
            backgroundColor: 'rgba(255,255,255,0.03)',
            fillerColor: 'rgba(24,144,255,0.15)',
            handleStyle: { color: '#1890ff' },
            textStyle: { color: 'rgba(255,255,255,0.5)' }
          }
        ],
        series: chartSeries
      }
    }
  }
}
</script>

<style lang="less" scoped>
.station-monitor-view {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  &__chart {
    width: 100%;
    flex: 1;
    min-height: 400px;
  }

  &__fields {
    margin-top: 20px;
    padding: 20px 22px;
    border-radius: 4px;
    border: 1px solid var(--pvms-border-soft, rgba(255, 255, 255, 0.08));
    background: var(--pvms-panel, linear-gradient(180deg, rgba(14, 49, 106, 0.96), rgba(9, 29, 67, 0.98)));
    box-shadow: var(--pvms-shadow-soft, none);
  }

  &__fields-title {
    margin-bottom: 16px;
    font-size: 15px;
    font-weight: 600;
    color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92));
  }

  &__fields-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  &__field-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.06);
  }

  &__field-label {
    font-size: 13px;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
  }

  &__field-value {
    font-size: 15px;
    font-weight: 600;
    color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92));
  }

  &__field-unit {
    margin-left: 4px;
    font-size: 12px;
    font-weight: 400;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
  }
}
</style>
