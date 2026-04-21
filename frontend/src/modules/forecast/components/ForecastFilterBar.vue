<template>
  <base-filter-bar
    class="forecast-filter-bar"
    data-testid="forecast-filter-bar"
    :fields="fields"
    :value="localQuery"
    @search="emitSearch"
    @change="handleChange"
    @refresh="$emit('refresh')"
    @reset="handleReset"
  />
</template>

<script>
import BaseFilterBar from '@/components/BaseFilterBar.vue'

export default {
  name: 'ForecastFilterBar',
  components: { BaseFilterBar },
  props: {
    query: {
      type: Object,
      default: () => ({})
    },
    regionOptions: {
      type: Array,
      default: () => []
    },
    stationOptions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      localQuery: {
        region: '',
        stationId: '',
        dateRange: [],
        forecastType: 'day-ahead'
      }
    }
  },
  computed: {
    fields() {
      return [
        {
          key: 'region',
          label: '区域',
          type: 'select',
          options: this.regionOptions.map(o => ({ value: o.value, label: o.label }))
        },
        {
          key: 'stationId',
          label: '电站',
          type: 'select',
          filterable: true,
          options: this.stationOptions.map(o => ({ value: o.value, label: o.label }))
        },
        {
          key: 'dateRange',
          label: '日期范围',
          type: 'date-range',
          span: 1
        },
        {
          key: 'forecastType',
          label: '预测类型',
          type: 'radio',
          default: 'day-ahead',
          options: [
            { value: 'day-ahead', label: '日前预测' },
            { value: 'ultra-short', label: '超短期预测' }
          ]
        }
      ]
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
      this.localQuery = Object.assign({}, this.localQuery, payload, { forecastType: 'day-ahead' })
      this.$emit('search', { ...this.localQuery })
    },
    emitSearch(payload) {
      this.localQuery = Object.assign({}, this.localQuery, payload || {})
      this.$emit('search', { ...this.localQuery })
    }
  }
}
</script>
