<template>
  <section class="station-overview-hero" data-testid="station-overview-hero">
    <div class="station-overview-hero__main">
      <el-button type="text" icon="el-icon-arrow-left" @click="$emit('back')">
        返回资源单元列表
      </el-button>
      <div class="station-overview-hero__title-row">
        <h2 class="station-overview-hero__title">
          {{ info.name }}
        </h2>
        <el-tag size="mini" :type="resolveTagType(info.statusLabel)">
          {{ info.statusLabel }}
        </el-tag>
      </div>
      <p class="station-overview-hero__summary">
        {{ info.strategyLabel }}
      </p>
      <div class="station-overview-hero__facts">
        <span>区域 {{ info.region }}</span>
        <span>纳管 {{ info.stationCount }} 座电站</span>
        <span>可调 {{ info.dispatchableCapacityMw }} MW</span>
        <span>模式 {{ info.dispatchMode }}</span>
      </div>
    </div>
    <div class="station-overview-hero__weather">
      <div class="station-overview-hero__weather-title">
        当前天气
      </div>
      <div class="station-overview-hero__weather-value">
        <i class="el-icon-sunny" />
        <span>{{ weather.temperature }}°C {{ weather.weather }}</span>
      </div>
      <div class="station-overview-hero__weather-meta">
        辐照 {{ weather.irradiance }} W/m² · 湿度 {{ weather.humidity }}%
      </div>
      <div class="station-overview-hero__weather-meta">
        风速 {{ weather.windSpeed }} m/s
      </div>
    </div>
  </section>
</template>

<script>
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'StationOverviewHero',
  props: {
    info: {
      type: Object,
      default: () => ({})
    },
    weather: {
      type: Object,
      default: () => ({})
    }
  },
  methods: {
    resolveTagType
  }
}
</script>

<style lang="less" scoped>
.station-overview-hero {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 28px;
  border: 1px solid var(--pvms-border-soft);
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(8, 44, 93, 0.98), rgba(6, 25, 59, 0.96));
  box-shadow: 0 18px 34px rgba(4, 16, 40, 0.24);
}

.station-overview-hero__main {
  flex: 1;
  min-width: 0;
}

.station-overview-hero__title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
}

.station-overview-hero__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 28px;
  line-height: 38px;
}

.station-overview-hero__summary {
  margin: 10px 0 0;
  color: var(--pvms-text-secondary);
  line-height: 22px;
}

.station-overview-hero__facts {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.station-overview-hero__facts span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.station-overview-hero__weather {
  min-width: 248px;
  padding: 18px 20px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
}

.station-overview-hero__weather-title {
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.station-overview-hero__weather-value {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 18px;
  color: var(--pvms-text-primary);
  font-size: 22px;
  font-weight: 600;
}

.station-overview-hero__weather-meta {
  margin-top: 10px;
  color: var(--pvms-text-secondary);
  line-height: 22px;
}

@media (max-width: 1280px) {
  .station-overview-hero {
    flex-direction: column;
  }

  .station-overview-hero__weather {
    min-width: auto;
  }
}
</style>
