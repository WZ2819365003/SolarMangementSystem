<template>
  <app-section-card
    class="dashboard-map-card"
    data-testid="dashboard-map-card"
    title="GIS 电站总览"
    subtitle="电站分布、运行状态与实时功率总览"
  >
    <template #extra>
      <dashboard-map-filter-panel
        :filters="filters"
        :options="filterOptions"
        @change="$emit('filters-change', $event)"
      />
    </template>

    <div class="dashboard-map-card__stage">
      <div
        ref="mapCanvas"
        class="dashboard-map-card__canvas"
        :class="{ 'is-hidden': !mapReady }"
      />

      <div v-if="!mapReady" class="dashboard-map-card__fallback">
        <div class="dashboard-map-card__fallback-grid" />
        <button
          v-for="item in fallbackMarkers"
          :key="item.id"
          class="dashboard-map-card__fallback-marker"
          :style="{
            left: item.left + '%',
            top: item.top + '%',
            backgroundColor: item.statusColor
          }"
          @click="$emit('station-select', item.id)"
        >
          <span>{{ item.shortLabel }}</span>
        </button>
        <div class="dashboard-map-card__fallback-tip">
          {{ mapError || '地图加载中，已切换到演示定位模式' }}
        </div>
      </div>

      <div class="dashboard-map-card__summary">
        <span
          v-for="item in summary"
          :key="item.key"
          class="dashboard-map-card__summary-item"
        >
          <i :style="{ backgroundColor: item.color }" />
          {{ item.label }} {{ item.count }}
        </span>
      </div>

      <button
        v-if="showResetOverview"
        type="button"
        class="dashboard-map-card__reset"
        data-testid="dashboard-map-reset-overview"
        @click="handleResetOverview"
      >
        返回全景
      </button>

      <dashboard-map-detail
        v-if="selectedStation"
        class="dashboard-map-card__detail"
        :station="selectedStation"
        @view-detail="$emit('view-detail', $event)"
      />
    </div>

    <dashboard-map-insight-panel
      :stations="stations"
      :alarms="alarmItems"
      :selected-station-id="selectedStationId"
      :vpp-node-payload="vppNodePayload"
      @station-select="$emit('station-select', $event)"
    />
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'
import { createAmapLoader } from '@/shared/plugins/amap'
import DashboardMapDetail from './DashboardMapDetail.vue'
import DashboardMapFilterPanel from './DashboardMapFilterPanel.vue'
import DashboardMapInsightPanel from './DashboardMapInsightPanel.vue'

