<template>
  <div class="pv-page forecast-page" data-testid="forecast-page">
    <forecast-hero :view-key="currentViewKey" />

    <forecast-tab-nav
      :tabs="tabs"
      :active-key="currentViewKey"
      @change="handleTabChange"
    />

    <forecast-filter-bar
      :query="query"
      :region-options="meta.regionOptions"
      :station-options="meta.stationOptions"
      @search="handleSearch"
      @refresh="loadCurrentView"
    />

    <component
      :is="currentViewComponent"
      :key="currentViewKey"
      :view-data="viewData"
      :query="query"
      :loading="loadingView"
    />
  </div>
</template>

<script>
import ForecastHero from '../components/ForecastHero.vue'
import ForecastTabNav from '../components/ForecastTabNav.vue'
import ForecastFilterBar from '../components/ForecastFilterBar.vue'
import {
  fetchForecastMeta,
  fetchForecastOverview,
  fetchForecastAdjustable,
  fetchForecastAccuracy,
  fetchForecastComparison,
  fetchForecastDeviationHeatmap
} from '@/api/pvms'

var viewComponentMap = {
  overview: 'ForecastOverviewView',
  adjustable: 'ForecastAdjustableView',
  accuracy: 'ForecastAccuracyView'
}

export default {
  name: 'ForecastPage',
  components: {
    ForecastHero,
    ForecastTabNav,
    ForecastFilterBar,
    ForecastOverviewView: () => import('../components/views/ForecastOverviewView.vue'),
    ForecastAdjustableView: () => import('../components/views/ForecastAdjustableView.vue'),
    ForecastAccuracyView: () => import('../components/views/ForecastAccuracyView.vue')
  },
  data() {
    return {
      tabs: [
        { key: 'overview', label: '预测总览', path: '/forecast/overview' },
        { key: 'adjustable', label: '可调能力分析', path: '/forecast/adjustable' },
        { key: 'accuracy', label: '精度评估', path: '/forecast/accuracy' }
      ],
      meta: {
        regionOptions: [],
        stationOptions: []
      },
      query: {
        region: '',
        stationId: '',
        dateRange: [],
        forecastType: 'day-ahead'
      },
      viewData: {},
      loadingView: false
    }
  },
  computed: {
    currentViewKey() {
      return this.$route.meta.viewKey || 'overview'
    },
    currentViewComponent() {
      return viewComponentMap[this.currentViewKey] || viewComponentMap.overview
    }
  },
  watch: {
    currentViewKey() {
      this.loadCurrentView()
    }
  },
  created() {
    this.loadMeta()
    this.loadCurrentView()
  },
  methods: {
    async loadMeta() {
      try {
        var res = await fetchForecastMeta()
        if (res.data) {
          this.meta.regionOptions = (res.data.regions || []).map(function (r) {
            return { label: r, value: r }
          })
          this.meta.stationOptions = (res.data.stations || []).map(function (s) {
            return { label: s.name, value: s.id }
          })
        }
      } catch (e) {
        console.error('[ForecastPage] 加载元数据失败', e)
      }
    },
    async loadCurrentView() {
      this.loadingView = true
      try {
        var key = this.currentViewKey
        var params = {
          region: this.query.region,
          stationId: this.query.stationId,
          forecastType: this.query.forecastType
        }
        if (this.query.dateRange && this.query.dateRange.length === 2) {
          params.startDate = this.query.dateRange[0]
          params.endDate = this.query.dateRange[1]
        }

        if (key === 'overview') {
          var results = await Promise.all([
            fetchForecastOverview(params),
            fetchForecastComparison(params),
            fetchForecastDeviationHeatmap(params)
          ])
          this.viewData = {
            ...(results[0].data || {}),
            comparison: results[1].data || {},
            heatmap: results[2].data || {}
          }
        } else if (key === 'adjustable') {
          var res = await fetchForecastAdjustable(params)
          this.viewData = res.data || {}
        } else if (key === 'accuracy') {
          var res2 = await fetchForecastAccuracy(params)
          this.viewData = res2.data || {}
        }
      } catch (e) {
        console.error('[ForecastPage] 加载视图数据失败', e)
      } finally {
        this.loadingView = false
      }
    },
    handleTabChange(key) {
      var tab = this.tabs.find(function (t) { return t.key === key })
      if (tab && this.$route.path !== tab.path) {
        this.$router.push(tab.path)
      }
    },
    handleSearch(newQuery) {
      this.query = { ...this.query, ...newQuery }
      this.loadCurrentView()
    }
  }
}
</script>

<style lang="less" scoped>
.forecast-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
