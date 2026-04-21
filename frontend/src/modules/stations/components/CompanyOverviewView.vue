<template>
  <div class="company-overview-view">
    <!-- KPI cards -->
    <div v-if="data && data.kpis" class="company-overview-view__kpis">
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

    <!-- Tabs: 下属电站 / 策略查看 -->
    <app-section-card v-if="data && (data.stations || data.resources)" :title="activeCompanyTab === 'stations' ? '下属电站' : '策略查看'" :subtitle="data.name">
      <el-tabs v-model="activeCompanyTab" type="card" class="company-overview-view__tabs">
        <el-tab-pane label="下属电站" name="stations">
          <el-table :data="data.stations || data.resources" size="mini" stripe @row-click="handleRowClick" class="company-overview-view__table">
            <el-table-column prop="name" label="电站名称" min-width="200" />
            <el-table-column prop="capacityKwp" label="装机容量(kWp)" width="130" />
            <el-table-column prop="realtimePowerKw" label="实时功率(kW)" width="130" />
            <el-table-column prop="loadKw" label="负荷(kW)" width="110" />
            <el-table-column prop="adjustableKw" label="可调空间(kW)" width="130" />
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="{ row }">
                <el-tag :type="resolveStatusType(row.status)" size="mini">{{ resolveStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="策略查看" name="strategies">
          <el-table :data="companyStrategyRows" size="mini" stripe class="company-overview-view__table">
            <el-table-column prop="stationName" label="电站名称" min-width="180" />
            <el-table-column prop="name" label="策略名称" min-width="160" />
            <el-table-column prop="type" label="策略类型" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="{ row }">
                <el-tag :type="row.status === '执行中' ? 'success' : 'warning'" size="mini">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetPowerKw" label="目标功率(kW)" width="130" align="right" />
            <el-table-column label="预估收益" width="120" align="right">
              <template slot-scope="{ row }">¥{{ row.estimatedRevenueCny }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="{ row }">
                <el-button type="text" size="mini" @click="openStrategyDrawer(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </app-section-card>

    <!-- Strategy Detail Drawer -->
    <el-drawer
      :visible.sync="strategyDrawerVisible"
      title="策略详情（只读）"
      direction="rtl"
      size="50%"
      append-to-body
      custom-class="company-overview-view__drawer"
      :close-on-click-modal="true"
    >
      <div v-if="selectedStrategy" class="company-overview-view__drawer-body">
        <!-- Basic info -->
        <div class="company-overview-view__drawer-section">
          <div class="company-overview-view__drawer-section-title">基本信息</div>
          <div class="company-overview-view__detail-grid">
            <div class="company-overview-view__detail-row" v-for="item in drawerDetailFields" :key="item.label">
              <span class="company-overview-view__detail-label">{{ item.label }}</span>
              <span class="company-overview-view__detail-value">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <!-- Electricity price time periods -->
        <div class="company-overview-view__drawer-section">
          <div class="company-overview-view__drawer-section-title">分时电价参考</div>
          <div class="company-overview-view__price-legend">
            <span class="company-overview-view__price-tag is-peak">峰: 1.11 元</span>
            <span class="company-overview-view__price-tag is-flat">平: 0.65 元</span>
            <span class="company-overview-view__price-tag is-valley">谷: 0.25 元</span>
          </div>
          <el-table :data="electricityPriceRows" size="mini" style="width: 100%;" :row-class-name="priceRowClassName">
            <el-table-column prop="period" label="时段" width="140" />
            <el-table-column prop="type" label="类型" width="80" align="center">
              <template slot-scope="{ row }">
                <el-tag :type="row.type === '峰' ? 'danger' : row.type === '谷' ? 'success' : ''" size="mini" effect="dark">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="电价(元/kWh)" width="130" align="right" />
            <el-table-column prop="strategy" label="策略动作" min-width="140">
              <template slot-scope="{ row }">
                <span :style="{ color: row.strategy === '削峰' ? '#ef476f' : row.strategy === '填谷' ? '#06d6a0' : 'rgba(255,255,255,0.6)' }">{{ row.strategy }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Execution summary -->
        <div class="company-overview-view__drawer-section">
          <div class="company-overview-view__drawer-section-title">执行概要</div>
          <div class="company-overview-view__detail-grid">
            <div class="company-overview-view__detail-row">
              <span class="company-overview-view__detail-label">执行记录数</span>
              <span class="company-overview-view__detail-value">{{ selectedStrategy.executionCount }} 条</span>
            </div>
            <div class="company-overview-view__detail-row">
              <span class="company-overview-view__detail-label">最近操作</span>
              <span class="company-overview-view__detail-value">{{ selectedStrategy.lastAction }}</span>
            </div>
            <div class="company-overview-view__detail-row">
              <span class="company-overview-view__detail-label">最近偏差率</span>
              <span class="company-overview-view__detail-value">{{ selectedStrategy.lastDeviationRate }}%</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
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

const STRATEGY_TYPES = ['需求响应', '调频辅助', '电网约束', '削峰填谷']

export default {
  name: 'CompanyOverviewView',
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
  data() {
    return {
      activeCompanyTab: 'stations',
      strategyDrawerVisible: false,
      selectedStrategy: null
    }
  },
  computed: {
    companyStrategyRows() {
      const stations = (this.data && (this.data.stations || this.data.resources)) || []
      const baseRevenue = [1280, 2150, 960, 1750, 3200, 880, 1560, 2480, 1120, 1890]
      return stations.map((station, index) => {
        const targetPower = station.realtimePowerKw || Math.round((station.capacityKwp || 0) * 0.6)
        return {
          stationName: station.name,
          name: (station.name || '电站') + '调峰策略',
          type: STRATEGY_TYPES[index % STRATEGY_TYPES.length],
          status: index % 5 === 3 ? '待执行' : '执行中',
          targetPowerKw: targetPower,
          estimatedRevenueCny: baseRevenue[index % baseRevenue.length],
          startTime: '2026-03-27 08:00',
          endTime: '2026-03-27 20:00',
          executionCount: 3 + (index % 7),
          lastAction: index % 2 === 0 ? '功率调整至目标值' : '响应调频信号',
          lastDeviationRate: (1.2 + (index % 5) * 0.8).toFixed(1)
        }
      })
    },
    drawerDetailFields() {
      if (!this.selectedStrategy) return []
      const s = this.selectedStrategy
      return [
        { label: '电站名称', value: s.stationName },
        { label: '策略名称', value: s.name },
        { label: '策略类型', value: s.type },
        { label: '执行状态', value: s.status },
        { label: '开始时间', value: s.startTime },
        { label: '结束时间', value: s.endTime },
        { label: '目标功率', value: s.targetPowerKw + ' kW' },
        { label: '预估收益', value: '¥' + s.estimatedRevenueCny }
      ]
    },
    electricityPriceRows() {
      return [
        { period: '00:00 - 06:00', type: '谷', price: '0.25', strategy: '填谷' },
        { period: '06:00 - 08:00', type: '平', price: '0.65', strategy: '正常运行' },
        { period: '08:00 - 11:00', type: '峰', price: '1.11', strategy: '削峰' },
        { period: '11:00 - 13:00', type: '平', price: '0.65', strategy: '正常运行' },
        { period: '13:00 - 15:00', type: '峰', price: '1.11', strategy: '削峰' },
        { period: '15:00 - 17:00', type: '平', price: '0.65', strategy: '正常运行' },
        { period: '17:00 - 19:00', type: '峰', price: '1.11', strategy: '削峰' },
        { period: '19:00 - 22:00', type: '平', price: '0.65', strategy: '正常运行' },
        { period: '22:00 - 24:00', type: '谷', price: '0.25', strategy: '填谷' }
      ]
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
      this.$emit('resource-click', row)
    },
    openStrategyDrawer(row) {
      this.selectedStrategy = row
      this.strategyDrawerVisible = true
    },
    priceRowClassName({ row }) {
      if (row.type === '峰') return 'price-row--peak'
      if (row.type === '谷') return 'price-row--valley'
      return ''
    }
  }
}
</script>

<style lang="less" scoped>
.company-overview-view {
  width: 100%;

  &__kpis {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
    margin-bottom: 20px;
  }

  &__table {
    /deep/ .el-table__row {
      cursor: pointer;
    }
  }

  // Tabs dark theme (matching StationDataPanel)
  &__tabs {
    /deep/ .el-tabs__header {
      margin-bottom: 12px;
      border-bottom: none;
    }

    /deep/ .el-tabs__nav-wrap::after {
      display: none;
    }

    /deep/ .el-tabs__nav {
      border: none;
      display: flex;
    }

    /deep/ .el-tabs__item {
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

  /deep/ .el-table {
    background-color: transparent;
    color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.85));

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
      font-size: 13px;
      color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.85));
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

    // Fix selected row text visibility
    .el-table__body tr.current-row > td {
      background: rgba(6, 162, 153, 0.15) !important;
      color: rgba(255, 255, 255, 0.92) !important;
    }

    // Price row coloring (for drawer table)
    .price-row--peak td {
      background-color: rgba(239, 71, 111, 0.08);
    }

    .price-row--valley td {
      background-color: rgba(6, 214, 160, 0.08);
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

      .el-drawer__close-btn {
        color: rgba(255, 255, 255, 0.5);
        &:hover { color: #fff; }
      }

      .el-drawer__body {
        padding: 0;
        overflow-y: auto;
      }
    }
  }

  &__drawer-body {
    padding: 20px 24px;
  }

  &__drawer-section {
    margin-bottom: 24px;
  }

  &__drawer-section-title {
    font-size: 14px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.88);
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }

  &__detail-grid {
    border: 1px solid rgba(255, 255, 255, 0.06);
    border-radius: 4px;
    overflow: hidden;
  }

  &__detail-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.04);
    &:last-child { border-bottom: none; }
  }

  &__detail-label {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.55);
  }

  &__detail-value {
    font-size: 13px;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.92);
  }

  &__price-legend {
    display: flex;
    gap: 16px;
    margin-bottom: 12px;
  }

  &__price-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: 3px;
    font-size: 12px;
    font-weight: 500;

    &.is-peak { background: rgba(239, 71, 111, 0.15); color: #ef476f; }
    &.is-flat { background: rgba(255, 209, 102, 0.15); color: #ffd166; }
    &.is-valley { background: rgba(6, 214, 160, 0.15); color: #06d6a0; }
  }
}
</style>
