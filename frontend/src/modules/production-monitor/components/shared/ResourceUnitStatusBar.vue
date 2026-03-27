<template>
  <app-section-card title="资源单元信息" subtitle="同区位电站共享天气与出力趋势，只在负荷分摊比例上不同">
    <div class="resource-unit-status-bar">
      <div v-for="item in items" :key="item.label" class="resource-unit-status-bar__item">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'ResourceUnitStatusBar',
  components: {
    AppSectionCard
  },
  props: {
    resourceUnit: {
      type: Object,
      default: () => ({})
    }
  },
  computed: {
    items() {
      return [
        { label: '资源单元', value: this.resourceUnit.name || '--' },
        { label: '聚合区域', value: `${this.resourceUnit.region || '--'} / ${this.resourceUnit.city || '--'}` },
        { label: '调度模式', value: this.resourceUnit.dispatchMode || '--' },
        { label: '成员电站', value: `${this.resourceUnit.stationCount || 0} 座` },
        { label: '站群最大距离', value: `${this.resourceUnit.clusterRadiusKm || '--'} km` },
        { label: '策略描述', value: this.resourceUnit.strategyLabel || '--' }
      ]
    }
  }
}
</script>

<style lang="less" scoped>
.resource-unit-status-bar {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.resource-unit-status-bar__item {
  min-height: 82px;
  padding: 14px 16px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
}

.resource-unit-status-bar__item span {
  display: block;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.resource-unit-status-bar__item strong {
  display: block;
  margin-top: 10px;
  color: var(--pvms-text-primary);
  font-size: 15px;
  line-height: 22px;
}

@media (max-width: 1320px) {
  .resource-unit-status-bar {
    grid-template-columns: 1fr;
  }
}
</style>
