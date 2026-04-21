<template>
  <app-section-card
    title="调度执行情况"
    subtitle="用于判断指令执行质量和当前调度风险"
    data-testid="resource-unit-dispatch-card"
  >
    <div class="resource-unit-dispatch-card">
      <div class="resource-unit-dispatch-card__grid">
        <div class="resource-unit-dispatch-card__metric">
          <span>下发指令数</span>
          <strong>{{ dispatchSummary.issuedCount }}</strong>
        </div>
        <div class="resource-unit-dispatch-card__metric">
          <span>成功执行数</span>
          <strong>{{ dispatchSummary.successCount }}</strong>
        </div>
        <div class="resource-unit-dispatch-card__metric">
          <span>执行成功率</span>
          <strong>{{ dispatchSummary.successRate }}%</strong>
        </div>
        <div class="resource-unit-dispatch-card__metric">
          <span>最新响应时长</span>
          <strong>{{ dispatchSummary.lastResponseSeconds }} s</strong>
        </div>
      </div>
      <div class="resource-unit-dispatch-card__footer">
        <span>考核风险</span>
        <el-tag size="mini" :type="resolveTagType(dispatchSummary.riskLabel)">
          {{ dispatchSummary.riskLabel }}
        </el-tag>
      </div>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'ResourceUnitDispatchCard',
  components: {
    AppSectionCard
  },
  props: {
    dispatchSummary: {
      type: Object,
      default: () => ({})
    }
  },
  methods: {
    resolveTagType
  }
}
</script>

<style lang="less" scoped>
.resource-unit-dispatch-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resource-unit-dispatch-card__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.resource-unit-dispatch-card__metric {
  padding: 12px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.resource-unit-dispatch-card__metric span {
  display: block;
  margin-bottom: 6px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.resource-unit-dispatch-card__metric strong {
  color: var(--pvms-text-primary);
  font-size: 20px;
}

.resource-unit-dispatch-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--pvms-text-secondary);
}

@media (max-width: 1380px) {
  .resource-unit-dispatch-card__grid {
    grid-template-columns: 1fr;
  }
}
</style>
