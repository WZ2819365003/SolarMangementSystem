<template>
  <div v-loading="loading" class="production-view" data-testid="production-weather-view">
    <section class="pv-card-grid">
      <app-metric-card
        v-for="item in viewData.summary || []"
        :key="item.key"
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :helper="item.helper"
        :icon="item.icon"
        :accent="item.accent"
      />
    </section>

    <weather-trend-panel
      :trend-data="viewData.trend || {}"
      title="72 小时光伏天气趋势"
      subtitle="围绕辐照度、温度、风速和云量，判断后续出力稳定性"
      test-id="weather-trend-72h"
    />

    <app-section-card
      class="pv-data-panel"
      title="天气影响研判"
      subtitle="先用 mock 表达光伏天气判断，后续再接真实天气采集和预测加工"
    >
      <div class="pv-table-shell" data-testid="weather-impact-table">
        <el-table :data="viewData.impactTable || []" size="mini">
          <el-table-column prop="timeRange" label="时间段" width="150" />
          <el-table-column prop="weather" label="天气现象" width="120" />
          <el-table-column prop="irradianceRange" label="辐照度区间" width="150" />
          <el-table-column prop="temperatureRange" label="温度区间" width="120" />
          <el-table-column prop="windSpeedRange" label="风速区间" width="120" />
          <el-table-column prop="outputLevel" label="预计出力" width="120" />
          <el-table-column prop="suggestion" label="调度建议" min-width="240" />
        </el-table>
      </div>
    </app-section-card>
  </div>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import WeatherTrendPanel from '../shared/WeatherTrendPanel.vue'

export default {
  name: 'ProductionWeatherView',
  components: {
    AppMetricCard,
    AppSectionCard,
    WeatherTrendPanel
  },
  props: {
    viewData: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
}
</script>