export default {
  name: 'DashboardMapCard',
  components: {
    AppSectionCard,
    DashboardMapDetail,
    DashboardMapFilterPanel,
    DashboardMapInsightPanel
  },
  props: {
    stations: {
      type: Array,
      default: () => []
    },
    summary: {
      type: Array,
      default: () => []
    },
    filters: {
      type: Object,
      default: () => ({})
    },
    filterOptions: {
      type: Object,
      default: () => ({})
    },
    selectedStationId: {
      type: String,
      default: ''
    },
    mapFocusToken: {
      type: Number,
      default: 0
    },
    alarmItems: {
      type: Array,
      default: () => []
    },
    vppNodePayload: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      AMapRef: null,
      circleMarkers: [],
      hasUserSelectionFocus: false,
      mapInstance: null,
      mapReady: false,
      mapError: '',
      overviewViewport: null,
      viewportSyncTimer: null
    }
  },
  computed: {
    selectedStation() {
      return this.stations.find(item => item.id === this.selectedStationId) || null
    },
    showResetOverview() {
      return this.hasUserSelectionFocus && this.circleMarkers.length > 0
    },
    fallbackMarkers() {
      if (!this.stations.length) {
        return []
      }

      const longitudes = this.stations.map(item => item.longitude)
      const latitudes = this.stations.map(item => item.latitude)
      const minLng = Math.min.apply(null, longitudes)
      const maxLng = Math.max.apply(null, longitudes)
      const minLat = Math.min.apply(null, latitudes)
      const maxLat = Math.max.apply(null, latitudes)

      return this.stations.map(item => ({
        id: item.id,
        shortLabel: item.name.slice(0, 2),
        statusColor: item.statusColor,
        left:
          10 +
          (((item.longitude - minLng) / ((maxLng - minLng) || 1)) * 76),
        top:
          12 +
          ((1 - (item.latitude - minLat) / ((maxLat - minLat) || 1)) * 64)
      }))
    }
  },
  watch: {
    filters: {
      deep: true,
      handler() {
        this.hasUserSelectionFocus = false
      }
    },
    stations() {
      if (this.mapReady) {
        this.renderMarkers()
      }
    },
    selectedStationId() {
      if (this.mapReady) {
        this.renderMarkers()
      }
    },
    mapFocusToken() {
      if (!this.mapReady) {
        return
      }

      if (this.viewportSyncTimer) {
        clearTimeout(this.viewportSyncTimer)
        this.viewportSyncTimer = null
      }
      this.hasUserSelectionFocus = true
      this.$nextTick(() => {
        this.focusSelectedStation()
      })
    }
  },
  mounted() {
    this.initializeMap()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.viewportSyncTimer) {
      clearTimeout(this.viewportSyncTimer)
      this.viewportSyncTimer = null
    }
    this.clearMarkers()
    if (this.mapInstance) {
      this.mapInstance.destroy()
      this.mapInstance = null
    }
  },
  methods: {
    async initializeMap() {
      try {
        const AMap = await createAmapLoader({
          plugins: ['AMap.Scale', 'AMap.ToolBar']
        })
        this.AMapRef = AMap
        this.mapInstance = new AMap.Map(this.$refs.mapCanvas, {
          resizeEnable: true,
          zoom: 4.6,
          center: [112.9388, 31.9263],
          mapStyle: 'amap://styles/darkblue',
          viewMode: '2D'
        })
        this.mapReady = true
        this.renderMarkers()
      } catch (error) {
        this.mapReady = false
        this.mapError = '地图服务加载失败，已切换为内置演示视图'
      }
    },
    handleResize() {
      if (this.mapInstance) {
        this.mapInstance.resize()
      }
    },
    clearMarkers() {
      if (!this.circleMarkers.length) {
        return
      }

      this.circleMarkers.forEach(marker => marker.setMap(null))
      this.circleMarkers = []
    },
    renderMarkers() {
      if (!this.mapInstance || !this.AMapRef) {
        return
      }

      this.clearMarkers()

      this.circleMarkers = this.stations.reduce((markers, item) => {
        if (item.id === this.selectedStationId) {
          const haloMarker = new this.AMapRef.CircleMarker({
            center: [item.longitude, item.latitude],
            radius: 18,
            fillColor: item.statusColor,
            fillOpacity: 0.14,
            strokeColor: item.statusColor,
            strokeWeight: 6,
            strokeOpacity: 0.2,
            bubble: true
          })
          haloMarker.setMap(this.mapInstance)
          markers.push(haloMarker)
        }

        const marker = new this.AMapRef.CircleMarker({
          center: [item.longitude, item.latitude],
          radius: item.id === this.selectedStationId ? 11 : 8,
          fillColor: item.statusColor,
          fillOpacity: 0.9,
          strokeColor: '#ffffff',
          strokeWeight: item.id === this.selectedStationId ? 3 : 1.5,
          strokeOpacity: 0.88,
          bubble: true
        })

        marker.setMap(this.mapInstance)
        marker.on('click', () => {
          this.$emit('station-select', item.id)
        })
        markers.push(marker)
        return markers
      }, [])

      if (!this.circleMarkers.length) {
        return
      }

      if (this.hasUserSelectionFocus && this.selectedStation) {
        this.applySelectedViewport()
      } else {
        this.mapInstance.setFitView(this.circleMarkers)
        this.syncOverviewViewport()
      }
    },
    syncOverviewViewport() {
      if (!this.mapInstance) {
        return
      }

      if (this.viewportSyncTimer) {
        clearTimeout(this.viewportSyncTimer)
      }

      this.viewportSyncTimer = window.setTimeout(() => {
        if (this.hasUserSelectionFocus) {
          return
        }

        const center = this.mapInstance.getCenter()
        if (!center) {
          return
        }

        this.overviewViewport = {
          zoom: this.mapInstance.getZoom(),
          center: [center.lng, center.lat]
        }
      }, 160)
    },
    applySelectedViewport() {
      if (!this.mapInstance || !this.selectedStation) {
        return
      }

      this.mapInstance.setZoomAndCenter(14.2, [
        this.selectedStation.longitude,
        this.selectedStation.latitude
      ])
      this.mapInstance.panBy(-120, 30)
    },
    focusSelectedStation() {
      const current = this.selectedStation
      if (!current || !this.mapInstance) {
        return
      }

      this.renderMarkers()
      this.applySelectedViewport()
    },
    handleResetOverview() {
      this.hasUserSelectionFocus = false
      if (this.overviewViewport) {
        this.mapInstance.setZoomAndCenter(
          this.overviewViewport.zoom,
          this.overviewViewport.center
        )
        return
      }

      this.renderMarkers()
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-map-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.dashboard-map-card /deep/ .app-section-card__content {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 16px;
  min-height: 0;
}

.dashboard-map-card__stage {
  position: relative;
  overflow: hidden;
  min-height: 540px;
  flex: none;
  border-radius: 4px;
  background:
    radial-gradient(circle at top right, rgba(26, 141, 255, 0.18), transparent 28%),
    linear-gradient(180deg, rgba(8, 36, 82, 0.96), rgba(5, 22, 52, 0.98));
}

.dashboard-map-card__canvas,
.dashboard-map-card__fallback {
  position: absolute;
  inset: 0;
}

.dashboard-map-card__canvas.is-hidden {
  opacity: 0;
  pointer-events: none;
}

.dashboard-map-card__fallback {
  overflow: hidden;
}

.dashboard-map-card__fallback-grid {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 68px 68px;
  opacity: 0.38;
}

.dashboard-map-card__fallback-marker {
  position: absolute;
  z-index: 1;
  width: 18px;
  height: 18px;
  border: none;
  border-radius: 50%;
  box-shadow: 0 0 0 6px rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transform: translate(-50%, -50%);
}

.dashboard-map-card__fallback-marker span {
  position: absolute;
  top: -24px;
  left: 50%;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(6, 23, 53, 0.9);
  color: var(--pvms-text-primary);
  font-size: 12px;
  white-space: nowrap;
  transform: translateX(-50%);
}

.dashboard-map-card__fallback-tip {
  position: absolute;
  right: 18px;
  bottom: 18px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(6, 23, 53, 0.86);
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.dashboard-map-card__summary {
  position: absolute;
  top: 18px;
  left: 18px;
  z-index: 2;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.dashboard-map-card__summary-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(6, 23, 53, 0.76);
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.dashboard-map-card__summary-item i {
  display: inline-flex;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dashboard-map-card__reset {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 2;
  height: 32px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  background: rgba(6, 23, 53, 0.86);
  color: var(--pvms-text-primary);
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.dashboard-map-card__reset:hover {
  border-color: rgba(26, 141, 255, 0.55);
  background: rgba(14, 42, 86, 0.92);
}

.dashboard-map-card__detail {
  position: absolute;
  right: 18px;
  bottom: 18px;
  z-index: 2;
}

@media (max-width: 1280px) {
  .dashboard-map-card__stage {
    min-height: 500px;
  }
}
</style>
