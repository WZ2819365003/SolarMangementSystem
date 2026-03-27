<template>
  <div class="resource-overview-view">
    <!-- KPI cards -->
    <div v-if="data && data.kpis" class="resource-overview-view__kpis">
      <app-metric-card
        v-for="item in data.kpis"
        :key="item.key"
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :icon="item.icon"
        :accent="item.accent"
        :helper="''"
      />
    </div>

    <!-- Member stations table -->
    <app-section-card title="成员电站" :subtitle="data ? data.name : ''">
      <el-table v-if="data && data.stations" :data="data.stations" size="mini" stripe @row-click="handleRowClick" class="resource-overview-view__table">
        <el-table-column prop="name" label="电站名称" min-width="180" />
        <el-table-column prop="capacityMw" label="容量(MW)" width="100" />
        <el-table-column prop="weight" label="权重" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="resolveStatusType(row.status)" size="mini">{{ resolveStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="onlineRate" label="在线率(%)" width="100" />
        <el-table-column prop="alarmCount" label="告警数" width="80" />
        <el-table-column prop="adjustableCapacityMw" label="可调容量(MW)" width="120" />
      </el-table>
    </app-section-card>

    <!-- Power curve -->
    <app-section-card title="聚合功率曲线">
      <div ref="chart" class="resource-overview-view__chart" />
    </app-section-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'

const STATUS_TYPE_MAP = {
  normal: 'success',
  warning: 'warning',
  fault: 'danger',
  offline: 'info',
  maintenance: ''
}

const STATUS_LABEL_MAP = {
  normal: '正常',
  warning: '告警',
  fault: '故障',
  offline: '离线',
  maintenance: '检修'
}

const CHART_COLORS = ['#1a8dff', '#06d6a0', '#ffd166', '#ef476f', '#118ab2', '#073b4c', '#ff6b6b', '#4ecdc4']

export default {
  name: 'ResourceOverviewView',
  components: {
    AppMetricCard,
    AppSectionCard
  },
  props: {
    data: {
      type: Object,
      default: () => ({})
    }
  },
  watch: {
    'data.powerCurve': {
      handler: 'renderChart',
      deep: true
    }
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
    resolveStatusType(status) {
      return STATUS_TYPE_MAP[status] !== undefined ? STATUS_TYPE_MAP[status] : 'info'
    },
    resolveStatusLabel(status) {
      return STATUS_LABEL_MAP[status] || status || '--'
    },
    handleRowClick(row) {
      this.$emit('station-click', row)
    },
    renderChart() {
      if (!this.$refs.chart) return
      const powerCurve = this.data && this.data.powerCurve
      if (!powerCurve || !powerCurve.times || !powerCurve.series) return

      if (!this.chart) {
        this.chart = echarts.init(this.$refs.chart)
      }

      const { times, series } = powerCurve

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
          itemStyle: { color: CHART_COLORS[idx % CHART_COLORS.length] },
          barMaxWidth: isBar ? 20 : undefined
        }
      })

      const option = {
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
          name: 'MW',
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

      this.chart.setOption(option, true)
    }
  }
}
</script>

<style lang="less" scoped>
.resource-overview-view {
  width: 100%;

  &__kpis {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 20px;
  }

  &__chart {
    width: 100%;
    min-height: 300px;
  }

  &__table {
    /deep/ .el-table__row {
      cursor: pointer;
    }
  }

  .app-section-card {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  /deep/ .el-table {
    background-color: transparent;
    color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.75));

    &::before {
      background-color: rgba(255, 255, 255, 0.06);
    }

    th {
      background-color: rgba(255, 255, 255, 0.04);
      color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
      border-bottom: 1px solid rgba(255, 255, 255, 0.06);
      font-size: 12px;
    }

    tr {
      background-color: transparent;
    }

    td {
      border-bottom: 1px solid rgba(255, 255, 255, 0.04);
      font-size: 12px;
    }

    .el-table__row--striped td {
      background-color: rgba(255, 255, 255, 0.02);
    }

    .el-table__row:hover > td {
      background-color: rgba(24, 144, 255, 0.08);
    }

    .el-table__empty-block {
      background-color: transparent;
    }
  }

  /deep/ .el-tag {
    border-radius: 3px;
    font-size: 11px;
    height: 22px;
    line-height: 20px;
    padding: 0 8px;
    background: transparent;

    &--success {
      color: #67c23a;
      border-color: rgba(103, 194, 58, 0.4);
    }

    &--warning {
      color: #e6a23c;
      border-color: rgba(230, 162, 60, 0.4);
    }

    &--danger {
      color: #f56c6c;
      border-color: rgba(245, 108, 108, 0.4);
    }

    &--info {
      color: rgba(255, 255, 255, 0.55);
      border-color: rgba(255, 255, 255, 0.2);
    }
  }
}
</style>
