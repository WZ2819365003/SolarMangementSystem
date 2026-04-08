<template>
  <div class="pv-page dashboard-overview" data-testid="dashboard-overview">
    <app-page-hero
      title="综合监控中心"
      description="围绕电站分布、功率偏差、经营指标和实时告警构建首页驾驶舱，保留宿主系统的深色主题、卡片层次和蓝绿主色基因。"
    >
      <template #meta>
        <div class="dashboard-overview__hero-tools">
          <el-select
            :value="selectedStationId"
            size="mini"
            placeholder="关注电站"
            @change="handleStationSelect"
          >
            <el-option
              v-for="item in stationOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-tag size="mini" effect="plain" type="success">
            Mock 数据演示
          </el-tag>
        </div>
        <div class="dashboard-overview__hero-meta">
          <div class="pv-text-muted">
            当前聚焦：{{ selectedStationName }}
          </div>
          <div class="dashboard-overview__hero-updated">
            数据时间：{{ kpiPayload.updatedAt || '--' }}
          </div>
        </div>
      </template>
    </app-page-hero>

    <section class="dashboard-overview__top-grid">
      <dashboard-map-card
        :stations="mapPayload.stations"
        :summary="mapPayload.summary"
        :alarm-items="alarmPayload.items"
        :filters="mapFilters"
        :filter-options="mapPayload.filters"
        :map-focus-token="mapFocusToken"
        :selected-station-id="selectedStationId"
        :vpp-node-payload="vppNodePayload"
        @filters-change="handleMapFiltersChange"
        @station-select="handleStationSelect"
        @view-detail="handleViewStation"
      />

      <dashboard-kpi-board class="dashboard-overview__kpi" :payload="enrichedKpiPayload" />
    </section>

    <section class="dashboard-overview__bottom-grid">
      <dashboard-power-curve
        :curve-data="enrichedCurvePayload"
        @date-change="handleCurveDateChange"
      />
      <dashboard-station-ranking
        :payload="rankingPayload"
        @metric-change="handleRankingMetricChange"
      />
      <dashboard-alarm-feed
        :payload="alarmPayload"
        :active-level="alarmLevel"
        @level-change="handleAlarmLevelChange"
        @open-alarm="handleOpenAlarm"
        @refresh="loadAlarmFeed"
      />
    </section>

    <dashboard-alarm-drawer
      :visible.sync="alarmDrawerVisible"
      :alarm="activeAlarm"
    />
  </div>
</template>

<script>
import AppPageHero from '@/components/AppPageHero.vue'
import {
  fetchAdjustableCapacity,
  fetchDashboardAlarmFeed,
  fetchDashboardKpiSummary,
  fetchDashboardPowerCurve,
  fetchDashboardStationRanking,
  fetchDashboardStationsGeo,
  fetchVppNodeStatus
} from '@/api/pvms'
import DashboardAlarmDrawer from '../components/DashboardAlarmDrawer.vue'
import DashboardAlarmFeed from '../components/DashboardAlarmFeed.vue'
import DashboardKpiBoard from '../components/DashboardKpiBoard.vue'
import DashboardMapCard from '../components/DashboardMapCard.vue'
import DashboardPowerCurve from '../components/DashboardPowerCurve.vue'
import DashboardStationRanking from '../components/DashboardStationRanking.vue'

