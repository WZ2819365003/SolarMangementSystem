<template>
  <base-filter-bar
    class="production-monitor-filter-bar"
    data-testid="production-monitor-filter-bar"
    :fields="fields"
    :value="localQuery"
    :columns="5"
    @search="emitSearch"
    @change="handleChange"
    @refresh="$emit('refresh')"
    @reset="handleReset"
  >
    <template #actions-append>
      <el-button plain size="small" icon="el-icon-download" @click="$emit('export')">
        导出
      </el-button>
    </template>
  </base-filter-bar>
</template>

<script>
import BaseFilterBar from '@/components/BaseFilterBar.vue'

export default {
  name: 'ProductionMonitorFilterBar',
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
    cityOptions: {
      type: Array,
      default: () => []
    },
    resourceUnitOptions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      localQuery: {
        region: '',
        city: '',
        resourceUnitId: '',
        granularity: '15m',
        date: ''
      }
    }
  },
  computed: {
    fields() {
      return [
        { key: 'region', label: '所属区域', type: 'select', options: this.regionOptions },
        { key: 'city', label: '所属城市', type: 'select', options: this.cityOptions },
        {
          key: 'resourceUnitId',
          label: '资源单元',
          type: 'select',
          filterable: true,
          clearable: false,
          options: this.resourceUnitOptions
        },
        {
          key: 'granularity',
          label: '时间粒度',
          type: 'select',
          clearable: false,
          options: [
            { value: '15m', label: '15 分钟' },
            { value: '30m', label: '30 分钟' },
            { value: '60m', label: '60 分钟' }
          ]
        },
        { key: 'date', label: '日期', type: 'date' }
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
      this.localQuery = Object.assign({}, this.localQuery, payload, { granularity: '15m' })
      this.$emit('search', { ...this.localQuery })
    },
    emitSearch(payload) {
      this.localQuery = Object.assign({}, this.localQuery, payload || {})
      this.$emit('search', { ...this.localQuery })
    }
  }
}
</script>
