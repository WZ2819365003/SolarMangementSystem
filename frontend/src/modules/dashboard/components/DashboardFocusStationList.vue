<template>
  <article
    class="dashboard-focus-stations"
    data-testid="dashboard-focus-stations"
    @mouseenter="pauseTicker"
    @mouseleave="resumeTicker"
  >
    <header class="dashboard-focus-stations__header">
      <div>
        <h3 class="dashboard-focus-stations__title">
          重点关注电站
        </h3>
        <p class="dashboard-focus-stations__subtitle">
          选中站点优先，异常状态自动前置
        </p>
      </div>
      <span class="dashboard-focus-stations__tag">地图联动</span>
    </header>

    <div class="dashboard-focus-stations__list">
      <button
        v-for="item in visibleItems"
        :key="item.id"
        type="button"
        class="dashboard-focus-stations__item"
        :class="{ 'is-selected': item.id === selectedStationId }"
        data-testid="dashboard-focus-station-item"
        @click="$emit('select', item.id)"
      >
        <div class="dashboard-focus-stations__item-head">
          <div class="dashboard-focus-stations__name-wrap">
            <div class="dashboard-focus-stations__name">
              {{ item.name }}
            </div>
            <div class="dashboard-focus-stations__meta">
              {{ item.region }} · {{ item.metaText }}
            </div>
          </div>
          <span
            class="dashboard-focus-stations__status"
            :style="{ '--status-color': item.statusColor }"
          >
            {{ item.statusLabel }}
          </span>
        </div>

        <div class="dashboard-focus-stations__item-foot">
          <span class="dashboard-focus-stations__issue">{{ item.issueLabel }}</span>
          <span class="dashboard-focus-stations__metric">{{ item.metricText }}</span>
        </div>

        <div class="dashboard-focus-stations__badges">
          <span
            v-for="badge in item.badges || []"
            :key="`${item.id}-${badge.key}`"
            class="dashboard-focus-stations__badge"
            data-testid="dashboard-focus-station-badge"
          >
            {{ badge.label }}
          </span>
        </div>
      </button>
    </div>

    <div
      v-if="pageCount > 1"
      class="dashboard-focus-stations__indicators"
      data-testid="dashboard-focus-station-indicators"
    >
      <button
        v-for="page in pageCount"
        :key="`indicator-${page - 1}`"
        type="button"
        class="dashboard-focus-stations__indicator"
        :class="{ 'is-active': currentPage === page - 1 }"
        data-testid="dashboard-focus-station-indicator"
        @click="handleIndicatorClick(page - 1)"
      />
    </div>
  </article>
</template>

<script>
export default {
  name: 'DashboardFocusStationList',
  props: {
    items: {
      type: Array,
      default: () => []
    },
    selectedStationId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      currentPage: 0,
      pageSize: 3,
      timerId: null
    }
  },
  computed: {
    pageCount() {
      if (this.items.length <= this.pageSize) {
        return 1
      }
      return this.items.length - this.pageSize + 1
    },
    visibleItems() {
      if (this.items.length <= this.pageSize) {
        return this.items
      }
      return this.items.slice(this.currentPage, this.currentPage + this.pageSize)
    }
  },
  watch: {
    items: {
      immediate: true,
      handler() {
        this.currentPage = 0
        this.resumeTicker()
      }
    }
  },
  beforeDestroy() {
    this.pauseTicker()
  },
  methods: {
    handleIndicatorClick(page) {
      this.currentPage = page
    },
    pauseTicker() {
      if (this.timerId) {
        clearInterval(this.timerId)
        this.timerId = null
      }
    },
    resumeTicker() {
      this.pauseTicker()
      if (this.pageCount <= 1) {
        return
      }
      this.timerId = window.setInterval(() => {
        this.currentPage = (this.currentPage + 1) % this.pageCount
      }, 3200)
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-focus-stations {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  background:
    linear-gradient(180deg, rgba(8, 31, 72, 0.88), rgba(7, 24, 56, 0.96));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.dashboard-focus-stations__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 14px;
}

.dashboard-focus-stations__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 15px;
  line-height: 22px;
}

.dashboard-focus-stations__subtitle {
  margin: 4px 0 0;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-focus-stations__tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(26, 141, 255, 0.12);
  color: #8fc8ff;
  font-size: 11px;
}

.dashboard-focus-stations__list {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 8px;
}

.dashboard-focus-stations__item {
  display: flex;
  flex-direction: column;
  padding: 11px 13px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease, background 0.2s ease;
}

.dashboard-focus-stations__item:hover,
.dashboard-focus-stations__item.is-selected {
  border-color: rgba(64, 158, 255, 0.5);
  background: rgba(26, 141, 255, 0.08);
  transform: translateY(-1px);
}

.dashboard-focus-stations__item-head,
.dashboard-focus-stations__item-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dashboard-focus-stations__name {
  color: var(--pvms-text-primary);
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
}

.dashboard-focus-stations__meta {
  margin-top: 4px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-focus-stations__status {
  flex: none;
  padding: 3px 10px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--status-color);
  font-size: 12px;
  font-weight: 600;
}

.dashboard-focus-stations__item-foot {
  margin-top: 8px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.dashboard-focus-stations__issue {
  color: var(--pvms-text-secondary);
}

.dashboard-focus-stations__metric {
  color: var(--pvms-text-primary);
  font-weight: 600;
}

.dashboard-focus-stations__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.dashboard-focus-stations__badge {
  padding: 4px 8px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--pvms-text-muted);
  font-size: 11px;
  line-height: 16px;
}

.dashboard-focus-stations__indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.dashboard-focus-stations__indicator {
  width: 18px;
  height: 6px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.dashboard-focus-stations__indicator.is-active {
  background: linear-gradient(90deg, #1a8dff, #42e695);
  transform: scaleX(1.1);
}
</style>
