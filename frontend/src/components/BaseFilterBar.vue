<template>
  <!--
    BaseFilterBar
    =============
    Schema-driven filter bar used across dashboard / forecast / production-monitor /
    station-monitoring / strategy pages.

    Commercial-grade pattern:
      • Hierarchy filters (company / region / station / resourceUnit) stay in sync
        with the global stationContext store — they read initial values from and
        write selections back to the store.
      • Dimensional filters (date / type / keyword / status / …) remain local to
        the calling page and travel only via the `search` / `change` events.
      • A single component replaces ~120 lines of per-page duplication.

    Usage:
      <base-filter-bar
        :fields="[
          { key: 'region',     type: 'hier-region',  span: 1 },
          { key: 'stationId',  type: 'hier-station', span: 1 },
          { key: 'dateRange',  type: 'date-range',   span: 2, label: '日期范围' },
          { key: 'keyword',    type: 'input',        span: 1, placeholder: '搜索…' }
        ]"
        :value="query"
        :auto-search="false"
        @search="handleSearch"
        @change="handleChange"
        @reset="handleReset"
      />
  -->
  <section class="pv-filter-bar" :class="{ 'pv-filter-bar--compact': compact }" data-testid="pv-filter-bar">
    <el-form
      :inline="true"
      class="pv-filter-bar__form"
      :style="{ 'grid-template-columns': gridTemplate }"
      @submit.native.prevent
    >
      <el-form-item
        v-for="field in visibleFields"
        :key="field.key"
        :label="field.label"
        :style="{ 'grid-column': 'span ' + (field.span || 1) }"
      >
        <!-- Hierarchy: Company -->
        <el-select
          v-if="field.type === 'hier-company'"
          v-model="localValue.companyId"
          :placeholder="field.placeholder || '全部公司'"
          clearable
          filterable
          @change="handleHierChange('companyId', $event)"
        >
          <el-option :label="field.placeholder || '全部公司'" value="" />
          <el-option
            v-for="item in hierOptions.companies"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <!-- Hierarchy: Region -->
        <el-select
          v-else-if="field.type === 'hier-region'"
          v-model="localValue.regionId"
          :placeholder="field.placeholder || '全部区域'"
          clearable
          filterable
          @change="handleHierChange('regionId', $event)"
        >
          <el-option :label="field.placeholder || '全部区域'" value="" />
          <el-option
            v-for="item in hierOptions.regions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <!-- Hierarchy: Station -->
        <el-select
          v-else-if="field.type === 'hier-station'"
          v-model="localValue.stationId"
          :placeholder="field.placeholder || '全部电站'"
          clearable
          filterable
          @change="handleHierChange('stationId', $event)"
        >
          <el-option :label="field.placeholder || '全部电站'" value="" />
          <el-option
            v-for="item in filteredStations"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <!-- Hierarchy: Resource Unit -->
        <el-select
          v-else-if="field.type === 'hier-resource-unit'"
          v-model="localValue.resourceUnitId"
          :placeholder="field.placeholder || '全部资源单元'"
          clearable
          filterable
          @change="handleHierChange('resourceUnitId', $event)"
        >
          <el-option :label="field.placeholder || '全部资源单元'" value="" />
          <el-option
            v-for="item in filteredResourceUnits"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <!-- Plain select -->
        <el-select
          v-else-if="field.type === 'select'"
          v-model="localValue[field.key]"
          :placeholder="field.placeholder || '请选择'"
          :clearable="field.clearable !== false"
          :filterable="field.filterable === true"
          @change="emitChange(field.key)"
        >
          <el-option
            v-for="item in (field.options || [])"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <!-- Radio group -->
        <el-radio-group
          v-else-if="field.type === 'radio'"
          v-model="localValue[field.key]"
          size="small"
          @change="emitChange(field.key)"
        >
          <el-radio-button
            v-for="item in (field.options || [])"
            :key="item.value"
            :label="item.value"
          >
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>

        <!-- Date range -->
        <el-date-picker
          v-else-if="field.type === 'date-range'"
          v-model="localValue[field.key]"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          @change="emitChange(field.key)"
        />

        <!-- Single date -->
        <el-date-picker
          v-else-if="field.type === 'date'"
          v-model="localValue[field.key]"
          type="date"
          :placeholder="field.placeholder || '选择日期'"
          value-format="yyyy-MM-dd"
          @change="emitChange(field.key)"
        />

        <!-- Number / slider (capacity, etc.) -->
        <el-slider
          v-else-if="field.type === 'range-slider'"
          v-model="localValue[field.key]"
          range
          :min="field.min || 0"
          :max="field.max || 100"
          @change="emitChange(field.key)"
        />

        <!-- Keyword input (default) -->
        <el-input
          v-else
          v-model="localValue[field.key]"
          :placeholder="field.placeholder || '请输入'"
          clearable
          :prefix-icon="field.icon || 'el-icon-search'"
          @keyup.enter.native="emitSearch"
          @clear="emitChange(field.key)"
        />
      </el-form-item>
    </el-form>

    <div v-if="showActions" class="pv-filter-bar__actions">
      <slot name="actions-prepend" />
      <el-button v-if="showReset" plain size="small" icon="el-icon-refresh-left" @click="emitReset">
        重置
      </el-button>
      <el-button v-if="showRefresh" plain size="small" icon="el-icon-refresh" @click="$emit('refresh')">
        刷新
      </el-button>
      <el-button
        v-if="showSearch"
        type="primary"
        size="small"
        icon="el-icon-search"
        data-testid="pv-filter-search"
        @click="emitSearch"
      >
        查询
      </el-button>
      <slot name="actions-append" />
    </div>
  </section>
