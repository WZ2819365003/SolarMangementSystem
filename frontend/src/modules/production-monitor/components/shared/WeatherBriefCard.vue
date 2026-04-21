<template>
  <app-section-card title="光伏天气快报" subtitle="同一资源单元内电站共享同城市天气剖面">
    <div class="weather-brief-card" data-testid="weather-brief-card">
      <div class="weather-brief-card__headline">
        <div>
          <div class="weather-brief-card__weather">
            {{ weather.weather || '--' }}
          </div>
          <div class="weather-brief-card__temperature">
            {{ weather.temperature || '--' }}°C
          </div>
        </div>
        <el-tag size="mini" effect="dark" type="success">
          {{ weather.cloudiness || '共享剖面' }}
        </el-tag>
      </div>
      <div class="weather-brief-card__metrics">
        <div>
          <span>辐照度</span>
          <strong>{{ weather.irradiance || '--' }} W/m²</strong>
        </div>
        <div>
          <span>湿度</span>
          <strong>{{ weather.humidity || '--' }}%</strong>
        </div>
        <div>
          <span>风速</span>
          <strong>{{ weather.windSpeed || '--' }} m/s</strong>
        </div>
      </div>
      <p class="weather-brief-card__conclusion">
        {{ weather.conclusion || '暂无天气结论' }}
      </p>
    </div>
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'WeatherBriefCard',
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
.weather-brief-card__headline {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.weather-brief-card__weather {
  color: var(--pvms-text-secondary);
  font-size: 13px;
}

.weather-brief-card__temperature {
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 36px;
  font-weight: 700;
}

.weather-brief-card__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}

.weather-brief-card__metrics div {
  padding: 12px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.weather-brief-card__metrics span {
  display: block;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.weather-brief-card__metrics strong {
  display: block;
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 14px;
}

.weather-brief-card__conclusion {
  margin: 16px 0 0;
  padding: 12px 14px;
  border-left: 3px solid rgba(6, 162, 153, 0.9);
  border-radius: 0 4px 4px 0;
  background: rgba(6, 162, 153, 0.08);
  color: var(--pvms-text-secondary);
  line-height: 22px;
}
</style>
