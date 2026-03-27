<template>
  <div class="production-load-view">
    <!-- KPI Dashboard -->
    <div class="production-load-view__kpi-board">
      <div class="production-load-view__kpi-item" v-for="kpi in kpiList" :key="kpi.key">
        <span class="production-load-view__kpi-label">{{ kpi.label }}</span>
        <span class="production-load-view__kpi-value">
          {{ kpi.value }}
          <span class="production-load-view__kpi-unit">{{ kpi.unit }}</span>
        </span>
      </div>
    </div>

    <!-- Filter bar -->
    <div class="production-load-view__filter">
      <el-input
        v-model="searchKeyword"
        size="mini"
        placeholder="搜索电站名称..."
        prefix-icon="el-icon-search"
        clearable
        style="width: 220px;"
      />
      <el-select v-model="statusFilter" size="mini" placeholder="全部状态" clearable style="width: 120px;">
        <el-option label="全部" value="" />
        <el-option label="正常" value="normal" />
        <el-option label="告警" value="warning" />
        <el-option label="故障" value="fault" />
        <el-option label="离线" value="offline" />
      </el-select>
      <el-button size="mini" icon="el-icon-refresh" @click="$emit('refresh')">刷新</el-button>
    </div>

    <!-- Station table -->
    <div class="production-load-view__table-wrap">
      <el-table
        :data="filteredStations"
        size="mini"
        stripe
        :header-cell-style="headerCellStyle"
        style="width: 100%;"
      >
        <el-table-column prop="name" label="电站名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="capacityKwp" label="装机容量(kWp)" min-width="130" align="right" />
        <el-table-column prop="realtimePowerKw" label="实时功率(kW)" min-width="130" align="right">
          <template slot-scope="{ row }">
            <span :class="{ 'is-highlight': row.realtimePowerKw > row.capacityKwp * 0.8 }">{{ row.realtimePowerKw }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="loadKw" label="负荷(kW)" min-width="120" align="right" />
        <el-table-column prop="adjustableKw" label="可调功率(kW)" min-width="130" align="right">
          <template slot-scope="{ row }">
            <span :style="{ color: row.adjustableKw > 0 ? '#06d6a0' : '#ef476f' }">{{ row.adjustableKw }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxRampRate" label="最大爬坡(kW/min)" min-width="140" align="right" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="resolveStatusType(row.status)" size="mini">{{ resolveStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click="openGridInteraction(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Grid Interaction Drawer -->
    <el-drawer
      :visible.sync="dialogVisible"
      :title="dialogTitle"
      direction="rtl"
      size="55%"
      append-to-body
      custom-class="production-load-view__drawer"
      :close-on-click-modal="true"
    >
      <div class="production-load-view__drawer-body">
        <div class="production-load-view__dialog-kpis">
          <div class="production-load-view__dialog-kpi" v-for="kpi in dialogKpis" :key="kpi.key">
            <span class="production-load-view__dialog-kpi-label">{{ kpi.label }}</span>
            <span class="production-load-view__dialog-kpi-value">{{ kpi.value }} <small>{{ kpi.unit }}</small></span>
          </div>
        </div>
        <div ref="dialogChart" class="production-load-view__dialog-chart" />
      </div>
    </el-drawer>
  </div>
</template>

<script>
import * as echarts from 'echarts'

const STATUS_TYPE_MAP = { normal: 'success', warning: 'warning', fault: 'danger', offline: 'info', maintenance: '' }
const STATUS_LABEL_MAP = { normal: '正常', warning: '告警', fault: '故障', offline: '离线', maintenance: '检修' }

export default {
  name: 'ProductionLoadView',
  props: {
    viewData: { type: Object, default: () => ({}) },
    loading: { type: Boolean, default: false }
  },
  data() {
    return {
      searchKeyword: '',
      statusFilter: '',
      dialogVisible: false,
      dialogTitle: '',
      dialogKpis: [],
      dialogChartData: null,
      chart: null,
      headerCellStyle: {
        background: 'rgba(255, 255, 255, 0.04)',
        color: 'rgba(255, 255, 255, 0.58)',
        borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
        fontSize: '12px'
      }
    }
  },
  computed: {
    kpiList() {
      var d = this.viewData
      if (!d || !d.summary) return []
      var s = d.summary
      return [
        { key: 'totalLoad', label: '总负荷', value: s.totalLoadMw, unit: 'MW' },
        { key: 'totalPvOutput', label: '总光伏出力', value: s.totalPvOutputMw, unit: 'MW' },
        { key: 'totalAdjustable', label: '总可调空间', value: s.totalAdjustableMw, unit: 'MW' },
        { key: 'avgRampRate', label: '平均爬坡速率', value: s.avgRampRate, unit: 'kW/min' },
        { key: 'stationCount', label: '电站总数', value: s.stationCount, unit: '座' },
        { key: 'onlineCount', label: '在线电站', value: s.onlineCount, unit: '座' }
      ]
    },
    filteredStations() {
      var list = (this.viewData && this.viewData.stations) || []
      var kw = this.searchKeyword ? this.searchKeyword.toLowerCase() : ''
      var sf = this.statusFilter
      return list.filter(function (item) {
        if (kw && item.name.toLowerCase().indexOf(kw) === -1) return false
        if (sf && item.status !== sf) return false
        return true
      })
    }
  },
  watch: {
    dialogVisible(val) {
      if (val) {
        this.$nextTick(function () { this.renderDialogChart() }.bind(this))
      } else {
        if (this.chart) {
          this.chart.dispose()
          this.chart = null
        }
      }
    }
  },
  methods: {
    resolveStatusType(status) { return STATUS_TYPE_MAP[status] !== undefined ? STATUS_TYPE_MAP[status] : 'info' },
    resolveStatusLabel(status) { return STATUS_LABEL_MAP[status] || status || '--' },
    openGridInteraction(row) {
      this.dialogTitle = row.name + ' — 电网交互曲线'
      this.dialogKpis = [
        { key: 'load', label: '当前负荷', value: row.loadKw, unit: 'kW' },
        { key: 'pv', label: '光伏出力', value: row.realtimePowerKw, unit: 'kW' },
        { key: 'adj', label: '可调功率', value: row.adjustableKw, unit: 'kW' },
        { key: 'ramp', label: '爬坡速率', value: row.maxRampRate, unit: 'kW/min' }
      ]
      this.dialogChartData = row.gridInteraction || null
      this.dialogVisible = true
    },
    renderDialogChart() {
      if (!this.$refs.dialogChart || !this.dialogChartData) return
      if (this.chart) this.chart.dispose()
      this.chart = echarts.init(this.$refs.dialogChart)
      var d = this.dialogChartData
      var colors = ['#1a8dff', '#06d6a0', '#ffd166', '#ef476f']
      var chartSeries = d.series.map(function (s, idx) {
        return {
          name: s.name,
          type: 'line',
          data: s.data,
          smooth: true,
          symbol: 'none',
          lineStyle: { width: 2 },
          areaStyle: s.area ? { opacity: 0.12 } : undefined,
          itemStyle: { color: colors[idx % colors.length] }
        }
      })
      this.chart.setOption({
        backgroundColor: 'transparent',
        grid: { left: 60, right: 30, top: 40, bottom: 70 },
        legend: {
          data: d.series.map(function (s) { return s.name }),
          bottom: 8,
          textStyle: { color: 'rgba(255,255,255,0.65)', fontSize: 12 },
          icon: 'roundRect', itemWidth: 14, itemHeight: 3
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(6, 20, 48, 0.95)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: { color: '#fff', fontSize: 12 }
        },
        xAxis: {
          type: 'category', data: d.times,
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
          axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          splitLine: { show: false }
        },
        yAxis: {
          type: 'value', name: 'kW',
          nameTextStyle: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          axisLine: { show: false },
          axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
        },
        dataZoom: [{
          type: 'slider', bottom: 30, height: 18,
          borderColor: 'rgba(255,255,255,0.1)',
          backgroundColor: 'rgba(255,255,255,0.03)',
          fillerColor: 'rgba(24,144,255,0.15)',
          handleStyle: { color: '#1890ff' },
          textStyle: { color: 'rgba(255,255,255,0.5)' }
        }],
        series: chartSeries
      })
    }
  },
  beforeDestroy() {
    if (this.chart) { this.chart.dispose(); this.chart = null }
  }
}
</script>

<style lang="less" scoped>
.production-load-view {
  width: 100%;

  &__kpi-board {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }

  &__kpi-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 16px 18px;
    border-radius: 4px;
    border: 1px solid rgba(255, 255, 255, 0.08);
    background: linear-gradient(180deg, rgba(14, 49, 106, 0.96), rgba(9, 29, 67, 0.98));
  }

  &__kpi-label {
    font-size: 12px;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
  }

  &__kpi-value {
    font-size: 22px;
    font-weight: 600;
    color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92));
  }

  &__kpi-unit {
    font-size: 12px;
    font-weight: 400;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
    margin-left: 4px;
  }

  &__filter {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 14px;
    padding: 12px 16px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.06);

    /deep/ .el-input__inner {
      background: rgba(255, 255, 255, 0.06);
      border-color: rgba(255, 255, 255, 0.12);
      color: rgba(255, 255, 255, 0.9);
      &::placeholder { color: rgba(255, 255, 255, 0.35); }
    }
    /deep/ .el-input__prefix { color: rgba(255, 255, 255, 0.4); }
    /deep/ .el-button {
      background: rgba(255, 255, 255, 0.06);
      border-color: rgba(255, 255, 255, 0.12);
      color: rgba(255, 255, 255, 0.78);
      &:hover { background: rgba(255, 255, 255, 0.1); color: #fff; }
    }
  }

  &__table-wrap {
    border-radius: 4px;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.06);

    /deep/ .el-table {
      background-color: transparent;
      color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.75));
      &::before { background-color: rgba(255, 255, 255, 0.06); }
      th { background-color: rgba(255, 255, 255, 0.04); color: rgba(255, 255, 255, 0.58); border-bottom: 1px solid rgba(255, 255, 255, 0.06); font-size: 12px; }
      tr { background-color: transparent; }
      td { border-bottom: 1px solid rgba(255, 255, 255, 0.04); font-size: 13px; }
      .el-table__row--striped td { background-color: rgba(255, 255, 255, 0.02); }
      .el-table__row:hover > td { background-color: rgba(24, 144, 255, 0.08); }
      .el-table__empty-block { background-color: transparent; }
      .el-table__fixed-right { background: transparent; }
    }

    .is-highlight { color: #ffd166; font-weight: 600; }

    /deep/ .el-tag {
      border-radius: 3px; font-size: 11px; height: 22px; line-height: 20px; padding: 0 8px; background: transparent;
      &--success { color: #67c23a; border-color: rgba(103, 194, 58, 0.4); }
      &--warning { color: #e6a23c; border-color: rgba(230, 162, 60, 0.4); }
      &--danger { color: #f56c6c; border-color: rgba(245, 108, 108, 0.4); }
      &--info { color: rgba(255, 255, 255, 0.55); border-color: rgba(255, 255, 255, 0.2); }
    }

    /deep/ .el-button--text {
      color: var(--pvms-primary, #06a299);
      &:hover { color: #1a8dff; }
    }
  }

  // Drawer dark theme
  &__drawer {
    /deep/ & {
      background: rgba(9, 29, 67, 0.98) !important;
      .el-drawer__header {
        color: rgba(255, 255, 255, 0.92);
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
        padding: 16px 20px;
        margin-bottom: 0;
      }
      .el-drawer__close-btn { color: rgba(255, 255, 255, 0.5); &:hover { color: #fff; } }
      .el-drawer__body { padding: 0; }
    }
  }

  &__drawer-body {
    padding: 20px 24px;
    height: 100%;
    overflow-y: auto;
  }

  &__dialog-kpis {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }

  &__dialog-kpi {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 12px 16px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.06);
  }

  &__dialog-kpi-label {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.58);
  }

  &__dialog-kpi-value {
    font-size: 18px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.92);
    small { font-size: 12px; font-weight: 400; color: rgba(255, 255, 255, 0.5); }
  }

  &__dialog-chart {
    width: 100%;
    height: calc(100% - 120px);
    min-height: 450px;
  }
}
</style>
