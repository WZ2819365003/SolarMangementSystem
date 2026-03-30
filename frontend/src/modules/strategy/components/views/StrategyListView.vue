<template>
  <div v-loading="loading" class="strategy-list-view" data-testid="strategy-list-view">
    <section class="pv-card-grid strategy-list-view__kpis">
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

    <app-section-card title="策略台账" subtitle="后端从 H2 元数据读取策略，并实时补充收益快照和执行状态">
      <template slot="extra">
        <div class="strategy-list-view__toolbar">
          <el-button
            size="mini"
            plain
            icon="el-icon-finished"
            :disabled="selection.length === 0"
            @click="handleBatchSubmit"
          >
            批量提交
          </el-button>
          <el-button
            size="mini"
            type="danger"
            plain
            icon="el-icon-delete"
            :disabled="selection.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
      </template>

      <el-table
        :data="items"
        size="mini"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="name" label="策略名称" min-width="200" />
        <el-table-column prop="typeLabel" label="类型" width="140" />
        <el-table-column label="状态" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="row.statusType" size="mini">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stationName" label="电站" min-width="140" />
        <el-table-column prop="targetPowerKw" label="目标功率(kW)" width="120" align="right" />
        <el-table-column prop="todayRevenue" label="当日收益(¥)" width="120" align="right" />
        <el-table-column label="置信区间" width="160">
          <template slot-scope="{ row }">
            {{ confidenceText(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click="openDetail(row)">详情</el-button>
            <el-button type="text" size="mini" @click="handleSubmit(row)">提交</el-button>
            <el-button type="text" size="mini" class="is-danger" @click="handleTerminate(row)">终止</el-button>
          </template>
        </el-table-column>
      </el-table>
    </app-section-card>

    <el-drawer
      :visible.sync="detailVisible"
      title="策略详情"
      direction="rtl"
      size="44%"
      append-to-body
    >
      <div v-loading="detailLoading" class="strategy-list-view__drawer">
        <div v-if="detailData">
          <app-section-card title="基础信息">
            <div class="strategy-list-view__detail-grid">
              <div
                v-for="item in detailFields"
                :key="item.label"
                class="strategy-list-view__detail-item"
              >
                <span class="strategy-list-view__detail-label">{{ item.label }}</span>
                <span class="strategy-list-view__detail-value">{{ item.value }}</span>
              </div>
            </div>
          </app-section-card>

          <app-section-card title="执行时段">
            <el-table :data="detailData.periods || []" size="mini" stripe>
              <el-table-column prop="periodOrder" label="序号" width="80" />
              <el-table-column prop="startLabel" label="开始" width="100">
                <template slot-scope="{ row }">
                  {{ slotLabel(row.startSlot) }}
                </template>
              </el-table-column>
              <el-table-column prop="endLabel" label="结束" width="100">
                <template slot-scope="{ row }">
                  {{ slotLabel(row.endSlot + 1) }}
                </template>
              </el-table-column>
              <el-table-column prop="actionType" label="动作" min-width="120" />
              <el-table-column prop="targetRatio" label="目标比例" width="120" align="right" />
            </el-table>
          </app-section-card>

          <app-section-card title="执行日志">
            <el-table :data="detailData.executionLogs || []" size="mini" stripe>
              <el-table-column prop="time" label="时间" width="170" />
              <el-table-column prop="action" label="动作" min-width="120" />
              <el-table-column prop="result" label="结果" width="100" />
              <el-table-column prop="deviationRate" label="偏差率(%)" width="120" align="right" />
            </el-table>
          </app-section-card>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import {
  fetchStrategyDetail,
  submitStrategy,
  terminateStrategy,
  batchSubmitStrategy,
  batchDeleteStrategy
} from '@/api/pvms'

export default {
  name: 'StrategyListView',
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
      selection: [],
      detailVisible: false,
      detailLoading: false,
      detailData: null
    }
  },
  computed: {
    kpiCards() {
      var kpi = this.viewData.kpi || {}
      return [
        { key: 'active', title: '执行中策略', value: kpi.activeCount || 0, unit: '个', helper: '当前正在后台执行的策略数量', icon: 'el-icon-s-claim', accent: 'teal' },
        { key: 'pending', title: '待执行策略', value: kpi.pendingCount || 0, unit: '个', helper: '已提交但尚未进入执行态', icon: 'el-icon-time', accent: 'orange' },
        { key: 'revenue', title: '当日收益', value: kpi.todayRevenue || 0, unit: '¥', helper: '按当日收益记录实时汇总', icon: 'el-icon-money', accent: 'blue' },
        { key: 'success', title: '成功率', value: kpi.successRate || 0, unit: '%', helper: '基于执行日志 success 结果计算', icon: 'el-icon-circle-check', accent: 'emerald' }
      ]
    },
    items() {
      return (this.viewData.list && this.viewData.list.items) || []
    },
    detailFields() {
      if (!this.detailData) {
        return []
      }
      return [
        { label: '策略名称', value: this.detailData.name },
        { label: '策略类型', value: this.detailData.typeLabel },
        { label: '电站', value: this.detailData.stationName },
        { label: '状态', value: this.detailData.statusLabel },
        { label: '目标功率', value: this.detailData.targetPowerKw + ' kW' },
        { label: '当日收益', value: this.detailData.todayRevenue + ' ¥' },
        { label: '置信区间', value: this.confidenceText(this.detailData) },
        { label: '备注', value: this.detailData.remark || '--' }
      ]
    }
  },
  methods: {
    slotLabel(slot) {
      var safeSlot = Math.max(0, Math.min(slot, 96))
      if (safeSlot >= 96) {
        return '24:00'
      }
      var hour = Math.floor(safeSlot / 4)
      var minute = (safeSlot % 4) * 15
      return String(hour).padStart(2, '0') + ':' + String(minute).padStart(2, '0')
    },
    confidenceText(row) {
      var range = row.confidenceRange || {}
      return (range.low || 0) + ' ~ ' + (range.high || 0)
    },
    handleSelectionChange(rows) {
      this.selection = rows || []
    },
    async openDetail(row) {
      this.detailVisible = true
      this.detailLoading = true
      try {
        var res = await fetchStrategyDetail({ id: row.id })
        this.detailData = res.data || null
      } catch (error) {
        console.error('[StrategyListView] failed to load detail', error)
      } finally {
        this.detailLoading = false
      }
    },
    async handleSubmit(row) {
      await submitStrategy({ id: row.id })
      this.$message.success('策略已提交')
      this.$emit('refresh')
    },
    async handleTerminate(row) {
      await terminateStrategy({ id: row.id })
      this.$message.success('策略已终止')
      this.$emit('refresh')
    },
    async handleBatchSubmit() {
      await batchSubmitStrategy({ ids: this.selection.map(function (item) { return item.id }) })
      this.$message.success('批量提交完成')
      this.$emit('refresh')
    },
    async handleBatchDelete() {
      await batchDeleteStrategy({ ids: this.selection.map(function (item) { return item.id }) })
      this.selection = []
      this.$message.success('批量删除完成')
      this.$emit('refresh')
    }
  }
}
</script>

<style lang="less" scoped>
.strategy-list-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.strategy-list-view__toolbar {
  display: flex;
  gap: 10px;
}

.strategy-list-view__drawer {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.strategy-list-view__detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.strategy-list-view__detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.strategy-list-view__detail-label {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.strategy-list-view__detail-value {
  color: var(--pvms-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.is-danger {
  color: #f56c6c;
}
</style>