</template>

<script>
import { mapState, mapActions } from 'vuex'

const HIER_FIELDS = ['hier-company', 'hier-region', 'hier-station', 'hier-resource-unit']

export default {
  name: 'BaseFilterBar',
  props: {
    fields: {
      type: Array,
      required: true
    },
    value: {
      type: Object,
      default: () => ({})
    },
    // grid column count for the form (auto-fit when 0)
    columns: {
      type: Number,
      default: 4
    },
    compact: {
      type: Boolean,
      default: false
    },
    showSearch: { type: Boolean, default: true },
    showRefresh: { type: Boolean, default: true },
    showReset: { type: Boolean, default: true },
    showActions: { type: Boolean, default: true },
    // if true, emit `change` every keystroke/selection (no search button needed)
    autoSearch: { type: Boolean, default: false },
    syncHierarchyFromStore: { type: Boolean, default: true }
  },
  data() {
    return {
      localValue: {}
    }
  },
  computed: {
    ...mapState('stationContext', {
      ctxCompanyId: state => state.companyId,
      ctxRegionId: state => state.regionId,
      ctxStationId: state => state.stationId,
      ctxResourceUnitId: state => state.resourceUnitId,
      ctxCompanies: state => state.companies,
      ctxRegions: state => state.regions,
      ctxStations: state => state.stations,
      ctxResourceUnits: state => state.resourceUnits
    }),
    hierOptions() {
      return {
        companies: this.ctxCompanies,
        regions: this.ctxRegions,
        stations: this.ctxStations,
        resourceUnits: this.ctxResourceUnits
      }
    },
    filteredStations() {
      const list = this.ctxStations || []
      const regionId = this.localValue.regionId
      const resourceUnitId = this.localValue.resourceUnitId
      return list
        .filter(s => !regionId || !s.regionId || s.regionId === regionId)
        .filter(s => !resourceUnitId || s.resourceUnitId === resourceUnitId)
    },
    filteredResourceUnits() {
      const list = this.ctxResourceUnits || []
      const regionId = this.localValue.regionId
      if (!regionId) return list
      return list.filter(u => !u.regionId || u.regionId === regionId)
    },
    visibleFields() {
      return this.fields.filter(f => f && !f.hidden)
    },
    gridTemplate() {
      const cols = this.columns > 0 ? this.columns : 4
      return `repeat(${cols}, minmax(0, 1fr))`
    }
  },
  watch: {
    value: {
      immediate: true,
      deep: true,
      handler(v) {
        this.localValue = Object.assign(this.buildDefaults(), v || {})
        // sync hierarchy from store on first mount
        if (this.syncHierarchyFromStore) {
          if (!this.localValue.companyId && this.ctxCompanyId) this.localValue.companyId = this.ctxCompanyId
          if (!this.localValue.regionId && this.ctxRegionId) this.localValue.regionId = this.ctxRegionId
          if (!this.localValue.stationId && this.ctxStationId) this.localValue.stationId = this.ctxStationId
          if (!this.localValue.resourceUnitId && this.ctxResourceUnitId) this.localValue.resourceUnitId = this.ctxResourceUnitId
        }
      }
    },
    ctxStationId(v) {
      if (!this.syncHierarchyFromStore) {
        return
      }
      if (v !== this.localValue.stationId) {
        this.$set(this.localValue, 'stationId', v)
        this.emitChange('stationId')
      }
    }
  },
  methods: {
    ...mapActions('stationContext', ['focusStation', 'focusResourceUnit']),
    buildDefaults() {
      const defaults = {}
      this.fields.forEach(f => {
        if (f.type === 'date-range') defaults[f.key] = []
        else if (f.type === 'range-slider') defaults[f.key] = [f.min || 0, f.max || 100]
        else if (f.type === 'radio' && f.default !== undefined) defaults[f.key] = f.default
        else defaults[f.key] = ''
      })
      return defaults
    },
    handleHierChange(key, v) {
      this.localValue[key] = v
      // sync to global store
      if (key === 'stationId') {
        const station = this.ctxStations.find(s => s.id === v)
        if (v && station) {
          if (station.resourceUnitId && this.localValue.resourceUnitId !== station.resourceUnitId) {
            this.$set(this.localValue, 'resourceUnitId', station.resourceUnitId)
          }
          this.focusStation({
            id: v,
            name: station.name,
            regionId: station.regionId,
            regionName: station.regionName,
            resourceUnitId: station.resourceUnitId,
            resourceUnitName: station.resourceUnitName
          })
        }
      }
      if (key === 'resourceUnitId') {
        const unit = this.ctxResourceUnits.find(u => u.id === v)
        if (this.localValue.stationId) {
          const station = this.ctxStations.find(s => s.id === this.localValue.stationId)
          if (v && (!station || station.resourceUnitId !== v)) {
            this.$set(this.localValue, 'stationId', '')
          }
        }
        this.focusResourceUnit({ id: v, name: unit ? unit.name : '' })
      }
      this.emitChange(key)
    },
    emitChange(key) {
      this.$emit('input', Object.assign({}, this.localValue))
      this.$emit('change', { key, value: this.localValue[key], payload: Object.assign({}, this.localValue) })
      if (this.autoSearch) {
        this.emitSearch()
      }
    },
    emitSearch() {
      this.$emit('search', Object.assign({}, this.localValue))
    },
    emitReset() {
      this.localValue = this.buildDefaults()
      this.$emit('input', Object.assign({}, this.localValue))
      this.$emit('reset', Object.assign({}, this.localValue))
    }
  }
}

