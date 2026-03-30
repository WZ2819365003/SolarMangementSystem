<template>
  <div v-loading="loading" class="strategy-config-view" data-testid="strategy-config-view">
    <div class="strategy-config-view__grid">
      <app-section-card title="策略树" subtitle="树结构来自后端 H2 元数据，站点统计会随着策略状态变化更新">
        <el-tree
          :data="treeData"
          node-key="id"
          default-expand-all
          :props="{ children: 'children', label: 'label' }"
          class="strategy-config-view__tree"
        >
          <div slot-scope="{ data }" class="strategy-config-view__tree-node">
            <span>{{ data.label }}</span>
            <span class="strategy-config-view__tree-meta">
              {{ data.strategyCount || 0 }} / {{ data.activeCount || 0 }}
            </span>
          </div>
        </el-tree>
      </app-section-card>

      <app-section-card title="策略配置" subtitle="策略元数据在后端存储，模拟结果由后台基于生产与预测事实数据实时计算">
        <el-form label-width="100px" class="strategy-config-view__form">
          <el-form-item label="电站">
            <el-select v-model="form.stationId" filterable @change="loadElectricityPrice">
              <el-option
                v-for="item in meta.stations"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="form.type">
              <el-option
                v-for="item in meta.typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model.trim="form.name" placeholder="输入策略名称" />
          </el-form-item>
          <el-form-item label="目标功率">
            <el-input-number v-model="form.targetPowerKw" :min="100" :step="100" :max="10000" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              value-format="yyyy-MM-ddTHH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              value-format="yyyy-MM-ddTHH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model.trim="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>

        <div class="strategy-config-view__actions">
          <el-button type="primary" icon="el-icon-cpu" @click="handleSimulate">单策略模拟</el-button>
          <el-button plain icon="el-icon-plus" @click="handleCreate">创建策略</el-button>
          <el-button plain icon="el-icon-copy-document" @click="handleBatchCreate">批量创建</el-button>
        </div>
      </app-section-card>
    </div>

    <div class="strategy-config-view__grid">
      <app-section-card title="电价时段" subtitle="电价模板保存在后端 H2，可在替换真实系统时切换为外部价格源">
        <el-table :data="pricePeriods" size="mini" stripe>
          <el-table-column prop="periodOrder" label="序号" width="80" />
          <el-table-column prop="startLabel" label="开始" width="90" />
          <el-table-column prop="endLabel" label="结束" width="90" />
          <el-table-column prop="priceType" label="时段类型" width="120" />
          <el-table-column prop="price" label="电价(¥/kWh)" width="120" align="right" />
        </el-table>
      </app-section-card>

      <app-section-card title="模拟结果" subtitle="这里的收益和区间不是前端 mock，而是后台结合系统事实数据即时计算">
        <div v-if="simulateResult" class="strategy-config-view__simulate">
          <div class="strategy-config-view__simulate-summary">
            <div class="strategy-config-view__simulate-item">
              <span>预估收益</span>
              <strong>{{ simulateResult.estimatedRevenue }} ¥</strong>
            </div>
            <div class="strategy-config-view__simulate-item">
              <span>置信区间</span>
              <strong>{{ simulateResult.confidenceRange.low }} ~ {{ simulateResult.confidenceRange.high }}</strong>
            </div>
            <div class="strategy-config-view__simulate-item">
              <span>成功概率</span>
              <strong>{{ simulateResult.successProbability }} %</strong>
            </div>
          </div>

          <el-table :data="timelinePreview" size="mini" stripe>
            <el-table-column prop="time" label="时间" width="90" />
            <el-table-column prop="loadKw" label="负荷(kW)" width="100" align="right" />
            <el-table-column prop="actualPvKw" label="实际出力(kW)" width="120" align="right" />
            <el-table-column prop="predictedPvKw" label="预测出力(kW)" width="120" align="right" />
            <el-table-column prop="dispatchKw" label="调节功率(kW)" width="120" align="right" />
            <el-table-column prop="priceType" label="时段" width="100" />
          </el-table>
        </div>
        <el-empty v-else description="先执行一次单策略模拟" :image-size="88" />
      </app-section-card>
    </div>

    <app-section-card title="批量候选策略" subtitle="选中已存在策略可直接批量模拟；未选中时会按当前配置自动生成两个候选站点">
      <template #extra>
        <el-button
          size="mini"
          type="primary"
          plain
          icon="el-icon-data-analysis"
          @click="handleBatchSimulate"
        >
          批量模拟
        </el-button>
      </template>

      <el-table
        :data="candidateRows"
        size="mini"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="name" label="策略名称" min-width="180" />
        <el-table-column prop="stationName" label="电站" min-width="140" />
        <el-table-column prop="typeLabel" label="类型" width="140" />
        <el-table-column prop="targetPowerKw" label="目标功率(kW)" width="120" align="right" />
        <el-table-column prop="statusLabel" label="状态" width="120" />
        <el-table-column prop="todayRevenue" label="当日收益(¥)" width="120" align="right" />
      </el-table>

      <div v-if="batchResult" class="strategy-config-view__batch-result">
        <div class="strategy-config-view__batch-total">
          批量模拟总收益：<strong>{{ batchResult.totalRevenue }} ¥</strong>
        </div>
        <el-table :data="batchResult.results || []" size="mini" stripe>
          <el-table-column prop="stationName" label="电站" min-width="140" />
          <el-table-column prop="type" label="类型" width="160" />
          <el-table-column prop="estimatedRevenue" label="预估收益(¥)" width="120" align="right" />
          <el-table-column label="区间" width="180">
            <template slot-scope="{ row }">
              {{ row.confidenceRange.low }} ~ {{ row.confidenceRange.high }}
            </template>
          </el-table-column>
          <el-table-column prop="successProbability" label="成功概率(%)" width="120" align="right" />
        </el-table>
      </div>
    </app-section-card>
  </div>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'
