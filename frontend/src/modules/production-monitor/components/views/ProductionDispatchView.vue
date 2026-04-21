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
        <el-table :data="viewData.records || []" size="mini">
          <el-table-column prop="issuedAt" label="指令时间" width="160" />
          <el-table-column prop="commandType" label="指令类型" width="110" />
          <el-table-column prop="targetPowerMw" label="目标功率(MW)" width="130" />
          <el-table-column prop="actualPowerMw" label="实际响应(MW)" width="130" />
          <el-table-column prop="responseSeconds" label="响应时长(s)" width="120" />
          <el-table-column prop="status" label="执行状态" width="110">
            <template slot-scope="{ row }">
              <el-tag size="mini" effect="dark" :type="resolveTagType(row.status)">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deviationReason" label="偏差说明" min-width="220" />
        </el-table>
      </div>
    </app-section-card>
  </div>
</template>

<script>
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
  methods: {
    resolveTagType
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
</style>
