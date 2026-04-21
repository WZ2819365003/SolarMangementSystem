<template>
  <base-filter-bar
    class="strategy-filter-bar"
    data-testid="strategy-filter-bar"
    :fields="fields"
    :value="localQuery"
    :columns="4"
    @search="emitSearch"
    @change="handleChange"
    @refresh="$emit('refresh')"
    @reset="handleReset"
  />
</template>

<script>
import BaseFilterBar from '@/components/BaseFilterBar.vue'

export default {
  name: 'StrategyFilterBar',
  components: { BaseFilterBar },
  props: {
    viewKey: { type: String, default: 'list' },
    query: { type: Object, default: () => ({}) },
    regionOptions: { type: Array, default: () => [] },
    stationOptions: { type: Array, default: () => [] },
    typeOptions: { type: Array, default: () => [] },
    statusOptions: { type: Array, default: () => [] }
  },
  data() {
    return {
      localQuery: {
        region: '',
        stationId: '',
        type: '',
        status: '',
        dateRange: [],
        keyword: ''
      }
    }
  },
  computed: {
    fields() {
      const base = [
        { key: 'region', label: '区域', type: 'select', options: this.regionOptions },
        { key: 'stationId', label: '电站', type: 'select', filterable: true, options: this.stationOptions },
        { key: 'type', label: '策略类型', type: 'select', options: this.typeOptions }
      ]
      if (this.viewKey === 'list') {
        base.push({ key: 'status', label: '状态', type: 'select', options: this.statusOptions })
      }
      if (this.viewKey === 'revenue') {
        base.push({ key: 'dateRange', label: '日期范围', type: 'date-range' })
      }
      base.push({
        key: 'keyword',
        label: '关键词',
        type: 'input',
        placeholder: '策略名称或电站名称'
      })
      return base
    }
  },
  watch: {
    query: {
      immediate: true,
      deep: true,
      handler(v) {
        this.localQuery = Object.assign({}, this.localQuery, v || {})
      }
    }
  },
  methods: {
    handleChange({ payload }) {
      this.localQuery = Object.assign({}, this.localQuery, payload)
    },
    handleReset(payload) {
      this.localQuery = Object.assign({}, this.localQuery, payload)
      this.$emit('search', { ...this.localQuery })
    },
    emitSearch(payload) {
      this.localQuery = Object.assign({}, this.localQuery, payload || {})
      this.$emit('search', { ...this.localQuery })
    }
  }
}
</script>
