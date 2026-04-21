<template>
  <article class="app-metric-card" :class="resolveAccentClass(accent)">
    <div class="app-metric-card__header">
      <div class="app-metric-card__icon">
        <i :class="icon" />
      </div>
      <span class="app-metric-card__title">{{ title }}</span>
    </div>
    <div class="app-metric-card__value">
      {{ value }}
      <span class="app-metric-card__unit">{{ unit }}</span>
    </div>
    <div class="app-metric-card__helper">
      {{ helper }}
    </div>
  </article>
</template>

<script>
import { resolveAccentClass } from '@/utils/formatters'

export default {
  name: 'AppMetricCard',
  props: {
    title: {
      type: String,
      required: true
    },
    value: {
      type: [String, Number],
      required: true
    },
    unit: {
      type: String,
      default: ''
    },
    helper: {
      type: String,
      default: ''
    },
    icon: {
      type: String,
      default: 'el-icon-data-line'
    },
    accent: {
      type: String,
      default: 'teal'
    }
  },
  methods: {
    resolveAccentClass
  }
}
</script>

<style lang="less" scoped>
.app-metric-card {
  padding: 20px 22px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(180deg, rgba(14, 49, 106, 0.96), rgba(9, 29, 67, 0.98));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.app-metric-card__header {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--pvms-text-secondary);
}

.app-metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--pvms-accent, var(--pvms-primary));
  font-size: 18px;
}

.app-metric-card__value {
  margin-top: 18px;
  color: var(--pvms-text-primary);
  font-size: 30px;
  font-weight: 600;
  line-height: 1;
}

.app-metric-card__unit {
  margin-left: 8px;
  color: var(--pvms-text-muted);
  font-size: 14px;
  font-weight: 400;
}

.app-metric-card__helper {
  margin-top: 16px;
  color: var(--pvms-text-muted);
  font-size: 13px;
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
</style>
