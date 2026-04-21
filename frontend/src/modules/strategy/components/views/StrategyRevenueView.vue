<template>
  <div v-loading="loading" class="strategy-revenue-view" data-testid="strategy-revenue-view">
    <section class="pv-card-grid strategy-revenue-view__kpis">
      <app-metric-card
        v-for="item in kpiCards"
        :key="item.key"
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :helper="item.helper"
        :icon="item.icon"
        :accent="item.accent"
      />
    </section>

    <app-section-card title="收益趋势" subtitle="趋势按后端收益表汇总，不再依赖前端 mock 曲线">
      <div ref="trendChart" class="strategy-revenue-view__chart" />
    </app-section-card>

    <div class="strategy-revenue-view__grid">
      <app-section-card title="收益明细" subtitle="按策略、日期、电站维度拆分收益构成">
        <el-table :data="detailItems" size="mini" stripe>
          <el-table-column prop="date" label="日期" width="110" />
          <el-table-column prop="strategyName" label="策略" min-width="180" />
          <el-table-column prop="stationName" label="电站" min-width="140" />
          <el-table-column prop="actualRevenue" label="实际收益(¥)" width="120" align="right" />
          <el-table-column prop="estimatedRevenue" label="预估收益(¥)" width="120" align="right" />
          <el-table-column prop="peakSaving" label="削峰收益(¥)" width="120" align="right" />
          <el-table-column prop="responseReward" label="响应奖励(¥)" width="120" align="right" />
          <el-table-column prop="penalty" label="罚金(¥)" width="120" align="right" />
        </el-table>
      </app-section-card>

      <app-section-card title="策略对比" subtitle="对比结果基于后端收益明细汇总，便于后续替换成真实结算数据">
        <div ref="compareChart" class="strategy-revenue-view__compare-chart" />

        <el-table :data="compareItems" size="mini" stripe>
          <el-table-column prop="name" label="策略" min-width="160" />
          <el-table-column prop="stationName" label="电站" min-width="140" />
          <el-table-column prop="totalRevenue" label="累计收益(¥)" width="120" align="right" />
          <el-table-column prop="estimatedRevenue" label="累计预估(¥)" width="120" align="right" />
          <el-table-column prop="achievementRate" label="达成率(%)" width="120" align="right" />
        </el-table>

        <p class="strategy-revenue-view__delta">
          两组策略累计收益差值：{{ compare.totalDelta || 0 }} ¥
        </p>
      </app-section-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'

var CHART_TEXT_COLOR = 'rgba(255,255,255,0.65)'
var AXIS_LINE_COLOR = 'rgba(255,255,255,0.15)'
var SPLIT_LINE_COLOR = 'rgba(255,255,255,0.06)'

