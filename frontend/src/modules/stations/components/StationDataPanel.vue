<template>
  <div class="station-data-panel">
    <!-- Empty state if no node selected -->
    <div v-if="!selectedNode" class="station-data-panel__empty">
      <i class="el-icon-monitor" />
      <p>请在左侧树中选择节点</p>
    </div>

    <template v-else>
      <station-data-header :node="selectedNode" />

      <!-- Company view -->
      <company-overview-view
        v-if="selectedNode.nodeType === 'company'"
        :data="companyData"
        @resource-click="handleResourceClick"
      />

      <!-- Station view -->
      <div v-if="selectedNode.nodeType === 'station'" class="station-data-panel__station">
        <!-- Station KPI Board -->
        <div v-if="stationKpis" class="station-data-panel__station-kpis">
          <div class="station-data-panel__station-kpi" v-for="kpi in stationKpiList" :key="kpi.key">
            <span class="station-data-panel__station-kpi-label">{{ kpi.label }}</span>
            <span class="station-data-panel__station-kpi-value">{{ kpi.value }} <small>{{ kpi.unit }}</small></span>
          </div>
        </div>

        <!-- Monthly VPP Statistics -->
        <div v-if="monthlyStatsData" class="station-data-panel__monthly-stats">
          <div class="station-data-panel__monthly-stats-title">本月 VPP 参与统计</div>
          <div class="station-data-panel__monthly-stats-grid">
            <div class="station-data-panel__monthly-stat" v-for="stat in monthlyStatsList" :key="stat.key">
              <span class="station-data-panel__monthly-stat-value">{{ stat.value }}<small>{{ stat.unit }}</small></span>
              <span class="station-data-panel__monthly-stat-label">{{ stat.label }}</span>
            </div>
          </div>
        </div>

        <!-- Toolbar -->
        <div class="station-data-panel__toolbar">
          <el-date-picker
            v-model="localDate"
            type="date"
            size="mini"
            value-format="yyyy-MM-dd"
            placeholder="选择日期"
            @change="handleDateChange"
          />
          <el-button-group>
            <el-button size="mini" icon="el-icon-arrow-left" @click="prevDay" />
            <el-button size="mini" icon="el-icon-arrow-right" @click="nextDay" />
          </el-button-group>
          <div class="station-data-panel__refresh-group">
            <span class="station-data-panel__refresh-label">刷新频率(秒)</span>
            <el-input-number
              v-model="localRefreshInterval"
              size="mini"
              :min="5"
              :max="300"
              :step="5"
            />
            <el-button size="mini" type="primary" @click="applyRefreshInterval">确定</el-button>
          </div>
          <el-button size="mini" icon="el-icon-refresh" @click="$emit('refresh')">刷新</el-button>
          <el-radio-group
            v-model="localGranularity"
            size="mini"
            @change="handleGranularityChange"
          >
            <el-radio-button label="15min">15分钟级</el-radio-button>
            <el-radio-button label="1min">分钟级</el-radio-button>
          </el-radio-group>
        </div>

        <!-- Data tabs -->
        <el-tabs v-model="localTab" type="card" @tab-click="handleTabClick">
          <el-tab-pane label="可调空间" name="adjustable" />
          <el-tab-pane label="光伏出力" name="pv-output" />
          <el-tab-pane label="负荷监控" name="load" />
          <el-tab-pane label="出力预测" name="forecast" />
          <el-tab-pane label="策略查看" name="strategy" />
        </el-tabs>

        <!-- Chart area (scrollable) -->
        <div class="station-data-panel__chart-area">
          <station-monitor-view
            v-if="localTab !== 'strategy'"
            :data="stationRealtimeData"
            :metric="localTab"
          />
          <station-strategy-view
            v-else
            :data="stationRealtimeData"
          />
        </div>
      </div>

      <!-- Inverter view -->
      <inverter-detail-view
        v-if="selectedNode.nodeType === 'inverter'"
        :data="inverterData"
      />
    </template>
  </div>
</template>

<script>
import StationDataHeader from './StationDataHeader.vue'
import CompanyOverviewView from './CompanyOverviewView.vue'
import InverterDetailView from './InverterDetailView.vue'
import StationMonitorView from './StationMonitorView.vue'
import StationStrategyView from './StationStrategyView.vue'

