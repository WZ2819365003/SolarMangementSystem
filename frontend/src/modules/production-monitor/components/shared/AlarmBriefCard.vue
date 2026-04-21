<template>
  <app-section-card title="告警快报" subtitle="聚合商需要的最小告警汇总信息">
    <div class="alarm-brief-card" data-testid="alarm-brief-card">
      <div class="alarm-brief-card__summary">
        <div v-for="item in summaryItems" :key="item.label" class="alarm-brief-card__tile">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
      <div class="alarm-brief-card__latest">
        <div class="alarm-brief-card__latest-title">
          {{ alarm.latestTitle || '暂无告警' }}
        </div>
        <div class="alarm-brief-card__latest-time">
          {{ alarm.latestTime || '--' }}
        </div>
      </div>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'AlarmBriefCard',
  components: {
    AppSectionCard
  },
  props: {
    alarm: {
      type: Object,
      default: () => ({})
    }
  },
  computed: {
    summaryItems() {
      return [
        { label: '未处理', value: this.alarm.total || 0 },
        { label: '严重', value: this.alarm.critical || 0 },
        { label: '重要', value: this.alarm.major || 0 },
        { label: '一般', value: this.alarm.minor || 0 }
      ]
    }
  }
}
</script>

<style lang="less" scoped>
.alarm-brief-card__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.alarm-brief-card__tile {
  padding: 12px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.alarm-brief-card__tile span {
  display: block;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.alarm-brief-card__tile strong {
  display: block;
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 22px;
}

.alarm-brief-card__latest {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 4px;
  background: rgba(245, 156, 35, 0.08);
}

.alarm-brief-card__latest-title {
  color: var(--pvms-text-primary);
  font-size: 14px;
  line-height: 22px;
}

.alarm-brief-card__latest-time {
  margin-top: 8px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}
</style>
