<template>
  <div class="pv-page production-monitor-page" data-testid="production-monitor-page">
    <production-monitor-hero
      v-if="secondaryMode === 'overview'"
      :resource-unit="currentResourceUnit"
      :view-key="currentViewKey"
    />

    <!-- Secondary navigation: 资源总览 | 电站监控 -->
    <div class="production-monitor-page__secondary-nav">
      <div
        class="production-monitor-page__secondary-tab"
        :class="{ 'is-active': secondaryMode === 'overview' }"
        @click="switchSecondary('overview')"
      >
        <i class="el-icon-s-data" />
        资源总览
      </div>
      <div
        class="production-monitor-page__secondary-tab"
        :class="{ 'is-active': secondaryMode === 'station' }"
        @click="switchSecondary('station')"
      >
        <i class="el-icon-monitor" />
        电站监控
      </div>
    </div>

    <!-- Overview mode -->
    <template v-if="secondaryMode === 'overview'">
      <production-monitor-tab-nav
        :tabs="tabs"
        :active-key="currentViewKey"
        @change="handleTabChange"
      />
      <production-monitor-filter-bar
        :query="query"
        :region-options="meta.regionOptions"
        :city-options="cityOptions"
        :resource-unit-options="resourceUnitOptions"
        @search="handleSearch"
        @export="handleExport"
        @refresh="loadCurrentView"
      />
      <component
        :is="currentViewComponent"
        :key="currentViewKey"
        :resource-unit="currentResourceUnit"
        :query="query"
        :view-data="viewData"
        :loading="loadingView"
      />
    </template>

    <!-- Station monitoring mode -->
    <div v-else class="production-monitor-page__station-layout">
      <station-tree-panel
        ref="stationTree"
        :tree-data="treeData"
        :selected-node-id="selectedNodeId"
        :loading="treeLoading"
        @node-click="handleNodeClick"
        @status-filter="handleStatusFilter"
      />
      <station-data-panel
        :selected-node="selectedNode"
        :company-data="companyData"
        :resource-data="null"
        :station-realtime-data="stationRealtimeData"
        :inverter-data="inverterData"
        :active-tab="activeStationTab"
        :current-date="stationDate"
        :granularity="stationGranularity"
        :refresh-interval="stationRefreshInterval"
        @tab-change="handleStationTabChange"
        @date-change="handleStationDateChange"
        @granularity-change="handleStationGranularityChange"
        @refresh-interval-change="handleStationRefreshIntervalChange"
        @refresh="handleStationRefresh"
        @resource-click="handleStationJumpFromCompany"
        @resource-station-click="handleStationJumpFromCompany"
      />
    </div>
  </div>
</template>

<script>
import {
  fetchProductionMonitorDispatch,
  fetchProductionMonitorLoad,
  fetchProductionMonitorMeta,
  fetchProductionMonitorOutput,
  fetchProductionMonitorOverview,
  fetchStationTree,
  fetchStationArchiveCompanyOverview,
  fetchStationArchiveInverterRealtime,
  fetchStationArchiveRealtime,
  fetchStationArchiveStrategy
} from '@/api/pvms'
import ProductionMonitorFilterBar from '../components/ProductionMonitorFilterBar.vue'
import ProductionMonitorHero from '../components/ProductionMonitorHero.vue'
import ProductionMonitorTabNav from '../components/ProductionMonitorTabNav.vue'
import ProductionDispatchView from '../components/views/ProductionDispatchView.vue'
import ProductionOutputView from '../components/views/ProductionOutputView.vue'
import ProductionLoadView from '../components/views/ProductionLoadView.vue'
import ProductionOverviewView from '../components/views/ProductionOverviewView.vue'
import StationTreePanel from '../../stations/components/StationTreePanel.vue'
import StationDataPanel from '../../stations/components/StationDataPanel.vue'

function createEmptyMeta() {
  return {
    defaultResourceUnitId: '',
    regionOptions: [],
    resourceUnits: []
  }
}