export default {
  name: 'OverviewPage',
  components: {
    AppPageHero,
    DashboardAlarmDrawer,
    DashboardAlarmFeed,
    DashboardKpiBoard,
    DashboardMapCard,
    DashboardPowerCurve,
    DashboardStationRanking
  },
  data() {
    return {
      activeAlarm: null,
      alarmDrawerVisible: false,
      alarmLevel: '',
      alarmPayload: {
        summary: {},
        items: []
      },
      currentDate: new Date().toISOString().slice(0, 10),
      curvePayload: {
        dateTabs: []
      },
      kpiPayload: {
        items: []
      },
      mapFilters: {
        status: '',
        region: '',
        capacityRange: ''
      },
      mapPayload: {
        filters: {},
        stations: [],
        summary: []
      },
      rankingMetric: 'energy',
      rankingPayload: {
        metricOptions: [],
        rankings: []
      },
      mapFocusToken: 0,
      selectedStationId: '',
      adjustablePayload: {},
      vppNodePayload: {}
    }
  },
  computed: {
    stationOptions() {
      return this.mapPayload.stations || []
    },
    selectedStationName() {
      const current = this.stationOptions.find(
        item => item.id === this.selectedStationId
      )
      return current ? current.name : '全系统'
    },
    enrichedKpiPayload() {
      const base = this.kpiPayload || {}
      const adj = this.adjustablePayload || {}
      const items = (base.items || []).slice()
      if (adj.maxUpCapacity !== undefined) {
        items.push({
          key: 'adj-up',
          title: '可上调容量',
          value: adj.maxUpCapacity,
          unit: 'kW',
          icon: 'el-icon-top',
          accent: 'emerald',
          group: 'VPP 可调能力',
          helper: '储能可放电功率'
        })
        items.push({
          key: 'adj-down',
          title: '可下调容量',
          value: adj.maxDownCapacity,
          unit: 'kW',
          icon: 'el-icon-bottom',
          accent: 'orange',
          group: 'VPP 可调能力',
          helper: '出力 + 储能可充电功率'
        })
      }
      return Object.assign({}, base, { items })
    },
    enrichedCurvePayload() {
      const base = this.curvePayload || {}
      const adj = this.adjustablePayload || {}
      return Object.assign({}, base, {
        adjustableTimeSeries: adj.timeSeries || []
      })
    }
  },
  async created() {
    await this.bootstrap()
  },
  methods: {
    async bootstrap() {
      await Promise.all([
        this.loadMap(),
        this.loadRanking(),
        this.loadAlarmFeed()
      ])

      if (!this.selectedStationId && this.stationOptions.length) {
        this.selectedStationId = this.stationOptions[0].id
      }

      await Promise.all([
        this.loadKpis(),
        this.loadCurve(),
        this.loadAlarmFeed(),
        this.loadAdjustable(),
        this.loadVppNodeStatus()
      ])
    },
    async loadMap() {
      try {
        const response = await fetchDashboardStationsGeo(this.mapFilters)
        this.mapPayload = response.data

        if (
          !this.selectedStationId ||
          !this.stationOptions.some(item => item.id === this.selectedStationId)
        ) {
          this.selectedStationId = this.stationOptions.length
            ? this.stationOptions[0].id
            : ''
        }
      } catch (e) {
        console.error('[Dashboard] 加载地图数据失败', e)
      }
    },
    async loadKpis() {
      try {
        const response = await fetchDashboardKpiSummary({
          stationId: this.selectedStationId
        })
        this.kpiPayload = response.data
      } catch (e) {
        console.error('[Dashboard] 加载KPI数据失败', e)
      }
    },
    async loadCurve(date = this.currentDate) {
      try {
        const response = await fetchDashboardPowerCurve({
          stationId: this.selectedStationId,
          date
        })
        this.curvePayload = response.data
        this.currentDate = response.data.currentDate
      } catch (e) {
        console.error('[Dashboard] 加载出力曲线失败', e)
      }
    },
    async loadRanking(metric = this.rankingMetric) {
      try {
        const response = await fetchDashboardStationRanking({
          metric
        })
        this.rankingPayload = response.data
        this.rankingMetric = response.data.currentMetric
      } catch (e) {
        console.error('[Dashboard] 加载排名数据失败', e)
      }
    },
    async loadAlarmFeed(level = this.alarmLevel) {
      try {
        // Alarm feed is always global — do not pass stationId.
        // The summary badge counts are also global, so they must match.
        const response = await fetchDashboardAlarmFeed({ level })
        this.alarmPayload = response.data
      } catch (e) {
        console.error('[Dashboard] 加载告警数据失败', e)
      }
    },
    async loadAdjustable() {
      try {
        const response = await fetchAdjustableCapacity()
        this.adjustablePayload = response.data
      } catch (e) {
        console.error('[Dashboard] 加载可调能力数据失败', e)
      }
    },
    async loadVppNodeStatus() {
      try {
        const response = await fetchVppNodeStatus()
        this.vppNodePayload = response.data
      } catch (e) {
        console.error('[Dashboard] 加载VPP节点状态失败', e)
      }
    },
    async handleMapFiltersChange(nextFilters) {
      this.mapFilters = Object.assign({}, nextFilters)
      await this.loadMap()
      await Promise.all([
        this.loadKpis(),
        this.loadCurve(),
        this.loadAlarmFeed()
      ])
    },
    async handleStationSelect(stationId) {
      if (!stationId) {
        return
      }

      const stationChanged = stationId !== this.selectedStationId
      this.selectedStationId = stationId
      this.mapFocusToken += 1

      if (!stationChanged) {
        return
      }

      await Promise.all([
        this.loadKpis(),
        this.loadCurve(),
        this.loadAlarmFeed()
      ])
    },
    async handleCurveDateChange(date) {
      await this.loadCurve(date)
    },
    async handleRankingMetricChange(metric) {
      await this.loadRanking(metric)
    },
    async handleAlarmLevelChange(level) {
      this.alarmLevel = level
      await this.loadAlarmFeed(level)
    },
    handleOpenAlarm(alarm) {
      this.activeAlarm = alarm
      this.alarmDrawerVisible = true
    },
    handleViewStation(station) {
      if (station && station.resourceUnitId) {
        this.$router.push({
          path: '/production-monitor/overview',
          query: {
            resourceUnitId: station.resourceUnitId
          }
        })
        return
      }

      this.$router.push({
        path: '/stations/archive',
        query: {
          focus: station.id
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-overview__hero-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dashboard-overview__hero-tools /deep/ .el-input__inner {
  min-width: 220px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: var(--pvms-text-primary);
}

.dashboard-overview__hero-meta {
  margin-top: 12px;
}

.dashboard-overview__hero-updated {
  margin-top: 8px;
  color: var(--pvms-text-primary);
  font-size: 14px;
}

.dashboard-overview__top-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(340px, 0.95fr);
  gap: 16px;
  align-items: stretch;
}

.dashboard-overview__kpi {
  height: 100%;
}

.dashboard-overview__bottom-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(300px, 0.85fr) minmax(300px, 0.9fr);
  gap: 16px;
}

@media (max-width: 1440px) {
  .dashboard-overview__top-grid,
  .dashboard-overview__bottom-grid {
    grid-template-columns: 1fr;
  }
}
</style>
