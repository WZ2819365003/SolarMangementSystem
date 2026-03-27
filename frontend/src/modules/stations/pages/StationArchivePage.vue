<template>
  <div class="station-archive-page">
    <station-tree-panel
      :tree-data="treeData"
      :selected-node-id="selectedNodeId"
      :loading="treeLoading"
      @node-click="handleNodeClick"
      @status-filter="handleStatusFilter"
    />
    <station-data-panel
      :selected-node="selectedNode"
      :company-data="companyData"
      :resource-data="resourceData"
      :station-realtime-data="stationRealtimeData"
      :active-tab="activeTab"
      :current-date="currentDate"
      :granularity="granularity"
      :refresh-interval="refreshInterval"
      @tab-change="handleTabChange"
      @date-change="handleDateChange"
      @granularity-change="handleGranularityChange"
      @refresh-interval-change="handleRefreshIntervalChange"
      @refresh="handleRefresh"
      @resource-click="handleResourceClick"
      @resource-station-click="handleStationJump"
    />
  </div>
</template>

<script>
import {
  fetchStationTree,
  fetchStationArchiveCompanyOverview,
  fetchStationArchiveResourceOverview,
  fetchStationArchiveRealtime,
  fetchStationArchiveStrategy
} from '@/api/pvms'
import StationTreePanel from '../components/StationTreePanel.vue'
import StationDataPanel from '../components/StationDataPanel.vue'

export default {
  name: 'StationArchivePage',
  components: {
    StationTreePanel,
    StationDataPanel
  },
  data() {
    return {
      treeData: [],
      treeLoading: false,
      selectedNodeId: '',
      selectedNode: null,
      companyData: null,
      resourceData: null,
      stationRealtimeData: null,
      activeTab: 'active-power',
      currentDate: new Date().toISOString().slice(0, 10),
      granularity: '15min',
      refreshInterval: 30,
      refreshTimer: null,
      statusFilter: ''
    }
  },
  async created() {
    await this.loadTree()
    this.restoreFromQuery()
  },
  beforeDestroy() {
    this.clearRefreshTimer()
  },
  methods: {
    async loadTree() {
      this.treeLoading = true
      try {
        const response = await fetchStationTree({ status: this.statusFilter })
        this.treeData = response.data.tree || []
      } catch (e) {
        console.error('[StationArchive] 加载树失败', e)
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
      this.updateQuery(node)
      this.loadNodeData(node)
    },
    updateQuery(node) {
      const query = { nodeType: node.nodeType, nodeId: node.id }
      this.$router.replace({ query }).catch(() => {})
    },
    async loadNodeData(node) {
      this.companyData = null
      this.resourceData = null
      this.stationRealtimeData = null

      if (node.nodeType === 'company') {
        await this.loadCompanyOverview(node.id)
      } else if (node.nodeType === 'resource') {
        await this.loadResourceOverview(node.id)
      } else if (node.nodeType === 'station') {
        await this.loadStationData(node.id)
        this.startRefreshTimer()
      }
    },
    async loadCompanyOverview(companyId) {
      try {
        const response = await fetchStationArchiveCompanyOverview({ companyId })
        this.companyData = response.data
      } catch (e) {
        console.error('[StationArchive] 加载公司数据失败', e)
      }
    },
    async loadResourceOverview(resourceId) {
      try {
        const response = await fetchStationArchiveResourceOverview({ resourceId })
        this.resourceData = response.data
      } catch (e) {
        console.error('[StationArchive] 加载资源数据失败', e)
      }
    },
    async loadStationData(stationId) {
      try {
        const metric = this.activeTab
        if (metric === 'strategy') {
          const response = await fetchStationArchiveStrategy({ stationId })
          this.stationRealtimeData = response.data
        } else {
          const response = await fetchStationArchiveRealtime({
            stationId,
            metric,
            date: this.currentDate,
            granularity: this.granularity
          })
          this.stationRealtimeData = response.data
        }
      } catch (e) {
        console.error('[StationArchive] 加载电站数据失败', e)
      }
    },
    handleNodeClick(node) {
      this.clearRefreshTimer()
      this.activeTab = 'active-power'
      this.selectNode(node)
    },
    async handleStatusFilter(status) {
      this.statusFilter = status
      await this.loadTree()
    },
    async handleTabChange(tabKey) {
      this.activeTab = tabKey
      if (this.selectedNode && this.selectedNode.nodeType === 'station') {
        await this.loadStationData(this.selectedNode.id)
      }
    },
    async handleDateChange(date) {
      this.currentDate = date
      if (this.selectedNode && this.selectedNode.nodeType === 'station') {
        await this.loadStationData(this.selectedNode.id)
      }
    },
    async handleGranularityChange(val) {
      this.granularity = val
      if (this.selectedNode && this.selectedNode.nodeType === 'station') {
        await this.loadStationData(this.selectedNode.id)
      }
    },
    handleRefreshIntervalChange(seconds) {
      this.refreshInterval = seconds
      this.startRefreshTimer()
    },
    async handleRefresh() {
      if (this.selectedNode) {
        await this.loadNodeData(this.selectedNode)
      }
    },
    handleResourceClick(resource) {
      const node = this.findNodeById(this.treeData, resource.id)
      if (node) {
        this.selectNode(node)
      }
    },
    handleStationJump(station) {
      const node = this.findNodeById(this.treeData, station.id)
      if (node) {
        this.activeTab = 'active-power'
        this.selectNode(node)
      }
    },
    startRefreshTimer() {
      this.clearRefreshTimer()
      if (this.refreshInterval > 0 && this.selectedNode && this.selectedNode.nodeType === 'station') {
        this.refreshTimer = setInterval(() => {
          this.loadStationData(this.selectedNode.id)
        }, this.refreshInterval * 1000)
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
.station-archive-page {
  display: flex;
  height: calc(100vh - 60px);
  overflow: hidden;
}
</style>