export default {
  name: 'StrategyRevenueView',
  components: {
    AppMetricCard,
    AppSectionCard
  },
  props: {
    viewData: {
      type: Object,
      default: function () {
        return {}
      }
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      trendInstance: null,
      compareInstance: null
    }
  },
  computed: {
    summary() {
      return this.viewData.summary || {}
    },
    detail() {
      return this.viewData.detail || {}
    },
    compare() {
      return this.viewData.compare || {}
    },
    kpiCards() {
      var kpi = this.summary.kpi || {}
      return [
        { key: 'today', title: '当日收益', value: kpi.todayRevenue || 0, unit: '¥', helper: '汇总当日实际收益', icon: 'el-icon-money', accent: 'teal' },
        { key: 'estimated', title: '当日预估', value: kpi.todayEstimatedRevenue || 0, unit: '¥', helper: '汇总当日预估收益', icon: 'el-icon-data-line', accent: 'blue' },
        { key: 'achievement', title: '收益达成率', value: kpi.revenueAchievementRate || 0, unit: '%', helper: '实际收益 / 预估收益', icon: 'el-icon-finished', accent: 'emerald' },
        { key: 'count', title: '策略数量', value: kpi.strategyCount || 0, unit: '个', helper: '参与当日收益统计的策略数', icon: 'el-icon-s-management', accent: 'orange' }
      ]
    },
    detailItems() {
      return this.detail.items || []
    },
    compareItems() {
      return this.compare.items || []
    }
  },
  watch: {
    viewData: {
      deep: true,
      handler() {
        var self = this
        self.$nextTick(function () {
          self.renderTrendChart()
          self.renderCompareChart()
        })
      }
    }
  },
  mounted() {
    this.handleResize = this._handleResize.bind(this)
    window.addEventListener('resize', this.handleResize)
    this.$nextTick(() => {
      this.renderTrendChart()
      this.renderCompareChart()
    })
  },
  beforeDestroy() {
    if (this.trendInstance) {
      this.trendInstance.dispose()
      this.trendInstance = null
    }
    if (this.compareInstance) {
      this.compareInstance.dispose()
      this.compareInstance = null
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    _handleResize() {
      if (this.trendInstance) {
        this.trendInstance.resize()
      }
      if (this.compareInstance) {
        this.compareInstance.resize()
      }
    },
    renderTrendChart() {
      if (!this.$refs.trendChart) return
      if (!this.trendInstance) {
        this.trendInstance = echarts.init(this.$refs.trendChart)
      }
      var trend = this.summary.trend || {}
      this.trendInstance.setOption({
        backgroundColor: 'transparent',
        textStyle: { color: CHART_TEXT_COLOR },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(0,0,0,0.75)',
          borderColor: 'rgba(255,255,255,0.1)',
          textStyle: { color: CHART_TEXT_COLOR }
        },
        legend: {
          top: 6,
          textStyle: { color: CHART_TEXT_COLOR }
        },
        grid: {
          top: 60,
          right: 20,
          bottom: 30,
          left: 60
        },
        xAxis: {
          type: 'category',
          data: trend.dates || [],
          axisLabel: { color: CHART_TEXT_COLOR },
          axisLine: { lineStyle: { color: AXIS_LINE_COLOR } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: CHART_TEXT_COLOR },
          axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
          splitLine: { lineStyle: { color: SPLIT_LINE_COLOR } }
        },
        series: [
          {
            name: '预估收益',
            type: 'line',
            smooth: true,
            symbol: 'none',
            data: trend.estimatedRevenue || [],
            lineStyle: { color: '#1a8dff' },
            itemStyle: { color: '#1a8dff' }
          },
          {
            name: '实际收益',
            type: 'line',
            smooth: true,
            symbol: 'none',
            data: trend.actualRevenue || [],
            lineStyle: { color: '#00b578' },
            itemStyle: { color: '#00b578' },
            areaStyle: { color: 'rgba(0,181,120,0.08)' }
          }
        ]
      }, true)
    },
    renderCompareChart() {
      if (!this.$refs.compareChart) return
      if (!this.compareInstance) {
        this.compareInstance = echarts.init(this.$refs.compareChart)
      }
      var names = this.compareItems.map(function (item) { return item.name })
      var values = this.compareItems.map(function (item) { return item.totalRevenue })
      this.compareInstance.setOption({
        backgroundColor: 'transparent',
        textStyle: { color: CHART_TEXT_COLOR },
        grid: {
          top: 10,
          right: 20,
          bottom: 30,
          left: 60
        },
        xAxis: {
          type: 'category',
          data: names,
          axisLabel: { color: CHART_TEXT_COLOR, interval: 0 },
          axisLine: { lineStyle: { color: AXIS_LINE_COLOR } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: CHART_TEXT_COLOR },
          axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
          splitLine: { lineStyle: { color: SPLIT_LINE_COLOR } }
        },
        series: [
          {
            type: 'bar',
            data: values,
            itemStyle: {
              color: function (params) {
                return params.dataIndex === 0 ? '#f59b23' : '#1a8dff'
              }
            }
          }
        ]
      }, true)
    }
  }
}
</script>

<style lang="less" scoped>
.strategy-revenue-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.strategy-revenue-view__chart {
  height: 320px;
}

.strategy-revenue-view__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(360px, 0.85fr);
  gap: 16px;
}

.strategy-revenue-view__compare-chart {
  height: 220px;
  margin-bottom: 12px;
}

.strategy-revenue-view__delta {
  margin: 14px 0 0;
  color: var(--pvms-text-primary);
  font-size: 13px;
}

@media (max-width: 1480px) {
  .strategy-revenue-view__grid {
    grid-template-columns: 1fr;
  }
}
</style>
