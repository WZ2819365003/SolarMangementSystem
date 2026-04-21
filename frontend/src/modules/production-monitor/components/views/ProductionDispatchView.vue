<template>
  <div v-loading="loading" class="production-view" data-testid="production-dispatch-view">
    <section class="pv-card-grid">
      <app-metric-card
        v-for="item in viewData.summary || []"
        :key="item.key"
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :helper="item.helper"
        :icon="item.icon"
        :accent="item.accent"
      />
    </section>

    <section class="pv-split-grid production-dispatch-view__top-grid">
      <dispatch-trend-panel :trend-data="viewData.executionTrend || {}" />
      <app-section-card
        class="production-dispatch-view__risk-card"
        title="响应表现与风险提示"
        subtitle="围绕指令偏差、天气扰动和聚合约束做简明判断"
      >
        <ul class="pv-list">
          <li v-for="item in viewData.riskHints || []" :key="item.title" class="pv-list__item">
            <div>
              <div class="pv-list__title">
                {{ item.title }}
              </div>
              <div class="pv-list__meta">
                {{ item.description }}
              </div>
            </div>
            <el-tag size="mini" effect="dark" :type="item.tagType">
              {{ item.level }}
            </el-tag>
          </li>
        </ul>
      </app-section-card>
    </section>

    <app-section-card
      class="pv-data-panel"
      title="调度执行记录"
      subtitle="采用统一深色表格承接执行明细，降低页面割裂感"
    >
      <div class="pv-table-shell" data-testid="dispatch-record-table">
        <el-table :data="pagedRecords" size="mini" style="width: 100%;">
          <el-table-column prop="issuedAt" label="指令时间" min-width="140" />
          <el-table-column prop="commandType" label="指令类型" min-width="100" />
          <el-table-column prop="targetPowerMw" label="目标功率(MW)" min-width="120" align="right" />
          <el-table-column prop="actualPowerMw" label="实际响应(MW)" min-width="120" align="right" />
          <el-table-column prop="responseSeconds" label="响应时长(s)" min-width="110" align="right" />
          <el-table-column prop="status" label="执行状态" min-width="100" align="center">
            <template slot-scope="{ row }">
              <el-tag size="mini" effect="dark" :type="resolveTagType(row.status)">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deviationReason" label="偏差说明" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" min-width="70" align="center">
            <template slot-scope="{ row }">
              <el-button type="text" size="mini" @click="openDispatchCurve(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          small
          background
          layout="total, sizes, prev, pager, next"
          :page-sizes="[8, 15, 30]"
          :page-size="pageSize"
          :current-page="currentPage"
          :total="(viewData.records || []).length"
          style="margin-top: 12px; text-align: right;"
          @size-change="val => { pageSize = val; currentPage = 1 }"
          @current-change="val => { currentPage = val }"
        />
      </div>
    </app-section-card>

    <!-- Dispatch Curve Drawer -->
    <el-drawer
      :visible.sync="curveVisible"
      :title="curveTitle"
      direction="rtl"
      size="55%"
      append-to-body
      :close-on-click-modal="true"
      custom-class="dispatch-curve-drawer"
    >
      <div class="dispatch-curve-drawer__body">
        <div class="dispatch-curve-drawer__kpis">
          <div class="dispatch-curve-drawer__kpi" v-for="kpi in curveKpis" :key="kpi.key">
            <span class="dispatch-curve-drawer__kpi-label">{{ kpi.label }}</span>
            <span class="dispatch-curve-drawer__kpi-value">{{ kpi.value }} <small>{{ kpi.unit }}</small></span>
          </div>
        </div>
        <div ref="curveChart" class="dispatch-curve-drawer__chart" />
        <div class="dispatch-curve-drawer__metrics">
          <div class="dispatch-curve-drawer__metric" v-for="m in curveMetrics" :key="m.key">
            <span class="dispatch-curve-drawer__metric-label">{{ m.label }}</span>
            <span class="dispatch-curve-drawer__metric-value" :style="{ color: m.color }">{{ m.value }}</span>
            <span class="dispatch-curve-drawer__metric-unit">{{ m.unit }}</span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import { resolveTagType } from '@/utils/formatters'
import DispatchTrendPanel from '../shared/DispatchTrendPanel.vue'

