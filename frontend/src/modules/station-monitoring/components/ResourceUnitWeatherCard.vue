<template>
  <app-section-card
    title="天气指标"
    subtitle="天气条件对出力与可调空间的影响"
    data-testid="resource-unit-weather-card"
  >
    <div class="resource-unit-weather-card">
      <div class="resource-unit-weather-card__top">
        <div>
          <div class="resource-unit-weather-card__value">
            {{ weather.temperature }}°C
          </div>
          <div class="resource-unit-weather-card__weather">
            {{ weather.weather }}
          </div>
        </div>
        <el-tag size="mini" type="success">
          天气判断
        </el-tag>
      </div>
      <div class="resource-unit-weather-card__metrics">
        <div class="resource-unit-weather-card__metric">
          <span>辐照度</span>
          <strong>{{ weather.irradiance }} W/m²</strong>
        </div>
        <div class="resource-unit-weather-card__metric">
          <span>湿度</span>
          <strong>{{ weather.humidity }}%</strong>
        </div>
        <div class="resource-unit-weather-card__metric">
          <span>风速</span>
          <strong>{{ weather.windSpeed }} m/s</strong>
        </div>
      </div>
      <div class="resource-unit-weather-card__suggestion">
        {{ weather.suggestion }}
      </div>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'ResourceUnitWeatherCard',
  components: {
    AppSectionCard
  },
  props: {
    weather: {
      type: Object,
      default: () => ({})
    }
  }
}
</script>

<style lang="less" scoped>
.resource-unit-weather-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resource-unit-weather-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.resource-unit-weather-card__value {
  color: var(--pvms-text-primary);
  font-size: 32px;
  font-weight: 700;
}

.resource-unit-weather-card__weather {
  margin-top: 6px;
  color: var(--pvms-text-secondary);
}

.resource-unit-weather-card__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.resource-unit-weather-card__metric {
  padding: 12px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.resource-unit-weather-card__metric span {
  display: block;
  margin-bottom: 6px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.resource-unit-weather-card__metric strong {
  color: var(--pvms-text-primary);
  font-size: 14px;
}

.resource-unit-weather-card__suggestion {
  padding: 12px 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, rgba(6, 162, 153, 0.12), rgba(26, 141, 255, 0.08));
  color: var(--pvms-text-secondary);
  line-height: 22px;
}

@media (max-width: 1380px) {
  .resource-unit-weather-card__metrics {
    grid-template-columns: 1fr;
  }
}
</style>
