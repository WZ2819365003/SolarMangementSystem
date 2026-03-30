<template>
  <div class="pv-page strategy-page" data-testid="strategy-page">
    <strategy-hero :view-key="currentViewKey" />

    <strategy-tab-nav
      :tabs="tabs"
      :active-key="currentViewKey"
      @change="handleTabChange"
    />

    <strategy-filter-bar
      :view-key="currentViewKey"
      :query="query"
      :region-options="meta.regionOptions"
      :station-options="meta.stationOptions"
      :type-options="meta.typeOptions"
      :status-options="meta.statusOptions"
      @search="handleSearch"
      @refresh="loadCurrentView"
    />

    <component
      :is="currentViewComponent"
      :key="currentViewKey"
      :view-data="viewData"
      :meta="meta"
      :query="query"
      :loading="loadingView"
      @refresh="loadCurrentView"
    />
  </div>
</template>

<script>
import StrategyHero from '../components/StrategyHero.vue'
import StrategyTabNav from '../components/StrategyTabNav.vue'
import StrategyFilterBar from '../components/StrategyFilterBar.vue'
import {
  fetchStrategyMeta,
  fetchStrategyTree,
  fetchStrategyKpi,
  fetchStrategyList,
  fetchStrategyElectricityPrice,
  fetchStrategyRevenueSummary,
  fetchStrategyRevenueDetail,
  fetchStrategyCompare
} from '@/api/pvms'

var viewComponentMap = {
  list: 'StrategyListView',
  config: 'StrategyConfigView',
  revenue: 'StrategyRevenueView'
}

export default {
  name: 'StrategyPage',
  components: {
    StrategyHero,
    StrategyTabNav,
    StrategyFilterBar,
    StrategyListView: () => import('../components/views/StrategyListView.vue'),
    StrategyConfigView: () => import('../components/views/StrategyConfigView.vue'),
    StrategyRevenueView: () => import('../components/views/StrategyRevenueView.vue')
  },
  data() {
    return {
      tabs: [
        { key: 'list', label: '策略列表', path: '/strategy/list' },
        { key: 'config', label: '策略配置', path: '/strategy/config' },
        { key: 'revenue', label: '收益分析', path: '/strategy/revenue' }
      ],
      meta: {
        defaultStationId: '',
        companies: [],
        stations: [],
        regionOptions: [],
        stationOptions: [],
        typeOptions: [],
        statusOptions: [],
        priceTemplate: []
      },
      query: {
        region: '',
        stationId: '',
        type: '',
        status: '',
        keyword: '',
        dateRange: []
      },
      viewData: {},
      loadingView: false
    }
  },
  computed: {
    currentViewKey() {
      return this.$route.meta.viewKey || 'list'
    },
    currentViewComponent() {
      return viewComponentMap[this.currentViewKey] || viewComponentMap.list
    }
  },
  watch: {
    currentViewKey() {
      this.loadCurrentView()
    }
  },
  async created() {
    await this.loadMeta()
    await this.loadCurrentView()
  },
  methods: {
    async loadMeta() {
      try {
        var res = await fetchStrategyMeta()
        var data = res.data || {}
        this.meta = {
          defaultStationId: data.defaultStationId || '',
          companies: data.companies || [],
          stations: data.stations || [],
          regionOptions: (data.companies || []).map(function (item) {
            return { label: item.region, value: item.region }
          }),
          stationOptions: (data.stations || []).map(function (item) {
            return { label: item.name, value: item.id }
          }),
          typeOptions: (data.types || []).map(function (item) {
            return { label: item.label, value: item.value }
          }),
          statusOptions: (data.statuses || []).map(function (item) {
            return { label: item.label, value: item.value }
          }),
          priceTemplate: data.pricePeriods || []
        }

        if (!this.query.stationId && data.defaultStationId) {
          this.query.stationId = data.defaultStationId
        }
      } catch (error) {
        console.error('[StrategyPage] failed to load meta', error)
      }
    },
    buildParams() {
      var params = {
        region: this.query.region,
        stationId: this.query.stationId,
        type: this.query.type,
        status: this.query.status,
        keyword: this.query.keyword
      }

      if (this.query.dateRange && this.query.dateRange.length === 2) {
        params.startDate = this.query.dateRange[0]
        params.endDate = this.query.dateRange[1]
      }

      return params
    },
    async loadCurrentView() {
      this.loadingView = true
      try {
        var params = this.buildParams()
        if (this.currentViewKey === 'list') {
          var listResults = await Promise.all([
            fetchStrategyKpi(params),
            fetchStrategyList(params)
          ])
          this.viewData = {
            kpi: listResults[0].data || {},
            list: listResults[1].data || {}
          }
        } else if (this.currentViewKey === 'config') {
          var configResults = await Promise.all([
            fetchStrategyTree(params),
            fetchStrategyElectricityPrice({ stationId: this.query.stationId || this.meta.defaultStationId }),
            fetchStrategyList({
              region: this.query.region,
              stationId: this.query.stationId,
              type: this.query.type
            })
          ])
          this.viewData = {
            tree: (configResults[0].data && configResults[0].data.tree) || [],
            electricityPrice: configResults[1].data || {},
            list: configResults[2].data || {}
          }
        } else if (this.currentViewKey === 'revenue') {
          var revenueResults = await Promise.all([
            fetchStrategyRevenueSummary(params),
            fetchStrategyRevenueDetail(params),
            fetchStrategyCompare({})
          ])
          this.viewData = {
            summary: revenueResults[0].data || {},
            detail: revenueResults[1].data || {},
            compare: revenueResults[2].data || {}
          }
        }
      } catch (error) {
        console.error('[StrategyPage] failed to load view', error)
      } finally {
        this.loadingView = false
      }
    },
    handleTabChange(key) {
      var tab = this.tabs.find(function (item) { return item.key === key })
      if (tab && this.$route.path !== tab.path) {
        this.$router.push(tab.path)
      }
    },
    handleSearch(nextQuery) {
      this.query = { ...this.query, ...nextQuery }
      this.loadCurrentView()
    }
  }
}
</script>

<style lang="less" scoped>
.strategy-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