export { HIER_FIELDS }
</script>

<style lang="less" scoped>
.pv-filter-bar {
  --pv-filter-gap: 16px;
  --pv-filter-padding: 18px 20px;

  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--pv-filter-gap);
  padding: var(--pv-filter-padding);
  border: 1px solid var(--pvms-border-soft, rgba(255, 255, 255, 0.06));
  border-radius: 4px;
  background: var(--pvms-panel, rgba(10, 30, 60, 0.82));
  box-shadow: var(--pvms-shadow-soft, 0 6px 20px rgba(0, 0, 0, 0.18));
  flex-wrap: wrap;
}

.pv-filter-bar--compact {
  --pv-filter-gap: 10px;
  --pv-filter-padding: 10px 14px;
}

.pv-filter-bar__form {
  display: grid;
  gap: 10px 12px;
  flex: 1;
  min-width: 0;
  min-height: 40px;
}

.pv-filter-bar__form /deep/ .el-form-item {
  margin-bottom: 0;
  display: flex;
  align-items: center;
  min-width: 0;
  white-space: nowrap;
}

.pv-filter-bar__form /deep/ .el-form-item__label {
  color: var(--pvms-text-muted, rgba(255, 255, 255, 0.55));
  padding-right: 8px;
  flex: 0 0 auto;
  white-space: nowrap;
}

.pv-filter-bar__form /deep/ .el-form-item__content {
  flex: 1;
  min-width: 0;
}

.pv-filter-bar__form /deep/ .el-select,
.pv-filter-bar__form /deep/ .el-date-editor,
.pv-filter-bar__form /deep/ .el-input {
  width: 100%;
  min-width: 120px;
}

.pv-filter-bar__actions {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
  align-items: center;
  min-height: 40px;
}

@media (max-width: 1500px) {
  .pv-filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .pv-filter-bar__form {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }

  .pv-filter-bar__actions {
    justify-content: flex-end;
    margin-top: 8px;
  }
}

@media (max-width: 768px) {
  .pv-filter-bar__form {
    grid-template-columns: 1fr !important;
  }
}
</style>
