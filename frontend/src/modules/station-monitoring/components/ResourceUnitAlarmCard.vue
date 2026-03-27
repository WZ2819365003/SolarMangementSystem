<template>
  <app-section-card
    title="告警概况"
    subtitle="聚合视角下的当前异常分布"
    data-testid="resource-unit-alarm-card"
  >
    <div class="resource-unit-alarm-card">
      <div class="resource-unit-alarm-card__top">
        <div class="resource-unit-alarm-card__total">
          <span>未处理告警</span>
          <strong>{{ alarmSummary.total }}</strong>
        </div>
        <div class="resource-unit-alarm-card__badges">
          <el-tag size="mini" type="danger">
            严重 {{ alarmSummary.critical }}
          </el-tag>
          <el-tag size="mini" type="warning">
            重要 {{ alarmSummary.major }}
          </el-tag>
          <el-tag size="mini">
            一般 {{ alarmSummary.minor }}
          </el-tag>
        </div>
      </div>
      <div class="resource-unit-alarm-card__latest">
        <div class="resource-unit-alarm-card__latest-label">
          最新告警
        </div>
        <div class="resource-unit-alarm-card__latest-title">
          {{ alarmSummary.latestTitle }}
        </div>
        <div class="resource-unit-alarm-card__latest-time">
          {{ alarmSummary.latestTime }}
        </div>
      </div>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'ResourceUnitAlarmCard',
  components: {
    AppSectionCard
  },
  props: {
    alarmSummary: {
      type: Object,
      default: () => ({})
    }
  }
}
</script>

<style lang="less" scoped>
.resource-unit-alarm-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resource-unit-alarm-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.resource-unit-alarm-card__total span {
  display: block;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.resource-unit-alarm-card__total strong {
  display: block;
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 30px;
  line-height: 1;
}

.resource-unit-alarm-card__badges {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
}

.resource-unit-alarm-card__latest {
  padding: 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.resource-unit-alarm-card__latest-label {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.resource-unit-alarm-card__latest-title {
  margin-top: 8px;
  color: var(--pvms-text-primary);
  line-height: 22px;
}

.resource-unit-alarm-card__latest-time {
  margin-top: 10px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

@media (max-width: 1380px) {
  .resource-unit-alarm-card__top {
    flex-direction: column;
  }
}
</style>
