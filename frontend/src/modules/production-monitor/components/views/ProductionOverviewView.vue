<template>
  <div v-loading="loading" class="production-view" data-testid="production-overview-view">
    <resource-unit-status-bar :resource-unit="resourceUnit" />

    <section
      class="pv-card-grid production-overview-view__kpis"
      data-testid="production-overview-kpis"
    >
      <app-metric-card
        v-for="item in kpiCards"
        :key="item.key"
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :helper="item.helper"
        :icon="item.icon"
        :accent="item.accent"
      />
    </section>

    <section
      class="production-overview-view__matrix"
      data-testid="production-overview-summary-grid"
    >
      <member-station-summary-table
        class="production-overview-view__member-card"
        :stations="viewData.memberStations || []"
      />

      <weather-brief-card
        class="production-overview-view__weather-card"
        :weather="viewData.weatherBrief || {}"
      />

      <alarm-brief-card
        class="production-overview-view__alarm-card"
        :alarm="viewData.alarmBrief || {}"
      />

      <app-section-card
        class="pv-data-panel production-overview-view__running-card"
        title="运行摘要"
        subtitle="当前资源单元小时级运行摘要，采用统一深色数据面板承载"
        data-testid="production-overview-running-summary"
      >
        <div class="pv-table-shell">
          <el-table :data="viewData.summaryTable || []" size="mini" style="width: 100%">
            <el-table-column prop="time" label="时间" width="96" />
            <el-table-column prop="realtimePowerMw" label="实时出力(MW)" width="120" />
            <el-table-column prop="dispatchableCapacityMw" label="可调容量(MW)" width="128" />
            <el-table-column prop="onlineRate" label="在线率" width="92">
              <template slot-scope="{ row }">
                {{ row.onlineRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="forecastDeviationRate" label="预测偏差率" width="108">
              <template slot-scope="{ row }">
                {{ row.forecastDeviationRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="weatherText" label="天气结论" min-width="200" />
          </el-table>
        </div>
      </app-section-card>
    </section>
  </div>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import AlarmBriefCard from '../shared/AlarmBriefCard.vue'
import MemberStationSummaryTable from '../shared/MemberStationSummaryTable.vue'
import ResourceUnitStatusBar from '../shared/ResourceUnitStatusBar.vue'
import WeatherBriefCard from '../shared/WeatherBriefCard.vue'

export default {
  name: 'ProductionOverviewView',
  components: {
    AlarmBriefCard,
    AppMetricCard,
    AppSectionCard,
    MemberStationSummaryTable,
    ResourceUnitStatusBar,
    WeatherBriefCard
  },
  props: {
    resourceUnit: {
      type: Object,
      default: () => ({})
    },
    viewData: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    kpiCards() {
      return this.viewData.kpis || []
    }
  }
}
</script>

<style lang="less" scoped>
.production-overview-view__kpis {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.production-overview-view__kpis /deep/ .app-metric-card {
  padding: 16px 16px 14px;
  border-radius: 4px;
}

.production-overview-view__kpis /deep/ .app-metric-card__header {
  gap: 10px;
}

.production-overview-view__kpis /deep/ .app-metric-card__icon {
  width: 34px;
  height: 34px;
  border-radius: 4px;
  font-size: 16px;
}

.production-overview-view__kpis /deep/ .app-metric-card__title {
  font-size: 13px;
}

.production-overview-view__kpis /deep/ .app-metric-card__value {
  margin-top: 14px;
  font-size: 24px;
}

.production-overview-view__kpis /deep/ .app-metric-card__unit {
  font-size: 13px;
}

.production-overview-view__kpis /deep/ .app-metric-card__helper {
  margin-top: 12px;
  font-size: 12px;
  line-height: 18px;
}

.production-overview-view__matrix {
  display: grid;
  grid-template-columns: minmax(0, 1.46fr) minmax(320px, 0.88fr);
  grid-template-areas:
    'member weather'
    'running alarm';
  gap: 16px;
  align-items: stretch;
}

.production-overview-view__member-card {
  grid-area: member;
}

.production-overview-view__weather-card {
  grid-area: weather;
}

.production-overview-view__alarm-card {
  grid-area: alarm;
}

.production-overview-view__running-card {
  grid-area: running;
}

.production-overview-view__member-card /deep/ .app-section-card,
.production-overview-view__running-card /deep/ .app-section-card,
.production-overview-view__weather-card /deep/ .app-section-card,
.production-overview-view__alarm-card /deep/ .app-section-card {
  height: 100%;
}

.production-overview-view__member-card /deep/ .app-section-card__header,
.production-overview-view__running-card /deep/ .app-section-card__header,
.production-overview-view__weather-card /deep/ .app-section-card__header,
.production-overview-view__alarm-card /deep/ .app-section-card__header {
  margin-bottom: 14px;
}

.production-overview-view__member-card /deep/ .app-section-card,
.production-overview-view__running-card /deep/ .app-section-card {
  padding: 18px 20px 16px;
}

.production-overview-view__member-card /deep/ .el-table th,
.production-overview-view__member-card /deep/ .el-table td,
.production-overview-view__running-card /deep/ .el-table th,
.production-overview-view__running-card /deep/ .el-table td {
  padding-top: 7px;
  padding-bottom: 7px;
}

@media (max-width: 1360px) {
  .production-overview-view__kpis {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .production-overview-view__matrix {
    grid-template-columns: 1fr;
    grid-template-areas:
      'member'
      'weather'
      'running'
      'alarm';
  }
}

@media (max-width: 900px) {
  .production-overview-view__kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
