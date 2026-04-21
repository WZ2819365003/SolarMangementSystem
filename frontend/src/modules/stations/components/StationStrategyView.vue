<template>
  <div class="station-strategy-view">
    <!-- Current strategy as table -->
    <div v-if="data && data.currentStrategy" class="station-strategy-view__current">
      <div class="station-strategy-view__header">
        <span class="station-strategy-view__title">当前执行策略</span>
        <el-button size="mini" type="primary" icon="el-icon-view" @click="showDetail">查看详情</el-button>
      </div>
      <el-table :data="[data.currentStrategy]" size="mini" style="width: 100%;">
        <el-table-column prop="name" label="策略名称" min-width="180" />
        <el-table-column prop="type" label="策略类型" width="120" />
        <el-table-column prop="status" label="执行状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === '执行中' ? 'success' : 'warning'" size="mini">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column prop="endTime" label="结束时间" width="150" />
        <el-table-column prop="targetPowerKw" label="目标功率(kW)" width="130" align="right" />
        <el-table-column label="预估收益" width="120" align="right">
          <template slot-scope="{ row }">¥{{ row.estimatedRevenueCny }}</template>
        </el-table-column>
      </el-table>
    </div>
    <div v-else class="station-strategy-view__empty">当前无执行中的策略</div>

    <!-- Execution logs table -->
    <div v-if="data && data.executionLogs && data.executionLogs.length" class="station-strategy-view__logs">
      <div class="station-strategy-view__title">执行记录</div>
      <el-table :data="data.executionLogs" size="mini" stripe style="width: 100%;">
        <el-table-column prop="time" label="时间" width="120" />
        <el-table-column prop="action" label="操作" min-width="200" />
        <el-table-column prop="result" label="结果" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.result === '成功' ? 'success' : 'danger'" size="mini">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviationRate" label="偏差率(%)" width="120" />
      </el-table>
    </div>

    <!-- Strategy Detail Drawer (slides from right) -->
    <el-drawer
      :visible.sync="detailVisible"
      title="策略详情（只读）"
      direction="rtl"
      size="50%"
      append-to-body
      custom-class="station-strategy-view__drawer"
      :close-on-click-modal="true"
    >
      <div v-if="data && data.currentStrategy" class="station-strategy-view__drawer-body">
        <!-- Basic info -->
        <div class="station-strategy-view__drawer-section">
          <div class="station-strategy-view__drawer-section-title">基本信息</div>
          <div class="station-strategy-view__detail-grid">
            <div class="station-strategy-view__detail-row" v-for="item in detailFields" :key="item.label">
              <span class="station-strategy-view__detail-label">{{ item.label }}</span>
              <span class="station-strategy-view__detail-value">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <!-- Electricity price time periods -->
        <div class="station-strategy-view__drawer-section">
          <div class="station-strategy-view__drawer-section-title">分时电价参考</div>
          <div class="station-strategy-view__price-legend">
            <span class="station-strategy-view__price-tag is-peak">峰: 1.11 元</span>
            <span class="station-strategy-view__price-tag is-flat">平: 0.65 元</span>
            <span class="station-strategy-view__price-tag is-valley">谷: 0.25 元</span>
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
        <div class="station-strategy-view__drawer-section">
          <div class="station-strategy-view__drawer-section-title">执行概要</div>
          <div class="station-strategy-view__detail-grid">
            <div class="station-strategy-view__detail-row">
              <span class="station-strategy-view__detail-label">执行记录数</span>
              <span class="station-strategy-view__detail-value">{{ (data.executionLogs || []).length }} 条</span>
            </div>
            <div class="station-strategy-view__detail-row">
              <span class="station-strategy-view__detail-label">最近操作</span>
              <span class="station-strategy-view__detail-value">{{ data.executionLogs && data.executionLogs.length ? data.executionLogs[0].action : '--' }}</span>
            </div>
            <div class="station-strategy-view__detail-row">
              <span class="station-strategy-view__detail-label">最近偏差率</span>
              <span class="station-strategy-view__detail-value">{{ data.executionLogs && data.executionLogs.length ? data.executionLogs[0].deviationRate + '%' : '--' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: 'StationStrategyView',
  props: {
    data: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      detailVisible: false
    }
  },
  computed: {
    strategyFields() {
      if (!this.data || !this.data.currentStrategy) return []
      const s = this.data.currentStrategy
      return [
        { label: '策略名称', value: s.name },
        { label: '策略类型', value: s.type },
        { label: '执行状态', value: s.status },
        { label: '开始时间', value: s.startTime },
        { label: '结束时间', value: s.endTime },
        { label: '目标功率', value: s.targetPowerKw + ' kW' },
        { label: '预估收益', value: '¥' + s.estimatedRevenueCny }
      ]
    },
    detailFields() {
      if (!this.data || !this.data.currentStrategy) return []
      const s = this.data.currentStrategy
      return [
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
    showDetail() {
      this.detailVisible = true
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
.station-strategy-view {
  width: 100%;

  &__current {
    margin-bottom: 16px;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.88);
  }

  &__empty {
    padding: 60px 0;
    text-align: center;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.58);
    border-radius: 4px;
    border: 1px solid rgba(255, 255, 255, 0.08);
    background: rgba(255, 255, 255, 0.02);
  }

  &__logs {
    margin-top: 16px;

    .station-strategy-view__title {
      margin-bottom: 12px;
    }
  }

  // Table dark theme
  /deep/ .el-table {
    background-color: transparent;
    color: rgba(255, 255, 255, 0.85);
    &::before { background-color: rgba(255, 255, 255, 0.06); }
    th { background-color: rgba(255, 255, 255, 0.04); color: rgba(255, 255, 255, 0.58); border-bottom: 1px solid rgba(255, 255, 255, 0.06); font-size: 12px; }
    tr { background-color: transparent; }
    td { border-bottom: 1px solid rgba(255, 255, 255, 0.04); font-size: 12px; }
    .el-table__row--striped td { background-color: rgba(255, 255, 255, 0.02); }
    .el-table__row:hover > td { background-color: rgba(24, 144, 255, 0.08); }
    .el-table__empty-block { background-color: transparent; }
    // Price row coloring
    .price-row--peak td { background-color: rgba(239, 71, 111, 0.08); }
    .price-row--valley td { background-color: rgba(6, 214, 160, 0.08); }
  }

  /deep/ .el-tag {
    border-radius: 3px; font-size: 11px; height: 22px; line-height: 20px; padding: 0 8px; background: transparent;
    &--success { color: #67c23a; border-color: rgba(103, 194, 58, 0.4); }
    &--warning { color: #e6a23c; border-color: rgba(230, 162, 60, 0.4); }
    &--danger { color: #f56c6c; border-color: rgba(245, 108, 108, 0.4); }
    &--info { color: rgba(255, 255, 255, 0.55); border-color: rgba(255, 255, 255, 0.2); }
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
      .el-drawer__body { padding: 0; overflow-y: auto; }
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