export default {
  name: 'StationDataPanel',
  components: {
    StationDataHeader,
    CompanyOverviewView,
    InverterDetailView,
    StationMonitorView,
    StationStrategyView
  },
  props: {
    selectedNode: {
      type: Object,
      default: null
    },
    companyData: {
      type: Object,
      default: () => ({})
    },
    stationRealtimeData: {
      type: Object,
      default: () => ({})
    },
    inverterData: {
      type: Object,
      default: null
    },
    activeTab: {
      type: String,
      default: 'active-power'
    },
    currentDate: {
      type: String,
      default: ''
    },
    granularity: {
      type: String,
      default: '15min'
    },
    refreshInterval: {
      type: Number,
      default: 30
    }
  },
  data() {
    return {
      localDate: this.currentDate,
      localTab: this.activeTab,
      localGranularity: this.granularity,
      localRefreshInterval: this.refreshInterval
    }
  },
  computed: {
    stationKpis() {
      return this.stationRealtimeData && this.stationRealtimeData.stationKpis
    },
    monthlyStatsData() {
      return this.stationRealtimeData && this.stationRealtimeData.monthlyStats
    },
    stationKpiList() {
      var k = this.stationKpis
      if (!k) return []
      return [
        { key: 'adj', label: '可调容量', value: k.adjustableCapacityKw, unit: 'kW' },
        { key: 'pvToday', label: '今日光伏收益', value: k.todayPvRevenue, unit: '元' },
        { key: 'dispToday', label: '今日调控收益', value: k.todayDispatchRevenue, unit: '元' },
        { key: 'pvMonth', label: '本月光伏收益', value: k.monthlyPvRevenue, unit: '元' },
        { key: 'dispMonth', label: '本月调控收益', value: k.monthlyDispatchRevenue, unit: '元' },
        { key: 'pvCum', label: '累计光伏收益', value: k.cumulativePvRevenue, unit: '元' },
        { key: 'dispCum', label: '累计调控收益', value: k.cumulativeDispatchRevenue, unit: '元' }
      ]
    },
    monthlyStatsList() {
      var m = this.monthlyStatsData
      if (!m) return []
      return [
        { key: 'respCount', label: '响应次数', value: m.responseCount, unit: '次' },
        { key: 'successRate', label: '响应成功率', value: m.responseSuccessRate, unit: '%' },
        { key: 'respEnergy', label: '响应电量', value: m.totalResponseEnergy, unit: 'kWh' },
        { key: 'avgDev', label: '平均偏差率', value: m.avgDeviationRate, unit: '%' },
        { key: 'peakShave', label: '调峰次数', value: m.peakShavingCount, unit: '次' },
        { key: 'freqReg', label: '调频次数', value: m.frequencyRegCount, unit: '次' }
      ]
    }
  },
  watch: {
    currentDate(val) {
      this.localDate = val
    },
    activeTab(val) {
      this.localTab = val
    },
    granularity(val) {
      this.localGranularity = val
    },
    refreshInterval(val) {
      this.localRefreshInterval = val
    }
  },
  methods: {
    prevDay() {
      if (!this.localDate) {
        return
      }
      var date = new Date(this.localDate)
      date.setDate(date.getDate() - 1)
      var year = date.getFullYear()
      var month = String(date.getMonth() + 1).padStart(2, '0')
      var day = String(date.getDate()).padStart(2, '0')
      this.localDate = year + '-' + month + '-' + day
      this.$emit('date-change', this.localDate)
    },
    nextDay() {
      if (!this.localDate) {
        return
      }
      var date = new Date(this.localDate)
      date.setDate(date.getDate() + 1)
      var year = date.getFullYear()
      var month = String(date.getMonth() + 1).padStart(2, '0')
      var day = String(date.getDate()).padStart(2, '0')
      this.localDate = year + '-' + month + '-' + day
      this.$emit('date-change', this.localDate)
    },
    handleDateChange(date) {
      this.$emit('date-change', date)
    },
    handleTabClick(tab) {
      this.$emit('tab-change', tab.name)
    },
    handleGranularityChange(val) {
      this.$emit('granularity-change', val)
    },
    applyRefreshInterval() {
      this.$emit('refresh-interval-change', this.localRefreshInterval)
    },
    handleResourceClick(resource) {
      this.$emit('resource-click', resource)
    }
  }
}
</script>

