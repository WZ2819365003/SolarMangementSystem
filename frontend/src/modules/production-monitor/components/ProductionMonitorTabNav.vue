<template>
  <section class="production-monitor-tab-nav" data-testid="production-monitor-tab-nav">
    <button
      v-for="item in tabs"
      :key="item.key"
      type="button"
      class="production-monitor-tab-nav__item"
      :class="{ 'is-active': item.key === activeKey }"
      :data-testid="`production-tab-${item.key}`"
      @click="$emit('change', item.key)"
    >
      {{ item.label }}
    </button>
  </section>
</template>

<script>
export default {
  name: 'ProductionMonitorTabNav',
  props: {
    tabs: {
      type: Array,
      default: () => []
    },
    activeKey: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="less" scoped>
.production-monitor-tab-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--pvms-border-soft);
  border-radius: 4px;
  background: linear-gradient(180deg, rgba(10, 40, 88, 0.96), rgba(7, 27, 66, 0.96));
}

.production-monitor-tab-nav__item {
  min-width: 132px;
  height: 46px;
  padding: 0 20px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--pvms-text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.production-monitor-tab-nav__item:hover {
  color: var(--pvms-text-primary);
  background: rgba(255, 255, 255, 0.04);
}

.production-monitor-tab-nav__item.is-active {
  color: #fff;
  background: linear-gradient(90deg, rgba(6, 162, 153, 0.32), rgba(26, 141, 255, 0.28));
  box-shadow: inset 0 -2px 0 rgba(82, 194, 255, 0.85);
}
</style>
