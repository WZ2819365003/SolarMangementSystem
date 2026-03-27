<template>
  <article
    class="station-card-item"
    :class="`is-${station.status}`"
    data-testid="station-card-item"
    @click="$emit('select', station)"
  >
    <header class="station-card-item__header">
      <div>
        <h3 class="station-card-item__title">
          {{ station.name }}
        </h3>
        <p class="station-card-item__region">
          {{ station.region }}
        </p>
      </div>
      <div class="station-card-item__status">
        <el-badge :value="station.alarmCount" :hidden="!station.alarmCount">
          <el-tag size="mini" :type="resolveTagType(station.statusLabel)">
            {{ station.statusLabel }}
          </el-tag>
        </el-badge>
      </div>
    </header>

    <div class="station-card-item__metrics">
      <div class="station-card-item__metric">
        <span class="station-card-item__label">纳管电站数</span>
        <strong>{{ station.stationCount }} 座</strong>
      </div>
      <div class="station-card-item__metric">
        <span class="station-card-item__label">可调容量</span>
        <strong>{{ station.dispatchableCapacityMw }} MW</strong>
      </div>
      <div class="station-card-item__metric">
        <span class="station-card-item__label">实时出力</span>
        <strong>{{ station.realtimePowerMw }} MW</strong>
      </div>
      <div class="station-card-item__metric">
        <span class="station-card-item__label">预测偏差</span>
        <strong :class="deviationClass">{{ station.forecastDeviationRate }}%</strong>
      </div>
    </div>

    <div class="station-card-item__progress">
      <div class="station-card-item__progress-meta">
        <span>在线率</span>
        <span>{{ station.onlineRate }}%</span>
      </div>
      <el-progress :percentage="station.onlineRate" :show-text="false" />
    </div>

    <footer class="station-card-item__footer">
      <div class="station-card-item__weather">
        <i class="el-icon-sunny" />
        <span>{{ station.weather.temperature }}°C {{ station.weather.weather }}</span>
      </div>
      <span class="station-card-item__irradiance">
        辐照 {{ station.weather.irradiance }} W/m²
      </span>
    </footer>
  </article>
</template>

<script>
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'StationCardItem',
  props: {
    station: {
      type: Object,
      required: true
    }
  },
  computed: {
    deviationClass() {
      return {
        'station-card-item__deviation-negative': this.station.forecastDeviationRate < -5,
        'station-card-item__deviation-positive': this.station.forecastDeviationRate > 3
      }
    }
  },
  methods: {
    resolveTagType
  }
}
</script>

<style lang="less" scoped>
.station-card-item {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 280px;
  padding: 20px 22px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-left: 4px solid transparent;
  border-radius: 4px;
  background: linear-gradient(180deg, rgba(11, 40, 86, 0.98), rgba(8, 28, 62, 0.96));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.station-card-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(4, 16, 40, 0.22);
}

.station-card-item.is-normal {
  border-left-color: #67c23a;
}

.station-card-item.is-warning {
  border-left-color: #e6a23c;
}

.station-card-item.is-fault {
  border-left-color: #f56c6c;
}

.station-card-item.is-maintenance {
  border-left-color: #409eff;
}

.station-card-item.is-offline {
  border-left-color: #909399;
}

.station-card-item__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.station-card-item__title {
  margin: 0;
  color: var(--pvms-text-primary);
  font-size: 18px;
  line-height: 28px;
}

.station-card-item__region {
  margin: 8px 0 0;
  color: var(--pvms-text-muted);
  font-size: 13px;
}

.station-card-item__metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.station-card-item__metric {
  padding: 12px 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.station-card-item__label {
  display: block;
  margin-bottom: 8px;
  color: var(--pvms-text-muted);
  font-size: 12px;
}

.station-card-item__metric strong {
  color: var(--pvms-text-primary);
  font-size: 15px;
}

.station-card-item__deviation-negative {
  color: #f56c6c;
}

.station-card-item__deviation-positive {
  color: #e6a23c;
}

.station-card-item__progress {
  margin-top: auto;
}

.station-card-item__progress-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.station-card-item__progress /deep/ .el-progress-bar__outer {
  background: rgba(255, 255, 255, 0.08);
}

.station-card-item__progress /deep/ .el-progress-bar__inner {
  background: linear-gradient(90deg, rgba(6, 162, 153, 0.95), rgba(26, 141, 255, 0.95));
}

.station-card-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.station-card-item__weather {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