export default {
  name: 'ProductionMonitorPage',
  components: {
    ProductionDispatchView,
    ProductionLoadView,
    ProductionMonitorFilterBar,
    ProductionMonitorHero,
    ProductionMonitorTabNav,
    ProductionOutputView,
    ProductionOverviewView,
    StationTreePanel,
    StationDataPanel
  },
  data() {
    return {
      tabs: [
        { key: 'overview', label: '出力概况', path: '/production-monitor/overview' },
        { key: 'load', label: '负荷与出力', path: '/production-monitor/load' },
        { key: 'dispatch', label: '调度执行', path: '/production-monitor/dispatch' }
      ],
      meta: createEmptyMeta(),
      query: {
        region: '',
        city: '',
        resourceUnitId: '',
        date: new Date().toISOString().slice(0, 10),
        granularity: '15m'
      },
      loadingView: false,
      viewData: {},
      // Station monitoring data
      treeData: [],
      treeLoading: false,
      selectedNodeId: '',
      selectedNode: null,
      companyData: null,
      stationRealtimeData: null,
      inverterData: null,
      activeStationTab: 'adjustable',
      stationDate: new Date().toISOString().slice(0, 10),
      stationGranularity: '15min',
      stationRefreshInterval: 30,
      refreshTimer: null,
      statusFilter: ''
    }
  },
  computed: {
    currentViewKey() {
      return this.$route.meta.viewKey || 'overview'
    },
    secondaryMode() {
      return this.currentViewKey === 'station' ? 'station' : 'overview'
    },
    currentResourceUnit() {
      return (
        this.meta.resourceUnits.find(item => item.id === this.query.resourceUnitId) || {}
      )
    },
    cityOptions() {
      const cities = this.meta.resourceUnits
        .filter(item => !this.query.region || item.region === this.query.region)
        .map(item => item.city)

      return [{ label: '全部城市', value: '' }].concat(
        Array.from(new Set(cities)).map(item => ({
          label: item,
          value: item
        }))
      )
    },
    resourceUnitOptions() {
      return this.meta.resourceUnits
        .filter(item => !this.query.region || item.region === this.query.region)
        .filter(item => !this.query.city || item.city === this.query.city)
        .map(item => ({
          label: item.name,
          value: item.id
        }))
    },
    currentViewComponent() {
      return {
        overview: 'ProductionOverviewView',
        output: 'ProductionOutputView',
        load: 'ProductionLoadView',
        dispatch: 'ProductionDispatchView'
      }[this.currentViewKey]
    }
  },
  watch: {
    '$route.meta.viewKey'() {
      if (this.secondaryMode === 'overview') {
        this.loadCurrentView()
      }
    },
    '$route.query.resourceUnitId': {
      async handler(resourceUnitId) {
        if (!resourceUnitId || !this.meta.resourceUnits.length) {
          return
        }

        const matched = this.meta.resourceUnits.find(item => item.id === resourceUnitId)
        if (!matched || matched.id === this.query.resourceUnitId) {
          return
        }

        this.query = {
          ...this.query,
          resourceUnitId: matched.id
        }
        await this.loadCurrentView()
      }
    }
  },
  created() {
    this.initializeModule()
  },
  beforeDestroy() {
    this.clearRefreshTimer()
  },
  methods: {
    async initializeModule() {
      try {
        const response = await fetchProductionMonitorMeta()
        this.meta = response.data || createEmptyMeta()
      } catch (e) {
        console.error('[ProductionMonitor] 加载元数据失败', e)
        this.meta = createEmptyMeta()
      }
      this.bootstrapQuery()
      if (this.secondaryMode === 'overview') {
        await this.loadCurrentView()
      } else {
        await this.loadTree()
        this.restoreFromQuery()
      }
    },
    bootstrapQuery() {
      const routeResourceUnitId = this.$route.query.resourceUnitId
      const defaultResourceUnit =
        this.meta.resourceUnits.find(item => item.id === routeResourceUnitId) ||
        this.meta.resourceUnits.find(
          item => item.id === this.meta.defaultResourceUnitId
        ) ||
        this.meta.resourceUnits[0]

      if (!defaultResourceUnit) {
        return
      }

      this.query = {
        ...this.query,
        region: '',
        city: '',
        resourceUnitId: defaultResourceUnit.id
      }
      this.syncRouteQuery()
    },
    async loadCurrentView() {
      if (!this.query.resourceUnitId && this.currentViewKey !== 'load') {
        return
      }

      this.loadingView = true

      try {
        const requestMap = {
          overview: fetchProductionMonitorOverview,
          output: fetchProductionMonitorOutput,
          load: fetchProductionMonitorLoad,
          dispatch: fetchProductionMonitorDispatch
        }

        const fetcher = requestMap[this.currentViewKey]
        if (!fetcher) {
          this.viewData = {}
          return
        }

        const response = await fetcher({
          ...this.query
        })
        this.viewData = response.data || {}
      } catch (e) {
        console.error('[ProductionMonitor] 加载视图数据失败', e)
        this.viewData = {}
      } finally {
        this.loadingView = false
      }
    },
    handleTabChange(viewKey) {
      const target = this.tabs.find(item => item.key === viewKey)
      if (target && target.path !== this.$route.path) {
        this.$router.push({
          path: target.path,
          query: {
            resourceUnitId: this.query.resourceUnitId
          }
        })
      }
    },
    async handleSearch(nextQuery) {
      const normalizedQuery = {
        ...this.query,
        ...nextQuery
      }

      const matchesScope = item => {
        if (normalizedQuery.region && item.region !== normalizedQuery.region) {
          return false
        }
        if (normalizedQuery.city && item.city !== normalizedQuery.city) {
          return false
        }
        return true
      }

      if (normalizedQuery.resourceUnitId) {
        const resourceUnit = this.meta.resourceUnits.find(
          item => item.id === normalizedQuery.resourceUnitId
        )

        if (!resourceUnit || !matchesScope(resourceUnit)) {
          const firstMatch = this.meta.resourceUnits.find(matchesScope)
          normalizedQuery.resourceUnitId = firstMatch ? firstMatch.id : ''
        }
      } else {
        const firstMatch = this.meta.resourceUnits.find(matchesScope)
        normalizedQuery.resourceUnitId = firstMatch ? firstMatch.id : ''
      }

      this.query = normalizedQuery
      this.syncRouteQuery()
      await this.loadCurrentView()
    },
    syncRouteQuery() {
      const nextQuery = this.query.resourceUnitId
        ? { resourceUnitId: this.query.resourceUnitId }
        : {}

      if (
        this.$route.query.resourceUnitId === nextQuery.resourceUnitId &&
        Object.keys(this.$route.query).length === Object.keys(nextQuery).length
      ) {
        return
      }

      this.$router.replace({
        path: this.$route.path,
        query: nextQuery
      })
    },
    handleExport() {
      this.$message.success('当前为 mock 设计版，导出能力将在接真实接口后打开')
    },

    // ---- Secondary navigation ----
    switchSecondary(mode) {
      if (mode === 'station') {
        this.$router.push('/production-monitor/station')
        if (!this.treeData.length) this.loadTree()
      } else {
        this.$router.push('/production-monitor/overview')
      }
    },

    // ---- Station monitoring methods ----
    async loadTree() {
      this.treeLoading = true
      try {
        const response = await fetchStationTree({ status: this.statusFilter })
        this.treeData = response.data.tree || []
        // Auto-select first company on initial load
        if (this.treeData.length && !this.selectedNodeId) {
          this.$nextTick(() => {
            const firstCompany = this.treeData[0]
            this.selectNode(firstCompany)
            // Expand first company in tree
            if (this.$refs && this.$refs.stationTree) {
              this.$refs.stationTree.$refs.tree &&
                this.$refs.stationTree.$refs.tree.store &&
                this.$refs.stationTree.$refs.tree.store.nodesMap[firstCompany.id] &&
                (this.$refs.stationTree.$refs.tree.store.nodesMap[firstCompany.id].expanded = true)
            }
          })
        }
      } catch (e) {
        console.error('[ProductionMonitor] 加载树失败', e)
      }
      this.treeLoading = false
    },
    restoreFromQuery() {
      const query = this.$route.query || {}
      if (query.nodeId) {
        const node = this.findNodeById(this.treeData, query.nodeId)
        if (node) {
          this.selectNode(node)
        }
      }
    },
    findNodeById(nodes, id) {
      for (let i = 0; i < nodes.length; i++) {
        if (nodes[i].id === id) return nodes[i]
        if (nodes[i].children) {
          const found = this.findNodeById(nodes[i].children, id)
          if (found) return found
        }
      }
      return null
    },
    buildParentPath(nodeId) {
      const path = []
      const walk = (nodes, chain) => {
        for (let i = 0; i < nodes.length; i++) {
          const n = nodes[i]
          const current = chain.concat([{ label: n.label }])
          if (n.id === nodeId) {
            path.push(...chain)
            return true
          }
          if (n.children && walk(n.children, current)) return true
        }
        return false
      }
      walk(this.treeData, [])
      return path
    },
    selectNode(node) {
      this.selectedNodeId = node.id
      this.selectedNode = Object.assign({}, node, {
        parentPath: this.buildParentPath(node.id)
      })
      this.updateStationQuery(node)
      this.loadNodeData(node)
    },
    updateStationQuery(node) {
      const query = { nodeType: node.nodeType, nodeId: node.id }
      this.$router.replace({ query }).catch(() => {})
    },
    async loadNodeData(node) {
      this.companyData = null
      this.stationRealtimeData = null
      this.inverterData = null

      if (node.nodeType === 'company') {
        await this.loadCompanyOverview(node.id)
      } else if (node.nodeType === 'station') {
        await this.loadStationData(node.id)
        this.startRefreshTimer()
      } else if (node.nodeType === 'inverter') {
        await this.loadInverterData(node.id)
      }
    },
    async loadCompanyOverview(companyId) {
      try {
        const response = await fetchStationArchiveCompanyOverview({ companyId })
        this.companyData = response.data
      } catch (e) {
        console.error('[ProductionMonitor] 加载公司数据失败', e)
      }
    },
    async loadStationData(stationId) {
      try {
        const metric = this.activeStationTab
        // Preserve KPIs and monthly stats across tab switches
        const preserved = {}
        if (this.stationRealtimeData) {
          if (this.stationRealtimeData.stationKpis) preserved.stationKpis = this.stationRealtimeData.stationKpis
          if (this.stationRealtimeData.monthlyStats) preserved.monthlyStats = this.stationRealtimeData.monthlyStats
        }
        if (metric === 'strategy') {
          const response = await fetchStationArchiveStrategy({ stationId })
          this.stationRealtimeData = Object.assign({}, preserved, response.data)
        } else {
          const response = await fetchStationArchiveRealtime({
            stationId,
            metric,
            date: this.stationDate,
            granularity: this.stationGranularity
          })
          this.stationRealtimeData = Object.assign({}, preserved, response.data)
        }
      } catch (e) {
        console.error('[ProductionMonitor] 加载电站数据失败', e)
      }
    },
    async loadInverterData(inverterId) {
      try {
        const response = await fetchStationArchiveInverterRealtime({ inverterId })
        this.inverterData = response.data
      } catch (e) {
        console.error('[ProductionMonitor] 加载逆变器数据失败', e)
      }
    },
    handleNodeClick(node) {
      this.clearRefreshTimer()
      this.activeStationTab = 'adjustable'
      this.selectNode(node)
    },
    async handleStatusFilter(status) {
      this.statusFilter = status
      await this.loadTree()
    },
    async handleStationTabChange(tabKey) {
      this.activeStationTab = tabKey
      if (this.selectedNode && this.selectedNode.nodeType === 'station') {
        await this.loadStationData(this.selectedNode.id)
      }
    },
    async handleStationDateChange(date) {
      this.stationDate = date
      if (this.selectedNode && this.selectedNode.nodeType === 'station') {
        await this.loadStationData(this.selectedNode.id)
      }
    },
    async handleStationGranularityChange(val) {
      this.stationGranularity = val
      if (this.selectedNode && this.selectedNode.nodeType === 'station') {
        await this.loadStationData(this.selectedNode.id)
      }
    },
    handleStationRefreshIntervalChange(seconds) {
      this.stationRefreshInterval = seconds
      this.startRefreshTimer()
    },
    async handleStationRefresh() {
      if (this.selectedNode) {
        await this.loadNodeData(this.selectedNode)
      }
    },
    handleStationJumpFromCompany(item) {
      const node = this.findNodeById(this.treeData, item.id)
      if (node) {
        this.activeStationTab = 'adjustable'
        this.selectNode(node)
      }
    },
    startRefreshTimer() {
      this.clearRefreshTimer()
      if (this.stationRefreshInterval > 0 && this.selectedNode && this.selectedNode.nodeType === 'station') {
        this.refreshTimer = setInterval(() => {
          this.loadStationData(this.selectedNode.id)
        }, this.stationRefreshInterval * 1000)
      }
    },
    clearRefreshTimer() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    }
  }
}
</script>

<style lang="less" scoped>
.production-monitor-page {
  gap: 18px;

  &__secondary-nav {
    display: flex;
    gap: 0;
    margin-bottom: -8px;
  }

  &__secondary-tab {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 24px;
    font-size: 14px;
    font-weight: 500;
    color: var(--pvms-text-muted, rgba(255, 255, 255, 0.58));
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: all 0.25s ease;
    user-select: none;

    i {
      font-size: 16px;
    }

    &:hover {
      color: var(--pvms-text-secondary, rgba(255, 255, 255, 0.78));
    }

    &.is-active {
      color: #fff;
      border-bottom-color: var(--pvms-primary, #06a299);
    }
  }

  &__station-layout {
    display: flex;
    flex: 1;
    height: calc(100vh - 160px);
    min-height: 500px;
    overflow: hidden;
    border-radius: 6px;
    border: 1px solid var(--pvms-border-soft, rgba(255, 255, 255, 0.08));
    background: var(--pvms-panel, linear-gradient(180deg, rgba(14, 49, 106, 0.96), rgba(9, 29, 67, 0.98)));
  }
}
</style>
