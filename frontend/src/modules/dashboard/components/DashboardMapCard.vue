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
import { createAmapLoader, getAmapConfig } from '@/shared/plugins/amap'
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
      LeafletRef: null,
      circleMarkers: [],
      hasUserSelectionFocus: false,
      mapInstance: null,
      mapProvider: '',
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
      this.destroyMap()
      this.mapInstance = null
    }
  },
  methods: {
    async initializeMap() {
      const config = getAmapConfig()
      if (config.key) {
        try {
          await this.initializeAmap()
          return
        } catch (error) {
          this.mapError = this.formatMapLoadError(error, '高德地图加载失败')
        }
      }

      try {
        await this.initializeLeaflet()
      } catch (error) {
        if (!config.key) {
          this.mapError = '高德地图 API 密钥未配置'
        }
        this.mapError = this.formatMapLoadError(error, this.mapError || '地图服务加载失败')
        this.destroyMap()
        this.mapProvider = ''
        this.mapReady = false
      }
    },
    formatMapLoadError(error, fallback) {
      return error && error.message ? `${fallback}（${error.message}）` : fallback
    },
    async initializeAmap() {
      const AMap = await createAmapLoader({
        plugins: ['AMap.Scale', 'AMap.ToolBar']
      })
      this.AMapRef = AMap
      this.mapProvider = 'amap'
      this.mapInstance = new AMap.Map(this.$refs.mapCanvas, {
        resizeEnable: true,
        zoom: 4.6,
        center: [112.9388, 31.9263],
        mapStyle: 'amap://styles/darkblue',
        viewMode: '2D'
      })
      this.mapReady = true
      this.mapError = ''
      this.renderMarkers()
    },
    async initializeLeaflet() {
      const leafletModule = await import('leaflet')
      await import('leaflet/dist/leaflet.css')
      const L = leafletModule.default || leafletModule
      this.LeafletRef = L
      this.mapProvider = 'leaflet'
      this.mapInstance = L.map(this.$refs.mapCanvas, {
        zoomControl: true,
        attributionControl: true
      }).setView([31.9263, 112.9388], 5)
      this.$refs.mapCanvas.classList.add('leaflet-container')
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 18
      }).addTo(this.mapInstance)
      this.mapReady = true
      this.mapError = ''
      this.renderMarkers()
      this.$nextTick(() => {
        if (this.mapInstance && this.mapProvider === 'leaflet') {
          this.mapInstance.invalidateSize()
        }
      })
    },
    destroyMap() {
      if (!this.mapInstance) {
        return
      }

      if (this.mapProvider === 'leaflet' && this.mapInstance.remove) {
        this.mapInstance.remove()
      } else if (this.mapInstance.destroy) {
        this.mapInstance.destroy()
      }
      this.mapProvider = ''
    },
    handleResize() {
      if (!this.mapInstance) {
        return
      }

      if (this.mapProvider === 'leaflet' && this.mapInstance.invalidateSize) {
        this.mapInstance.invalidateSize()
        return
      }

      if (this.mapInstance.resize) {
        this.mapInstance.resize()
      }
    },
    clearMarkers() {
      if (!this.circleMarkers.length) {
        return
      }

      if (this.mapProvider === 'leaflet') {
        this.circleMarkers.forEach(marker => {
          if (this.mapInstance && this.mapInstance.removeLayer) {
            this.mapInstance.removeLayer(marker)
          }
        })
      } else {
        this.circleMarkers.forEach(marker => marker.setMap(null))
      }
      this.circleMarkers = []
    },
    renderMarkers() {
      if (!this.mapInstance) {
        return
      }

      if (this.mapProvider === 'leaflet') {
        this.renderLeafletMarkers()
        return
      }

      if (this.AMapRef) {
        this.renderAmapMarkers()
      }
    },
    renderAmapMarkers() {
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

      this.applyMarkerViewport()
    },
    renderLeafletMarkers() {
      if (!this.mapInstance || !this.LeafletRef) {
        return
      }

      this.clearMarkers()

      this.circleMarkers = this.stations.reduce((markers, item) => {
        if (item.id === this.selectedStationId) {
          const haloMarker = this.LeafletRef.circleMarker([item.latitude, item.longitude], {
            radius: 19,
            color: item.statusColor,
            weight: 6,
            opacity: 0.22,
            fillColor: item.statusColor,
            fillOpacity: 0.12
          }).addTo(this.mapInstance)
          markers.push(haloMarker)
        }

        const marker = this.LeafletRef.circleMarker([item.latitude, item.longitude], {
          radius: item.id === this.selectedStationId ? 11 : 8,
          color: '#ffffff',
          weight: item.id === this.selectedStationId ? 3 : 1.5,
          opacity: 0.88,
          fillColor: item.statusColor,
          fillOpacity: 0.9
        }).addTo(this.mapInstance)

        marker.on('click', () => {
          this.$emit('station-select', item.id)
        })
        markers.push(marker)
        return markers
      }, [])

      this.applyMarkerViewport()
    },
    applyMarkerViewport() {
      if (!this.circleMarkers.length) {
        return
      }

      if (this.hasUserSelectionFocus && this.selectedStation) {
        this.applySelectedViewport()
      } else if (this.mapProvider === 'leaflet') {
        const group = this.LeafletRef.featureGroup(this.circleMarkers)
        this.mapInstance.fitBounds(group.getBounds().pad(0.12), {
          maxZoom: 12
        })
        this.syncOverviewViewport()
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
          center: this.mapProvider === 'leaflet'
            ? { lng: center.lng, lat: center.lat }
            : [center.lng, center.lat]
        }
      }, 160)
    },
    applySelectedViewport() {
      if (!this.mapInstance || !this.selectedStation) {
        return
      }

      if (this.mapProvider === 'leaflet') {
        this.mapInstance.setView([
          this.selectedStation.latitude,
          this.selectedStation.longitude
        ], 14)
        this.mapInstance.panBy([-120, 30])
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
        if (this.mapProvider === 'leaflet') {
          this.mapInstance.setView([
            this.overviewViewport.center.lat,
            this.overviewViewport.center.lng
          ], this.overviewViewport.zoom)
          return
        }

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

.dashboard-map-card /deep/ .leaflet-container {
  background: #061735;
  color: var(--pvms-text-secondary);
}

.dashboard-map-card /deep/ .leaflet-control-attribution {
  background: rgba(6, 23, 53, 0.72);
  color: var(--pvms-text-muted);
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
  z-index: 500;
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
  z-index: 500;
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
  z-index: 500;
}

@media (max-width: 1280px) {
  .dashboard-map-card__stage {
    min-height: 500px;
  }
}
</style>
