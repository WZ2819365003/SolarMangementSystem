<template>
  <app-section-card
    title="成员电站摘要"
    subtitle="用于解释资源单元出力、天气和异常来源"
    data-testid="resource-unit-member-stations"
  >
    <div class="resource-unit-member-stations">
      <article
        v-for="station in stations"
        :key="station.id"
        class="resource-unit-member-stations__item"
        data-testid="resource-unit-member-station-item"
      >
        <div class="resource-unit-member-stations__header">
          <div>
            <h4>{{ station.name }}</h4>
            <p>{{ station.weather.weather }} · {{ station.weather.temperature }}°C</p>
          </div>
          <el-tag size="mini" :type="resolveTagType(station.statusLabel)">
            {{ station.statusLabel }}
          </el-tag>
        </div>
        <div class="resource-unit-member-stations__metrics">
          <span>实时出力 {{ station.realtimePowerKw }} kW</span>
          <span>在线率 {{ station.onlineRate }}%</span>
          <span>告警 {{ station.alarmCount }}</span>
        </div>
      </article>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'ResourceUnitMemberStationsCard',
  components: {
    AppSectionCard
  },
  props: {
    stations: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    resolveTagType
  }
}
</script>

<style lang="less" scoped>
.resource-unit-member-stations {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.resource-unit-member-stations__item {
  padding: 16px;
  border-radius: 4px;
  background: linear-gradient(180deg, rgba(13, 43, 91, 0.96), rgba(9, 29, 67, 0.96));
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.resource-unit-member-stations__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.resource-unit-member-stations__header h4 {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 15px;
}

.resource-unit-member-stations__header p {
  margin: 8px 0 0;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.resource-unit-member-stations__metrics {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 16px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

@media (max-width: 1380px) {
  .resource-unit-member-stations {
    grid-template-columns: 1fr;
  }
}
</style>