import {
  fetchStrategyElectricityPrice,
  simulateStrategy,
  batchSimulateStrategy,
  createStrategy,
  batchCreateStrategy
} from '@/api/pvms'

export default {
  name: 'StrategyConfigView',
  components: {
    AppSectionCard
  },
  props: {
    viewData: {
      type: Object,
      default: function () {
        return {}
      }
    },
    meta: {
      type: Object,
      default: function () {
        return {}
      }
    },
    query: {
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
      form: {
        stationId: '',
        type: 'peak-shaving',
        name: '',
        targetPowerKw: 1200,
        startTime: '2026-03-30T08:00:00',
        endTime: '2026-03-30T18:00:00',
        remark: ''
      },
      localPrice: {
        periods: []
      },
      simulateResult: null,
      batchResult: null,
      selection: []
    }
  },
  computed: {
    treeData() {
      return this.viewData.tree || []
    },
    pricePeriods() {
      return this.localPrice.periods || []
    },
    candidateRows() {
      return (this.viewData.list && this.viewData.list.items) || []
    },
    timelinePreview() {
      return (this.simulateResult && this.simulateResult.timeline ? this.simulateResult.timeline : []).slice(0, 12)
    },
    candidateStations() {
      var region = this.query.region
      var stations = this.meta.stations || []
      return stations.filter(function (item) {
        return !region || item.region === region
      })
    }
  },
  watch: {
    query: {
      immediate: true,
      deep: true,
      handler(value) {
        if (value.stationId) {
          this.form.stationId = value.stationId
        }
        if (value.type) {
          this.form.type = value.type
        }
      }
    },
    viewData: {
      immediate: true,
      deep: true,
      handler(value) {
        this.localPrice = value.electricityPrice || { periods: [] }
      }
    }
  },
  methods: {
    buildPayload(stationId, stationName) {
      return {
        stationId: stationId || this.form.stationId,
        type: this.form.type,
        name: stationName ? stationName + '-' + (this.form.name || 'strategy') : this.form.name,
        targetPowerKw: Number(this.form.targetPowerKw),
        startTime: this.form.startTime,
        endTime: this.form.endTime,
        remark: this.form.remark
      }
    },
    handleSelectionChange(rows) {
      this.selection = rows || []
    },
    async loadElectricityPrice(stationId) {
      var res = await fetchStrategyElectricityPrice({ stationId: stationId || this.form.stationId })
      this.localPrice = res.data || { periods: [] }
    },
    async handleSimulate() {
      var res = await simulateStrategy(this.buildPayload())
      this.simulateResult = res.data || null
      this.$message.success('模拟完成')
    },
    async handleCreate() {
      await createStrategy(this.buildPayload())
      this.$message.success('策略已创建')
      this.$emit('refresh')
    },
    async handleBatchCreate() {
      var stations = this.candidateStations.slice(0, 2)
      if (!stations.length) {
        this.$message.warning('没有可用站点用于批量创建')
        return
      }
      var payload = {
        strategies: stations.map((item) => this.buildPayload(item.id, item.name))
      }
      await batchCreateStrategy(payload)
      this.$message.success('批量创建完成')
      this.$emit('refresh')
    },
    async handleBatchSimulate() {
      var payloadStrategies = this.selection.length
        ? this.selection.map((item) => ({
            stationId: item.stationId,
            type: item.type,
            targetPowerKw: item.targetPowerKw,
            startTime: item.startTime,
            endTime: item.endTime,
            remark: item.name
          }))
        : this.candidateStations.slice(0, 2).map((item) => this.buildPayload(item.id, item.name))

      if (!payloadStrategies.length) {
        this.$message.warning('没有可用于批量模拟的数据')
        return
      }

      var res = await batchSimulateStrategy({ strategies: payloadStrategies })
      this.batchResult = res.data || null
      this.$message.success('批量模拟完成')
    }
  }
}
</script>

<style lang="less" scoped>
.strategy-config-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.strategy-config-view__grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
}

.strategy-config-view__tree /deep/ .el-tree {
  background: transparent;
  color: var(--pvms-text-primary);
}

.strategy-config-view__tree-node {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.strategy-config-view__tree-meta {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.strategy-config-view__form /deep/ .el-select,
.strategy-config-view__form /deep/ .el-date-editor,
.strategy-config-view__form /deep/ .el-input,
.strategy-config-view__form /deep/ .el-input-number {
  width: 100%;
}

.strategy-config-view__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

.strategy-config-view__simulate {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.strategy-config-view__simulate-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.strategy-config-view__simulate-item {
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.strategy-config-view__simulate-item span {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.strategy-config-view__simulate-item strong {
  color: var(--pvms-text-primary);
  font-size: 16px;
}

.strategy-config-view__batch-result {
  margin-top: 16px;
}

.strategy-config-view__batch-total {
  margin-bottom: 12px;
  color: var(--pvms-text-primary);
}

@media (max-width: 1480px) {
  .strategy-config-view__grid {
    grid-template-columns: 1fr;
  }

  .strategy-config-view__simulate-summary {
    grid-template-columns: 1fr;
  }
}
</style>