export default {
  name: 'ProductionDispatchView',
  components: {
    AppMetricCard,
    AppSectionCard,
    DispatchTrendPanel
  },
  props: {
    viewData: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      currentPage: 1,
      pageSize: 8,
      curveVisible: false,
      curveTitle: '',
      curveKpis: [],
      curveMetrics: [],
      curveRow: null,
      curveChart: null
    }
  },
  computed: {
    pagedRecords() {
      var list = this.viewData.records || []
      var start = (this.currentPage - 1) * this.pageSize
      return list.slice(start, start + this.pageSize)
    }
  },
  watch: {
    curveVisible(val) {
      if (val) {
        this.$nextTick(() => this.renderCurveChart())
      } else {
        if (this.curveChart) { this.curveChart.dispose(); this.curveChart = null }
      }
    }
  },
  beforeDestroy() {
    if (this.curveChart) { this.curveChart.dispose(); this.curveChart = null }
  },
  methods: {
    resolveTagType,
    openDispatchCurve(row) {
      this.curveRow = row
      this.curveTitle = row.issuedAt + ' — 目标功率 vs 实际响应曲线'
      this.curveKpis = [
        { key: 'target', label: '目标功率', value: row.targetPowerMw, unit: 'MW' },
        { key: 'actual', label: '实际响应', value: row.actualPowerMw, unit: 'MW' },
        { key: 'resp', label: '响应时长', value: row.responseSeconds, unit: 's' },
        { key: 'status', label: '执行状态', value: row.status, unit: '' }
      ]
      // Compute response quality metrics
      var deviation = row.targetPowerMw > 0
        ? Math.abs(((row.actualPowerMw - row.targetPowerMw) / row.targetPowerMw) * 100).toFixed(1)
        : '0.0'
      var deviationNum = parseFloat(deviation)
      this.curveMetrics = [
        { key: 'deviation', label: '功率偏差率', value: deviation, unit: '%', color: deviationNum > 5 ? '#f56c6c' : '#67c23a' },
        { key: 'response', label: '响应达标', value: row.responseSeconds <= 120 ? '达标' : '超时', unit: '', color: row.responseSeconds <= 120 ? '#67c23a' : '#f56c6c' },
        { key: 'type', label: '指令类型', value: row.commandType, unit: '', color: 'rgba(255,255,255,0.78)' },
        { key: 'reason', label: '偏差原因', value: row.deviationReason || '无', unit: '', color: 'rgba(255,255,255,0.58)' }
      ]
      this.curveVisible = true
    },
    renderCurveChart() {
      if (!this.$refs.curveChart) return
      if (this.curveChart) this.curveChart.dispose()
      var row = this.curveRow
      if (!row) return
      this.curveChart = echarts.init(this.$refs.curveChart)
      // Build 24-slot (1-hour before + 1-hour after = 2h) simulated curve around dispatch time
      var slots = 24
      var target = parseFloat(row.targetPowerMw) || 0
      var actual = parseFloat(row.actualPowerMw) || 0
      var rampSlots = Math.min(Math.ceil((row.responseSeconds || 60) / 300), 6)
      var targetData = []
      var actualData = []
      var times = []
      for (var i = 0; i < slots; i++) {
        var label = 'T' + (i < 12 ? '-' + (12 - i) + 'm' : '+' + (i - 12) + 'm')
        times.push(label)
        // Before dispatch: baseline ~60% of target
        if (i < 12) {
          targetData.push(+(target * 0.6).toFixed(2))
          actualData.push(+(target * 0.6 * (0.95 + Math.random() * 0.1)).toFixed(2))
        } else if (i < 12 + rampSlots) {
          // Ramping phase
          var progress = (i - 12) / rampSlots
          targetData.push(+(target * (0.6 + 0.4 * progress)).toFixed(2))
          actualData.push(+(actual * (0.6 + 0.4 * progress) * (0.92 + Math.random() * 0.12)).toFixed(2))
        } else {
          // Steady state
          targetData.push(+target.toFixed(2))
          actualData.push(+(actual * (0.97 + Math.random() * 0.06)).toFixed(2))
        }
      }
      this.curveChart.setOption({
        backgroundColor: 'transparent',
        grid: { left: 60, right: 30, top: 50, bottom: 50 },
        legend: {
          data: ['目标功率', '实际响应'],
          top: 8,
          textStyle: { color: 'rgba(255,255,255,0.65)', fontSize: 12 },
          icon: 'roundRect', itemWidth: 14, itemHeight: 3
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(6,20,48,0.95)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: { color: '#fff', fontSize: 12 },
          formatter: function (params) {
            return params[0].name + '<br/>' + params.map(function (p) {
              return p.marker + p.seriesName + ': ' + p.value + ' MW'
            }).join('<br/>')
          }
        },
        xAxis: {
          type: 'category', data: times,
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
          axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 10, interval: 3 },
          splitLine: { show: false }
        },
        yAxis: {
          type: 'value', name: 'MW',
          nameTextStyle: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          axisLine: { show: false },
          axisLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 11 },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
        },
        series: [
          {
            name: '目标功率', type: 'line', data: targetData, smooth: false, symbol: 'none',
            lineStyle: { width: 2, type: 'dashed', color: '#ffd166' },
            itemStyle: { color: '#ffd166' }
          },
          {
            name: '实际响应', type: 'line', data: actualData, smooth: true, symbol: 'none',
            lineStyle: { width: 2, color: '#06d6a0' },
            areaStyle: { opacity: 0.1, color: '#06d6a0' },
            itemStyle: { color: '#06d6a0' }
          }
        ]
      })
    }
  }
}
</script>

<style lang="less" scoped>
.production-dispatch-view__top-grid {
  grid-template-columns: minmax(0, 1.48fr) minmax(320px, 0.96fr);
  align-items: stretch;
}

.production-dispatch-view__risk-card {
  height: 100%;
}

@media (max-width: 1280px) {
  .production-dispatch-view__top-grid {
    grid-template-columns: 1fr;
  }
}

// Dispatch curve drawer
/deep/ .dispatch-curve-drawer {
  background: rgba(9, 29, 67, 0.98) !important;
  .el-drawer__header {
    color: rgba(255, 255, 255, 0.92);
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    padding: 16px 20px;
    margin-bottom: 0;
  }
  .el-drawer__close-btn { color: rgba(255, 255, 255, 0.5); }
  .el-drawer__body { padding: 0; overflow: hidden; }
}

.dispatch-curve-drawer__body {
  padding: 20px 24px;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

.dispatch-curve-drawer__kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.dispatch-curve-drawer__kpi {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 16px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.dispatch-curve-drawer__kpi-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.58);
}

.dispatch-curve-drawer__kpi-value {
  font-size: 18px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.92);
  small { font-size: 12px; font-weight: 400; color: rgba(255, 255, 255, 0.5); }
}

.dispatch-curve-drawer__chart {
  width: 100%;
  height: 340px;
  margin-bottom: 16px;
}

.dispatch-curve-drawer__metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.dispatch-curve-drawer__metric {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.dispatch-curve-drawer__metric-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  min-width: 70px;
}

.dispatch-curve-drawer__metric-value {
  font-size: 14px;
  font-weight: 600;
}

.dispatch-curve-drawer__metric-unit {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}
</style>
