<template>
  <div v-loading="loading" class="forecast-adjustable-view" data-testid="forecast-adjustable-view">
    <!-- KPI Cards -->
    <section class="pv-card-grid forecast-adjustable-view__kpis">
      <app-metric-card
        v-for="item in kpiCards"
        :key="item.key"
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :icon="item.icon"
        :accent="item.accent"
      />
    </section>

    <!-- 24h Capacity Curve -->
    <app-section-card title="24小时可调能力预测">
      <div ref="capacityChart" class="forecast-adjustable-view__chart" />
    </app-section-card>

    <!-- VPP Node Timeline -->
    <app-section-card title="VPP 节点可调时间线">
      <div ref="timelineChart" class="forecast-adjustable-view__chart" />
    </app-section-card>

    <!-- Station Table -->
    <app-section-card title="电站可调能力明细">
      <el-table :data="stationRows" size="mini" stripe>
        <el-table-column prop="name" label="电站" min-width="160" />
        <el-table-column prop="currentAdj" label="当前可调(MW)" width="130" align="right" />
        <el-table-column prop="predicted4h" label="4h预测(MW)" width="130" align="right" />
        <el-table-column prop="maxUp" label="可上调(MW)" width="120" align="right" />
        <el-table-column prop="maxDown" label="可下调(MW)" width="120" align="right" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.statusType" size="mini">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </app-section-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'ForecastAdjustableView',

  components: {
    AppMetricCard,
    AppSectionCard
  },

  props: {
    viewData: {
      type: Object,
      default: () => ({})
    },
    query: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  },

  computed: {
    kpis() {
      return (this.viewData && this.viewData.kpis) || {}
    },

    kpiCards() {
      const kpis = this.kpis
      return [
        { key: 'total', title: '总可调容量', value: kpis.totalAdjustable, unit: 'MW', icon: 'el-icon-s-data', accent: 'teal' },
        { key: 'up', title: '可上调容量', value: kpis.upAdjustable, unit: 'MW', icon: 'el-icon-top', accent: 'emerald' },
        { key: 'down', title: '可下调容量', value: kpis.downAdjustable, unit: 'MW', icon: 'el-icon-bottom', accent: 'orange' },
        { key: 'max24h', title: '24h最大可调', value: kpis.max24h, unit: 'MW', icon: 'el-icon-data-line', accent: 'blue' }
      ]
    },

    stationRows() {
      return (this.viewData && this.viewData.stationTable) || []
    }
  },

  watch: {
    viewData: {
      deep: true,
      handler() {
        this.$nextTick(() => {
          this.renderCapacityChart()
          this.renderTimelineChart()
        })
      }
    }
  },

  mounted() {
    this._handleResize = this.handleResize.bind(this)
    window.addEventListener('resize', this._handleResize)
    this.$nextTick(() => {
      this.renderCapacityChart()
      this.renderTimelineChart()
    })
  },

  beforeDestroy() {
    window.removeEventListener('resize', this._handleResize)
    if (this._capacityChart) {
      this._capacityChart.dispose()
      this._capacityChart = null
    }
    if (this._timelineChart) {
      this._timelineChart.dispose()
      this._timelineChart = null
    }
  },

  methods: {
    handleResize() {
      if (this._capacityChart) {
        this._capacityChart.resize()
      }
      if (this._timelineChart) {
        this._timelineChart.resize()
      }
    },

    renderCapacityChart() {
      const curve = this.viewData && this.viewData.capacityCurve
      if (!curve) return

      const el = this.$refs.capacityChart
      if (!el) return

      if (!this._capacityChart) {
        this._capacityChart = echarts.init(el)
      }

      const textColor = 'rgba(255,255,255,0.65)'
      const splitLineColor = 'rgba(255,255,255,0.08)'

      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(0,0,0,0.75)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: { color: textColor }
        },
        legend: {
          data: ['预测值', '上界', '下界'],
          textStyle: { color: textColor },
          top: 8
        },
        grid: {
          top: 50,
          right: 24,
          bottom: 36,
          left: 60
        },
        xAxis: {
          type: 'category',
          data: curve.timeLabels,
          axisLabel: { color: textColor },
          axisLine: { lineStyle: { color: splitLineColor } },
          axisTick: { lineStyle: { color: splitLineColor } }
        },
        yAxis: {
          type: 'value',
          name: 'MW',
          nameTextStyle: { color: textColor },
          axisLabel: { color: textColor },
          splitLine: { lineStyle: { color: splitLineColor } },
          axisLine: { lineStyle: { color: splitLineColor } }
        },
        series: [
          {
            name: '上界',
            type: 'line',
            data: curve.upperBound,
            symbol: 'none',
            lineStyle: { color: '#67C23A', type: [5, 3] },
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '下界',
            type: 'line',
            data: curve.lowerBound,
            symbol: 'none',
            lineStyle: { color: '#E6A23C', type: [5, 3] },
            itemStyle: { color: '#E6A23C' }
          },
          {
            name: '预测值',
            type: 'line',
            data: curve.predicted,
            symbol: 'none',
            lineStyle: { color: '#06a299', width: 2 },
            itemStyle: { color: '#06a299' },
            areaStyle: {
              color: 'rgba(6,162,153,0.15)',
              origin: 'auto'
            }
          }
        ]
      }

      this._capacityChart.setOption(option, true)
    },

    renderTimelineChart() {
      const timeline = this.viewData && this.viewData.timeline
      if (!timeline || !timeline.length) return

      const el = this.$refs.timelineChart
      if (!el) return

      if (!this._timelineChart) {
        this._timelineChart = echarts.init(el)
      }

      const textColor = 'rgba(255,255,255,0.65)'
      const splitLineColor = 'rgba(255,255,255,0.08)'
      const stationNames = timeline.map(function(s) { return s.name })

      var seriesMap = {
        available: [],
        dispatching: [],
        unavailable: []
      }

      timeline.forEach(function(station, yIdx) {
        if (!station.windows) return
        station.windows.forEach(function(w) {
          var parts = w.start.split(':')
          var startH = parseFloat(parts[0]) + parseFloat(parts[1]) / 60
          var endParts = w.end.split(':')
          var endH = parseFloat(endParts[0]) + parseFloat(endParts[1]) / 60
          var bucket = seriesMap[w.status]
          if (bucket) {
            bucket.push([startH, endH, yIdx])
          }
        })
      })

      function createRenderItem() {
        return function(params, api) {
          var startVal = api.value(0)
          var endVal = api.value(1)
          var yIndex = api.value(2)
          var start = api.coord([startVal, yIndex])
          var end = api.coord([endVal, yIndex])
          var bandSize = api.size([0, 1])
          var height = bandSize[1] * 0.6
          var rectShape = echarts.graphic.clipRectByRect(
            {
              x: start[0],
              y: start[1] - height / 2,
              width: end[0] - start[0],
              height: height
            },
            {
              x: params.coordSys.x,
              y: params.coordSys.y,
              width: params.coordSys.width,
              height: params.coordSys.height
            }
          )
          return rectShape && {
            type: 'rect',
            shape: rectShape,
            style: api.style()
          }
        }
      }

      var seriesList = [
        {
          name: '可用',
          type: 'custom',
          renderItem: createRenderItem(),
          itemStyle: { color: '#67C23A' },
          encode: { x: [0, 1], y: 2 },
          data: seriesMap.available
        },
        {
          name: '调度中',
          type: 'custom',
          renderItem: createRenderItem(),
          itemStyle: { color: '#06a299' },
          encode: { x: [0, 1], y: 2 },
          data: seriesMap.dispatching
        },
        {
          name: '不可用',
          type: 'custom',
          renderItem: createRenderItem(),
          itemStyle: { color: 'rgba(255,255,255,0.15)' },
          encode: { x: [0, 1], y: 2 },
          data: seriesMap.unavailable
        }
      ]

      var option = {
        backgroundColor: 'transparent',
        tooltip: {
          backgroundColor: 'rgba(0,0,0,0.75)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: { color: textColor },
          formatter: function(params) {
            if (!params || !params.value) return ''
            var start = params.value[0]
            var end = params.value[1]
            var sH = Math.floor(start)
            var sM = Math.round((start - sH) * 60)
            var eH = Math.floor(end)
            var eM = Math.round((end - eH) * 60)
            function pad(n) { return n < 10 ? '0' + n : '' + n }
            return params.seriesName + '<br/>' +
              pad(sH) + ':' + pad(sM) + ' - ' + pad(eH) + ':' + pad(eM)
          }
        },
        legend: {
          data: ['可用', '调度中', '不可用'],
          textStyle: { color: textColor },
          top: 8
        },
        grid: {
          top: 50,
          right: 24,
          bottom: 36,
          left: 120
        },
        xAxis: {
          type: 'value',
          min: 0,
          max: 24,
          axisLabel: {
            color: textColor,
            formatter: function(val) {
              return val < 10 ? '0' + val + ':00' : val + ':00'
            }
          },
          axisLine: { lineStyle: { color: splitLineColor } },
          splitLine: { lineStyle: { color: splitLineColor } }
        },
        yAxis: {
          type: 'category',
          data: stationNames,
          inverse: true,
          axisLabel: { color: textColor },
          axisLine: { lineStyle: { color: splitLineColor } },
          axisTick: { show: false }
        },
        series: seriesList
      }

      this._timelineChart.setOption(option, true)
    }
  }
}
</script>

<style lang="less" scoped>
.forecast-adjustable-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.forecast-adjustable-view__kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.forecast-adjustable-view__chart {
  width: 100%;
  min-height: 520px;
}

@media (max-width: 1280px) {
  .forecast-adjustable-view__kpis {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
