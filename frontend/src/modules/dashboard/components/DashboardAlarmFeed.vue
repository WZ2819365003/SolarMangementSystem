<template>
  <app-section-card
    class="dashboard-alarm-feed"
    data-testid="dashboard-alarm-feed"
    title="实时告警"
    subtitle="未闭环告警滚动列表"
  >
    <template #extra>
      <el-button type="text" @click="$emit('refresh')">
        刷新
      </el-button>
    </template>

    <div class="dashboard-alarm-feed__badges">
      <button
        v-for="item in badgeItems"
        :key="item.level"
        class="dashboard-alarm-feed__badge"
        :class="{ 'is-active': activeLevel === item.level }"
        :style="{ '--badge-color': item.color }"
        @click="$emit('level-change', activeLevel === item.level ? '' : item.level)"
      >
        {{ item.label }} {{ item.count }}
      </button>
    </div>

    <div
      class="dashboard-alarm-feed__list"
      @mouseenter="pauseTicker"
      @mouseleave="resumeTicker"
    >
      <button
        v-for="item in visibleItems"
        :key="item.id"
        class="dashboard-alarm-feed__item"
        :style="{ '--alarm-color': resolveAlarmColor(item.level) }"
        @click="$emit('open-alarm', item)"
      >
        <div class="dashboard-alarm-feed__time">
          {{ item.time }}
        </div>
        <div class="dashboard-alarm-feed__content">
          <div class="dashboard-alarm-feed__title">
            {{ item.deviceName }} · {{ item.description }}
          </div>
          <div class="dashboard-alarm-feed__meta">
            {{ item.stationName }} · {{ item.levelLabel }} · {{ item.status }}
          </div>
        </div>
      </button>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

const alarmMeta = {
  critical: {
    label: '紧急',
    color: '#f56c6c'
  },
  major: {
    label: '重要',
    color: '#e6a23c'
  },
  minor: {
    label: '一般',
    color: '#f2c037'
  },
  hint: {
    label: '提示',
    color: '#409eff'
  }
}

export default {
  name: 'DashboardAlarmFeed',
  components: {
    AppSectionCard
  },
  props: {
    payload: {
      type: Object,
      default: () => ({
        summary: {},
        items: []
      })
    },
    activeLevel: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      cursor: 0,
      timerId: null
    }
  },
  computed: {
    badgeItems() {
      return Object.keys(alarmMeta).map(key => ({
        level: key,
        label: alarmMeta[key].label,
        color: alarmMeta[key].color,
        count: this.payload.summary[key] || 0
      }))
    },
    visibleItems() {
      const list = this.payload.items || []
      if (!list.length) {
        return []
      }

      const rotated = list.slice(this.cursor).concat(list.slice(0, this.cursor))
      return rotated.slice(0, Math.min(4, rotated.length))
    }
  },
  watch: {
    'payload.items': {
      immediate: true,
      handler() {
        this.cursor = 0
        this.resumeTicker()
      }
    }
  },
  beforeDestroy() {
    this.pauseTicker()
  },
  methods: {
    resolveAlarmColor(level) {
      return (alarmMeta[level] || alarmMeta.hint).color
    },
    pauseTicker() {
      if (this.timerId) {
        clearInterval(this.timerId)
        this.timerId = null
      }
    },
    resumeTicker() {
      this.pauseTicker()
      if ((this.payload.items || []).length <= 1) {
        return
      }

      this.timerId = window.setInterval(() => {
        const length = (this.payload.items || []).length
        if (!length) {
          return
        }
        this.cursor = (this.cursor + 1) % length
      }, 3000)
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-alarm-feed__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.dashboard-alarm-feed__badge {
  height: 30px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.03);
  color: var(--pvms-text-secondary);
  cursor: pointer;
}

.dashboard-alarm-feed__badge.is-active {
  border-color: var(--badge-color);
  color: #fff;
}

.dashboard-alarm-feed__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dashboard-alarm-feed__item {
  display: grid;
  grid-template-columns: 54px 1fr;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 14px 14px 12px;
  border: none;
  border-left: 4px solid var(--alarm-color);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  text-align: left;
  cursor: pointer;
}

.dashboard-alarm-feed__time {
  color: var(--alarm-color);
  font-size: 12px;
  font-weight: 700;
}

.dashboard-alarm-feed__title {
  color: var(--pvms-text-primary);
  font-size: 13px;
  line-height: 20px;
}

.dashboard-alarm-feed__meta {
  margin-top: 6px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}
</style>