<style lang="less" scoped>
.station-data-panel {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  min-height: 0;

  // Scrollbar styling
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.12);
    border-radius: 3px;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }

  // ---- Empty state ----
  &__empty {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
    user-select: none;

    i {
      font-size: 60px;
      margin-bottom: 16px;
      opacity: 0.35;
    }

    p {
      margin: 0;
      font-size: 14px;
      line-height: 1.6;
    }
  }

  // ---- Station view area ----
  &__station {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  // ---- Station KPI Board ----
  &__station-kpis {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
    margin-bottom: 12px;
  }

  &__station-kpi {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 12px 14px;
    border-radius: 4px;
    border: 1px solid rgba(255, 255, 255, 0.06);
    background: rgba(255, 255, 255, 0.03);
  }

  &__station-kpi-label {
    font-size: 11px;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.55));
  }

  &__station-kpi-value {
    font-size: 18px;
    font-weight: 600;
    color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92));
    small { font-size: 11px; font-weight: 400; color: rgba(255, 255, 255, 0.5); margin-left: 3px; }
  }

  // ---- Monthly VPP Stats ----
  &__monthly-stats {
    margin-bottom: 12px;
    padding: 14px 16px;
    border-radius: 4px;
    border: 1px solid rgba(255, 255, 255, 0.06);
    background: rgba(255, 255, 255, 0.02);
  }

  &__monthly-stats-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--pvms-text-primary, rgba(255, 255, 255, 0.88));
    margin-bottom: 10px;
  }

  &__monthly-stats-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
  }

  &__monthly-stat {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 10px 6px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.03);
  }

  &__monthly-stat-value {
    font-size: 16px;
    font-weight: 600;
    color: var(--pvms-primary, #06a299);
    small { font-size: 10px; font-weight: 400; color: rgba(255, 255, 255, 0.5); margin-left: 2px; }
  }

  &__monthly-stat-label {
    font-size: 11px;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.55));
  }

  // ---- Toolbar ----
  &__toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    margin-bottom: 12px;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid var(--pvms-border-soft, rgba(255, 255, 255, 0.08));
    border-radius: 6px;

    // Date picker dark overrides
    /deep/ .el-date-editor {
      width: 150px;

      .el-input__inner {
        background: rgba(255, 255, 255, 0.06);
        border: 1px solid rgba(255, 255, 255, 0.12);
        color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92));
        font-size: 12px;

        &::placeholder {
          color: rgba(255, 255, 255, 0.35);
        }
      }

      .el-input__prefix {
        color: rgba(255, 255, 255, 0.4);
      }
    }

    // Button group dark overrides
    /deep/ .el-button-group {
      .el-button {
        background: rgba(255, 255, 255, 0.06);
        border-color: rgba(255, 255, 255, 0.12);
        color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.78));
        padding: 7px 10px;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: #fff;
        }
      }
    }

    // Standalone buttons
    /deep/ .el-button {
      background: rgba(255, 255, 255, 0.06);
      border-color: rgba(255, 255, 255, 0.12);
      color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.78));

      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: #fff;
      }

      &--primary {
        background: var(--pvms-primary, #06a299);
        border-color: var(--pvms-primary, #06a299);
        color: #fff;

        &:hover {
          opacity: 0.85;
        }
      }
    }

    // Radio group dark overrides
    /deep/ .el-radio-group {
      .el-radio-button__inner {
        background: rgba(255, 255, 255, 0.06);
        border-color: rgba(255, 255, 255, 0.12);
        color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.78));
        font-size: 12px;
        padding: 7px 14px;
        box-shadow: none;

        &:hover {
          color: #fff;
        }
      }

      .el-radio-button__orig-radio:checked + .el-radio-button__inner {
        background: var(--pvms-primary, #06a299);
        border-color: var(--pvms-primary, #06a299);
        color: #fff;
        box-shadow: -1px 0 0 0 var(--pvms-primary, #06a299);
      }
    }

    // Input number dark overrides
    /deep/ .el-input-number {
      width: 100px;

      .el-input__inner {
        background: rgba(255, 255, 255, 0.06);
        border-color: rgba(255, 255, 255, 0.12);
        color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92));
        font-size: 12px;
      }

      .el-input-number__decrease,
      .el-input-number__increase {
        background: rgba(255, 255, 255, 0.04);
        border-color: rgba(255, 255, 255, 0.12);
        color: rgba(255, 255, 255, 0.55);

        &:hover {
          color: var(--pvms-primary, #06a299);
        }
      }
    }
  }

  // ---- Refresh group ----
  &__refresh-group {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__refresh-label {
    font-size: 12px;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
    white-space: nowrap;
  }

  // ---- Tabs dark overrides ----
  /deep/ .el-tabs {
    margin-bottom: 0;

    .el-tabs__header {
      margin-bottom: 0;
      border-bottom: none;
    }

    .el-tabs__nav-wrap::after {
      display: none;
    }

    .el-tabs__nav {
      border: none;
      display: flex;
    }

    .el-tabs__item {
      height: 34px;
      line-height: 34px;
      padding: 0 16px;
      font-size: 13px;
      color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 0;
      transition: all 0.25s ease;

      &:first-child {
        border-radius: 4px 0 0 4px;
      }

      &:last-child {
        border-radius: 0 4px 4px 0;
      }

      &:hover {
        color: rgba(255, 255, 255, 0.85);
        background: rgba(24, 144, 255, 0.08);
      }

      &.is-active {
        color: #fff;
        background: var(--pvms-primary, #06a299);
        border-color: var(--pvms-primary, #06a299);
      }
    }
  }

  // ---- Chart area (scrollable) ----
  &__chart-area {
    flex: 1;
    min-height: 0;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.12);
      border-radius: 3px;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  // Fix table selection row visibility in dark theme
  /deep/ .el-table__body tr.current-row > td {
    background: rgba(6, 162, 153, 0.15) !important;
    color: var(--pvms-text-primary, rgba(255, 255, 255, 0.92)) !important;
  }
  /deep/ .el-table__body tr.hover-row > td {
    background: rgba(255, 255, 255, 0.04) !important;
  }
}
</style>
