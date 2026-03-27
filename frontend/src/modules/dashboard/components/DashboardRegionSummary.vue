<template>
  <article
    class="dashboard-region-summary"
    data-testid="dashboard-region-summary"
  >
    <header class="dashboard-region-summary__header">
      <div>
        <h3 class="dashboard-region-summary__title">
          区域态势
        </h3>
        <p class="dashboard-region-summary__subtitle">
          区域容量与在线率分布
        </p>
      </div>
    </header>

    <div class="dashboard-region-summary__list">
      <section
        v-for="item in items"
        :key="item.regionName"
        class="dashboard-region-summary__item"
      >
        <div class="dashboard-region-summary__top">
          <span class="dashboard-region-summary__name">{{ item.regionName }}</span>
          <span class="dashboard-region-summary__rate">{{ item.onlineRate }}%</span>
        </div>
        <div class="dashboard-region-summary__meta">
          {{ item.stationCount }} 座 · {{ item.capacityMw }} MWp
          <span v-if="item.riskCount" class="dashboard-region-summary__risk">
            {{ item.riskCount }} 座关注
          </span>
        </div>
        <div class="dashboard-region-summary__bar">
          <span :style="{ width: `${Math.max(8, item.onlineRate)}%` }" />
        </div>
      </section>
    </div>
  </article>
</template>

<script>
export default {
  name: 'DashboardRegionSummary',
  props: {
    items: {
      type: Array,
      default: () => []
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-region-summary {
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  background:
    linear-gradient(180deg, rgba(8, 31, 72, 0.88), rgba(7, 24, 56, 0.96));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.dashboard-region-summary__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 15px;
  line-height: 22px;
}

.dashboard-region-summary__subtitle {
  margin: 4px 0 0;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-region-summary__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 14px;
}

.dashboard-region-summary__item {
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
}

.dashboard-region-summary__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.dashboard-region-summary__name {
  color: var(--pvms-text-primary);
  font-size: 13px;
  font-weight: 600;
}

.dashboard-region-summary__rate {
  color: #7fe7ff;
  font-size: 12px;
  font-weight: 700;
}

.dashboard-region-summary__meta {
  margin-top: 6px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-region-summary__risk {
  color: #f59b23;
}

.dashboard-region-summary__bar {
  height: 8px;
  margin-top: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.dashboard-region-summary__bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #0ab7ff, #42e695);
  box-shadow: 0 0 12px rgba(10, 183, 255, 0.24);
}
</style>
