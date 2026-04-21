<template>
  <div class="pv-page station-overview-page" data-testid="station-overview-page">
    <station-overview-hero
      :info="overview.info"
      :weather="overview.weather"
      @back="$router.push('/station')"
    />

    <station-overview-kpi-board :kpi="overview.kpi" />

    <section class="station-overview-page__middle">
      <station-overview-power-curve
        :curve-data="curveData"
        @date-change="handleDateChange"
      />

      <div class="station-overview-page__side">
        <resource-unit-weather-card :weather="overview.weather" />
        <resource-unit-dispatch-card :dispatch-summary="overview.dispatchSummary" />
        <resource-unit-alarm-card :alarm-summary="overview.alarmSummary" />
      </div>
    </section>

    <resource-unit-member-stations-card :stations="overview.memberStations" />
  </div>
</template>

<script>
import {
  fetchResourceUnitOverview,
  fetchResourceUnitPowerCurve
} from '@/api/pvms'
import ResourceUnitAlarmCard from '../components/ResourceUnitAlarmCard.vue'
import ResourceUnitDispatchCard from '../components/ResourceUnitDispatchCard.vue'
import ResourceUnitMemberStationsCard from '../components/ResourceUnitMemberStationsCard.vue'
import ResourceUnitWeatherCard from '../components/ResourceUnitWeatherCard.vue'
import StationOverviewHero from '../components/StationOverviewHero.vue'
import StationOverviewKpiBoard from '../components/StationOverviewKpiBoard.vue'
import StationOverviewPowerCurve from '../components/StationOverviewPowerCurve.vue'

function createEmptyOverview() {
  return {
    info: {},
    kpi: {},
    weather: {},
    dispatchSummary: {},
    alarmSummary: {},
    memberStations: []
  }
}

function createEmptyCurve() {
  return {
    resourceUnitName: '',
    currentDate: '2026-03-23',
    actual: [],
    forecast: [],
    baseline: [],
    irradiance: []
  }
}

export default {
  name: 'StationOverviewPage',
  components: {
    ResourceUnitAlarmCard,
    ResourceUnitDispatchCard,
    ResourceUnitMemberStationsCard,
    ResourceUnitWeatherCard,
    StationOverviewHero,
    StationOverviewKpiBoard,
    StationOverviewPowerCurve
  },
  data() {
    return {
      overview: createEmptyOverview(),
      curveData: createEmptyCurve()
    }
  },
  computed: {
    stationId() {
      return this.$route.params.id
    }
  },
  watch: {
    stationId: {
      immediate: true,
      handler() {
        this.loadData()
      }
    }
  },
  methods: {
    async loadData() {
      const [overviewResponse, curveResponse] = await Promise.all([
        fetchResourceUnitOverview(this.stationId),
        fetchResourceUnitPowerCurve(this.stationId, {
          date: this.curveData.currentDate || '2026-03-23'
        })
      ])

      if (!overviewResponse.data) {
        this.$router.replace('/station')
        return
      }

      this.overview = overviewResponse.data
      this.curveData = curveResponse.data || createEmptyCurve()
    },
    async handleDateChange(date) {
      const response = await fetchResourceUnitPowerCurve(this.stationId, {
        date
      })
      this.curveData = response.data || createEmptyCurve()
    }
  }
}
</script>

<style lang="less" scoped>
.station-overview-page__middle {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(360px, 0.95fr);
  gap: 18px;
  align-items: stretch;
}

.station-overview-page__side {
  display: grid;
  grid-template-rows: repeat(3, minmax(0, auto));
  gap: 18px;
}

@media (max-width: 1380px) {
  .station-overview-page__middle {
    grid-template-columns: 1fr;
  }
}
</style>
