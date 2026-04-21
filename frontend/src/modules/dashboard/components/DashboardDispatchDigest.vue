<template>
  <article
    class="dashboard-dispatch-digest"
    data-testid="dashboard-dispatch-digest"
  >
    <header class="dashboard-dispatch-digest__header">
      <div>
        <h3 class="dashboard-dispatch-digest__title">
          调度摘要
        </h3>
        <p class="dashboard-dispatch-digest__subtitle">
          值班态势与优先事项
        </p>
      </div>
    </header>

    <div class="dashboard-dispatch-digest__headline">
      {{ headline }}
    </div>

    <div class="dashboard-dispatch-digest__grid">
      <section
        v-for="item in items"
        :key="item.key"
        class="dashboard-dispatch-digest__tile"
        :class="`is-${item.accent}`"
      >
        <div class="dashboard-dispatch-digest__label">
          {{ item.label }}
        </div>
        <div class="dashboard-dispatch-digest__value">
          {{ item.value }}
          <span class="dashboard-dispatch-digest__unit">{{ item.unit }}</span>
        </div>
        <div class="dashboard-dispatch-digest__helper">
          {{ item.helper }}
        </div>

        <div
          class="dashboard-dispatch-digest__progress"
          data-testid="dashboard-dispatch-progress"
        >
          <span :style="{ width: `${Math.max(8, item.progress || 0)}%` }" />
        </div>
      </section>
    </div>

    <div
      class="dashboard-dispatch-digest__actions"
      data-testid="dashboard-dispatch-actions"
    >
      <div
        v-for="item in actions"
        :key="item.key"
        class="dashboard-dispatch-digest__action-item"
        data-testid="dashboard-dispatch-action-item"
      >
        <span class="dashboard-dispatch-digest__action-label">{{ item.label }}</span>
        <span class="dashboard-dispatch-digest__action-text">{{ item.text }}</span>
      </div>
    </div>
  </article>
</template>

<script>
export default {
  name: 'DashboardDispatchDigest',
  props: {
    items: {
      type: Array,
      default: () => []
    },
    actions: {
      type: Array,
      default: () => []
    },
    headline: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-dispatch-digest {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  background:
    radial-gradient(circle at top right, rgba(26, 141, 255, 0.14), transparent 30%),
    linear-gradient(180deg, rgba(8, 31, 72, 0.88), rgba(7, 24, 56, 0.96));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.dashboard-dispatch-digest__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 15px;
  line-height: 22px;
}

.dashboard-dispatch-digest__subtitle {
  margin: 4px 0 0;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-dispatch-digest__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.dashboard-dispatch-digest__headline {
  margin-top: 14px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--pvms-text-secondary);
  font-size: 12px;
  line-height: 18px;
}

.dashboard-dispatch-digest__tile {
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.dashboard-dispatch-digest__label {
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.dashboard-dispatch-digest__value {
  margin-top: 12px;
  color: var(--pvms-text-primary);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.dashboard-dispatch-digest__unit {
  margin-left: 4px;
  color: var(--pvms-text-muted);
  font-size: 12px;
  font-weight: 400;
}

.dashboard-dispatch-digest__helper {
  margin-top: 8px;
  color: var(--pvms-text-muted);
  font-size: 12px;
  line-height: 18px;
}

.dashboard-dispatch-digest__progress {
  height: 6px;
  margin-top: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.dashboard-dispatch-digest__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(26, 141, 255, 0.9), rgba(66, 230, 149, 0.9));
}

.dashboard-dispatch-digest__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

.dashboard-dispatch-digest__action-item {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 10px;
  padding: 10px 12px;
  border-left: 3px solid rgba(26, 141, 255, 0.55);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
}

.dashboard-dispatch-digest__action-label {
  color: #8fc8ff;
  font-size: 12px;
  font-weight: 600;
}

.dashboard-dispatch-digest__action-text {
  color: var(--pvms-text-secondary);
  font-size: 12px;
  line-height: 18px;
}

.is-orange {
  box-shadow: inset 0 0 0 1px rgba(245, 155, 35, 0.14);
}

.is-red {
  box-shadow: inset 0 0 0 1px rgba(245, 108, 108, 0.14);
}

.is-blue {
  box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.14);
}

.is-teal {
  box-shadow: inset 0 0 0 1px rgba(6, 162, 153, 0.14);
}
</style>
