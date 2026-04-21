<template>
  <app-section-card
    class="dashboard-weather-card"
    data-testid="dashboard-weather-card"
    title="天气信息"
    :subtitle="payload.stationName || '当前关注电站'"
  >
    <template #extra>
      <el-tag size="mini" effect="plain" type="success">
        未来 72h
      </el-tag>
    </template>

    <div class="dashboard-weather-card__hero">
      <div>
        <div class="dashboard-weather-card__condition">
          {{ payload.current.weather }}
        </div>
        <div class="dashboard-weather-card__temperature">
          {{ payload.current.temperature }}°C
        </div>
      </div>

      <div class="dashboard-weather-card__metrics">
        <div>辐照度 {{ payload.current.irradiance }} W/m²</div>
        <div>湿度 {{ payload.current.humidity }}%</div>
        <div>风速 {{ payload.current.windSpeed }} m/s</div>
        <div>{{ payload.current.windDirection }}</div>
      </div>
    </div>

    <div class="dashboard-weather-card__forecast">
      <article
        v-for="item in payload.forecast || []"
        :key="item.date"
        class="dashboard-weather-card__forecast-item"
      >
        <div class="dashboard-weather-card__forecast-date">
          {{ item.date }}
        </div>
        <div class="dashboard-weather-card__forecast-weather">
          {{ item.weather }}
        </div>
        <div class="dashboard-weather-card__forecast-temp">
          {{ item.tempLow }}° ~ {{ item.tempHigh }}°
        </div>
        <div class="dashboard-weather-card__forecast-irradiance">
          ~{{ item.irradianceEstimate }} W/m²
        </div>
      </article>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'DashboardWeatherCard',
  components: {
    AppSectionCard
  },
  props: {
    payload: {
      type: Object,
      default: () => ({
        current: {},
        forecast: []
      })
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-weather-card__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border-radius: 4px;
  background:
    radial-gradient(circle at top right, rgba(250, 200, 50, 0.18), transparent 35%),
    linear-gradient(135deg, rgba(8, 40, 91, 0.96), rgba(8, 28, 61, 0.96));
}

.dashboard-weather-card__condition {
  color: var(--pvms-text-secondary);
  font-size: 13px;
}

.dashboard-weather-card__temperature {
  margin-top: 10px;
  color: var(--pvms-text-primary);
  font-size: 34px;
  font-weight: 700;
}

.dashboard-weather-card__metrics {
  display: grid;
  gap: 8px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
  text-align: right;
}

.dashboard-weather-card__forecast {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.dashboard-weather-card__forecast-item {
  padding: 14px 12px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.03);
  text-align: center;
}

.dashboard-weather-card__forecast-date {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.dashboard-weather-card__forecast-weather {
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.dashboard-weather-card__forecast-temp {
  margin-top: 8px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.dashboard-weather-card__forecast-irradiance {
  margin-top: 8px;
  color: var(--pvms-primary);
  font-size: 12px;
}

@media (max-width: 900px) {
  .dashboard-weather-card__hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .dashboard-weather-card__metrics {
    text-align: left;
  }

  .dashboard-weather-card__forecast {
    grid-template-columns: 1fr;
  }
}
</style>
