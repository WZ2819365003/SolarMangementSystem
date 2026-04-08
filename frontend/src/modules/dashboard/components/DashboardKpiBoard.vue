<template>
  <app-section-card
    class="dashboard-kpi-board"
    data-testid="dashboard-kpi-board"
    title="核心 KPI 看板"
    :subtitle="`${payload.focusLabel || '全系统'} · 汇总视角`"
  >
    <template #extra>
      <span class="dashboard-kpi-board__stamp">{{ payload.updatedAt }}</span>
    </template>

    <div class="dashboard-kpi-board__groups">
      <div
        v-for="group in groupedItems"
        :key="group.label"
        class="dashboard-kpi-board__group"
      >
        <div class="dashboard-kpi-board__group-label">{{ group.label }}</div>
        <div class="dashboard-kpi-board__grid">
          <article
            v-for="item in group.items"
            :key="item.key"
            class="dashboard-kpi-board__tile"
            :class="resolveAccentClass(item.accent)"
          >
            <div class="dashboard-kpi-board__tile-head">
              <span class="dashboard-kpi-board__icon">
                <i :class="item.icon" />
              </span>
              <span class="dashboard-kpi-board__title">{{ item.title }}</span>
            </div>

            <div class="dashboard-kpi-board__value">
              {{ item.value }}
              <span class="dashboard-kpi-board__unit">{{ item.unit }}</span>
            </div>

            <div class="dashboard-kpi-board__helper">
              {{ item.helper }}
            </div>

            <div
              v-if="typeof item.changeRate === 'number'"
              class="dashboard-kpi-board__trend"
              :class="item.changeRate >= 0 ? 'is-up' : 'is-down'"
            >
              <i :class="item.changeRate >= 0 ? 'el-icon-top' : 'el-icon-bottom'" />
              <span>{{ Math.abs(item.changeRate) }}%</span>
              <span class="dashboard-kpi-board__trend-label">{{ item.changeLabel }}</span>
            </div>
          </article>
        </div>
      </div>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'
import { resolveAccentClass } from '@/utils/formatters'

export default {
  name: 'DashboardKpiBoard',
  components: {
    AppSectionCard
  },
  props: {
    payload: {
      type: Object,
      default: () => ({
        items: []
      })
    }
  },
  computed: {
    groupedItems() {
      const items = this.payload.items || []
      const map = {}
      const order = []
      items.forEach(function (item) {
        var label = item.group || '概览'
        if (!map[label]) {
          map[label] = []
          order.push(label)
        }
        map[label].push(item)
      })
      return order.map(function (label) {
        return { label: label, items: map[label] }
      })
    }
  },
  methods: {
    resolveAccentClass
  }
}
</script>

<style lang="less" scoped>
.dashboard-kpi-board__stamp {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-kpi-board__groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
  flex: 1;
}

.dashboard-kpi-board__group {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.dashboard-kpi-board__group-label {
  margin-bottom: 8px;
  padding-left: 2px;
  color: var(--pvms-text-secondary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.dashboard-kpi-board__group-label::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 13px;
  margin-right: 8px;
  border-radius: 2px;
  background: var(--pvms-primary);
  vertical-align: -1px;
}

.dashboard-kpi-board__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 12px;
  flex: 1;
}

.dashboard-kpi-board__tile {
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.dashboard-kpi-board__tile-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dashboard-kpi-board__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--pvms-accent, var(--pvms-primary));
  font-size: 16px;
}

.dashboard-kpi-board__title {
  color: var(--pvms-text-secondary);
  font-size: 13px;
}

.dashboard-kpi-board__value {
  margin-top: 14px;
  color: var(--pvms-text-primary);
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
}

.dashboard-kpi-board__unit {
  margin-left: 6px;
  color: var(--pvms-text-muted);
  font-size: 13px;
  font-weight: 400;
}

.dashboard-kpi-board__helper {
  margin-top: 12px;
  color: var(--pvms-text-muted);
  font-size: 12px;
  line-height: 18px;
}

.dashboard-kpi-board__trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  font-size: 12px;
  font-weight: 600;
}

.dashboard-kpi-board__trend.is-up {
  color: #67c23a;
}

.dashboard-kpi-board__trend.is-down {
  color: #f56c6c;
}

.dashboard-kpi-board__trend-label {
  color: var(--pvms-text-muted);
  font-weight: 400;
}

.is-teal {
  --pvms-accent: #06a299;
}

.is-blue {
  --pvms-accent: #1a8dff;
}

.is-emerald {
  --pvms-accent: #00b578;
}

.is-orange {
  --pvms-accent: #f59b23;
}

@media (max-width: 768px) {
  .dashboard-kpi-board__grid {
    grid-template-columns: 1fr;
  }
}
</style>
