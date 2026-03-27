<template>
  <div v-loading="loading" class="production-view" data-testid="production-output-view">
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

    <section class="pv-split-grid production-output-view__top-grid">
      <power-curve-panel
        :chart-data="viewData"
        title="出力预测 vs 实际"
        subtitle="围绕资源单元总出力的主曲线，直接查看偏差和白天峰值。"
      />
      <weather-trend-panel
        :trend-data="viewData.weatherTrend || {}"
        title="共享天气趋势"
        subtitle="资源单元内成员电站共用的天气变化基线。"
      />
    </section>

    <section class="pv-split-grid production-output-view__bottom-grid">
      <app-section-card
        class="production-output-view__ranking-card"
        title="成员电站出力贡献"
        subtitle="在统一天气基线下，各站仅按容量权重和状态分摊出力。"
      >
        <div data-testid="member-station-ranking">
          <ul class="pv-list">
            <li
              v-for="item in viewData.contributionRanking || []"
              :key="item.id"
              class="pv-list__item"
            >
              <div>
                <div class="pv-list__title">
                  {{ item.name }}
                </div>
                <div class="pv-list__meta">
                  {{ item.weatherText }}
                </div>
              </div>
              <div class="pv-text-muted">
                {{ item.realtimePowerMw }} MW / {{ item.shareRate }}%
              </div>
            </li>
          </ul>
        </div>
      </app-section-card>

      <app-section-card
        class="pv-data-panel production-output-view__timeslice-card"
        title="分时数据"
        subtitle="用深色数据面板承接分时明细，和图表形成同一视觉语义。"
      >
        <div class="pv-table-shell production-output-view__timeslice-shell">
          <el-table :data="viewData.table || []" size="mini" style="width: 100%">
            <el-table-column prop="time" label="时间" min-width="110" />
            <el-table-column prop="actualPowerMw" label="实际(MW)" min-width="105" />
            <el-table-column prop="forecastPowerMw" label="预测(MW)" min-width="105" />
            <el-table-column prop="baselinePowerMw" label="基线(MW)" min-width="105" />
            <el-table-column prop="deviationRate" label="偏差率" min-width="95">
              <template slot-scope="{ row }">
                {{ row.deviationRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="irradiance" label="辐照度" min-width="100" />
            <el-table-column prop="temperature" label="温度" min-width="86">
              <template slot-scope="{ row }">
                {{ row.temperature }}°C
              </template>
            </el-table-column>
            <el-table-column prop="cloudiness" label="云量" min-width="92" />
            <el-table-column prop="maxUpCapacityKw" label="可上调(kW)" min-width="105" />
            <el-table-column prop="maxDownCapacityKw" label="可下调(kW)" min-width="105" />
          </el-table>
        </div>
      </app-section-card>
    </section>
  </div>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import PowerCurvePanel from '../shared/PowerCurvePanel.vue'
import WeatherTrendPanel from '../shared/WeatherTrendPanel.vue'

export default {
  name: 'ProductionOutputView',
  components: {
    AppMetricCard,
    AppSectionCard,
    PowerCurvePanel,
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

<style lang="less" scoped>
.production-output-view__top-grid {
  grid-template-columns: minmax(0, 1.55fr) minmax(340px, 1fr);
}

.production-output-view__bottom-grid {
  grid-template-columns: minmax(300px, 0.82fr) minmax(0, 1.38fr);
  align-items: stretch;
}

.production-output-view__ranking-card {
  height: 100%;
}

.production-output-view__timeslice-card {
  height: 100%;
  overflow: hidden;
}

.production-output-view__timeslice-shell {
  width: 100%;
  overflow-x: auto;
}

@media (max-width: 1280px) {
  .production-output-view__top-grid,
  .production-output-view__bottom-grid {
    grid-template-columns: 1fr;
  }
}
</style>
