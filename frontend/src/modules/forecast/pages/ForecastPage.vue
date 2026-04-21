<template>
  <div class="pv-page forecast-page" data-testid="forecast-page">
    <forecast-hero :view-key="currentViewKey" />

    <forecast-tab-nav
      :tabs="tabs"
      :active-key="currentViewKey"
      @change="handleTabChange"
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
import {
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
    },
    moduleFilter() {
      return this.$store.getters['stationContext/getModuleFilter']('forecast')
    },
    query() {
      return Object.assign({
        resourceUnitId: '',
        stationId: '',
        dateRange: []
      }, this.moduleFilter || {})
    }
  },
  watch: {
    currentViewKey() {
      this.loadCurrentView()
    },
    moduleFilter: {
      deep: true,
      handler() {
        this.loadCurrentView()
      }
    }
  },
  created() {
    this.loadCurrentView()
  },
  methods: {
    async loadCurrentView() {
      this.loadingView = true
      try {
        var key = this.currentViewKey
        var params = {
          resourceUnitId: this.query.resourceUnitId,
          stationId: this.query.stationId,
          forecastType: 'day-ahead'
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
