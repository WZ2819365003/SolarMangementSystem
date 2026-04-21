<template>
  <section v-if="schema" class="global-filter-bar" data-testid="global-filter-bar">
    <base-filter-bar
      :fields="resolvedFields"
      :value="draftValue"
      :columns="schema.columns || 5"
      :sync-hierarchy-from-store="false"
      compact
      @change="handleDraftChange"
      @search="commitFilters"
      @reset="commitFilters"
      @refresh="handleRefresh"
    />
  </section>
</template>

<script>
import BaseFilterBar from '@/components/BaseFilterBar.vue'
import { fetchProductionMonitorMeta, fetchStrategyMeta } from '@/api/pvms'
import { getFilterSchema } from '@/shared/filters/filterSchemas'

function toOption(item) {
  return {
    label: item.label || item.name || item.value,
    value: item.value || item.id || ''
  }
}

export default {
  name: 'GlobalFilterBar',
  components: {
    BaseFilterBar
  },
  data() {
    return {
      draftValue: {},
      strategyTypeOptions: [],
      strategyStatusOptions: [],
      bootstrapped: false
    }
  },
  computed: {
    filterKey() {
      return this.$route.meta && this.$route.meta.filterKey
    },
    schema() {
      return getFilterSchema(this.filterKey)
    },
    moduleFilter() {
      if (!this.filterKey) {
        return {}
      }
      return this.$store.getters['stationContext/getModuleFilter'](this.filterKey)
    },
    resolvedFields() {
      if (!this.schema) {
        return []
      }

      const optionSources = {
        strategyTypes: this.strategyTypeOptions,
        strategyStatuses: this.strategyStatusOptions
      }

      return this.schema.fields.map(field => {
        if (!field.optionSource) {
          return field
        }
        return Object.assign({}, field, {
          options: optionSources[field.optionSource] || []
        })
      })
    }
  },
  watch: {
    filterKey: {
      immediate: true,
      async handler() {
        await this.bootstrapOptions()
        this.resetDraftFromStore()
      }
    },
    moduleFilter: {
      deep: true,
      handler() {
        this.resetDraftFromStore()
      }
    }
  },
  methods: {
    async bootstrapOptions() {
      if (!this.schema) {
        return
      }

      if (!this.bootstrapped) {
        await this.loadScopeCatalog()
        this.bootstrapped = true
      }

      if (this.filterKey && this.filterKey.indexOf('strategy:') === 0 && !this.strategyTypeOptions.length) {
        await this.loadStrategyOptions()
      }
    },
    async loadScopeCatalog() {
      try {
        const response = await fetchProductionMonitorMeta()
        const data = response.data || {}
        const resourceUnits = (data.resourceUnits || []).map(unit => ({
          id: unit.id,
          name: unit.name,
          regionId: unit.region || unit.regionId || '',
          regionName: unit.region || unit.regionName || '',
          city: unit.city || '',
          stationIds: unit.stationIds || []
        }))
        const unitsById = resourceUnits.reduce((result, unit) => {
          result[unit.id] = unit
          return result
        }, {})
        const stations = (data.stations || []).map(station => {
          const unit = unitsById[station.resourceUnitId] || {}
          return {
            id: station.id,
            name: station.name,
            resourceUnitId: station.resourceUnitId || '',
            resourceUnitName: unit.name || '',
            regionId: station.regionId || unit.regionId || '',
            regionName: station.regionName || unit.regionName || ''
          }
        })
        const regions = (data.regionOptions || []).map(toOption).map(item => ({
          id: item.value,
          name: item.label
        })).filter(item => item.id)

        this.$store.dispatch('stationContext/registerRegions', regions)
        this.$store.dispatch('stationContext/registerResourceUnits', resourceUnits)
        this.$store.dispatch('stationContext/registerStations', stations)
      } catch (error) {
        this.$emit('catalog-error', error)
      }
    },
    async loadStrategyOptions() {
      try {
        const response = await fetchStrategyMeta()
        const data = response.data || {}
        this.strategyTypeOptions = [{ label: '全部类型', value: '' }].concat(
          (data.types || []).map(toOption)
        )
        this.strategyStatusOptions = [{ label: '全部状态', value: '' }].concat(
          (data.statuses || []).map(toOption)
        )
      } catch (error) {
        this.$emit('strategy-options-error', error)
      }
    },
    resetDraftFromStore() {
      if (!this.schema) {
        this.draftValue = {}
        return
      }

      const stored = this.moduleFilter || {}
      const hasStoredFilter = Object.keys(stored).length > 0
      const next = Object.assign({}, this.schema.defaults || {}, stored)
      const focus = this.$store.getters['stationContext/focus'] || {}
      if (!hasStoredFilter && !next.resourceUnitId && focus.resourceUnitId) {
        next.resourceUnitId = focus.resourceUnitId
      }
      if (!hasStoredFilter && !next.stationId && focus.stationId) {
        next.stationId = focus.stationId
      }
      const normalized = this.normalizeScope(next)
      this.draftValue = normalized

      if (!hasStoredFilter && this.filterKey && (normalized.resourceUnitId || normalized.stationId)) {
        this.$store.dispatch('stationContext/setModuleFilter', {
          moduleKey: this.filterKey,
          payload: Object.assign({}, normalized, { __updatedAt: Date.now() })
        })
      }
    },
    normalizeScope(payload) {
      const next = Object.assign({}, payload || {})
      if (next.resourceUnitId && next.stationId) {
        const stations = this.$store.state.stationContext.stations || []
        const station = stations.find(item => item.id === next.stationId)
        if (!station || station.resourceUnitId !== next.resourceUnitId) {
          next.stationId = ''
        }
      }
      return next
    },
    handleDraftChange({ payload }) {
      this.draftValue = this.normalizeScope(payload)
    },
    commitFilters(payload) {
      if (!this.filterKey) {
        return
      }

      const next = this.normalizeScope(Object.assign({}, this.draftValue, payload || {}))
      this.draftValue = next
      this.$store.dispatch('stationContext/setModuleFilter', {
        moduleKey: this.filterKey,
        payload: Object.assign({}, next, { __updatedAt: Date.now() })
      })
    },
    handleRefresh() {
      this.commitFilters(this.draftValue)
    }
  }
}
</script>

<style lang="less" scoped>
.global-filter-bar {
  padding: 12px 28px 0;
}

.global-filter-bar /deep/ .pv-filter-bar {
  align-items: center;
}
</style>
