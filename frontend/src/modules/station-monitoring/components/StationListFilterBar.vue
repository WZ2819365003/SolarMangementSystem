<template>
  <base-filter-bar
    class="station-list-filter-bar"
    data-testid="station-list-filter-bar"
    :fields="fields"
    :value="localQuery"
    :columns="4"
    @search="handleSearch"
    @change="handleChange"
    @refresh="$emit('refresh')"
    @reset="handleReset"
  >
    <template #actions-prepend>
      <el-radio-group
        :value="viewMode"
        size="small"
        @input="$emit('change-view', $event)"
      >
        <el-radio-button label="card">卡片</el-radio-button>
        <el-radio-button label="table">表格</el-radio-button>
      </el-radio-group>
    </template>
  </base-filter-bar>
</template>

<script>
import BaseFilterBar from '@/components/BaseFilterBar.vue'

export default {
  name: 'StationListFilterBar',
  components: { BaseFilterBar },
  props: {
    query: {
      type: Object,
      default: () => ({})
    },
    filters: {
      type: Object,
      default: () => ({})
    },
    viewMode: {
      type: String,
      default: 'card'
    }
  },
  data() {
    return {
      localQuery: {
        keyword: '',
        status: '',
        region: '',
        capacityRange: ''
      }
    }
  },
  computed: {
    fields() {
      return [
        {
          key: 'keyword',
          label: '关键字',
          type: 'input',
          placeholder: '资源单元 / 区域 / 策略',
          span: 1
        },
        { key: 'status', label: '状态', type: 'select', options: this.filters.statusOptions || [] },
        { key: 'region', label: '区域', type: 'select', options: this.filters.regionOptions || [] },
        {
          key: 'capacityRange',
          label: '可调容量',
          type: 'select',
          options: this.filters.capacityOptions || []
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
      this.localQuery = Object.assign({}, this.localQuery, payload)
      this.$emit('reset')
    },
    handleSearch(payload) {
      this.localQuery = Object.assign({}, this.localQuery, payload || {})
      this.$emit('search', { ...this.localQuery })
    }
  }
}
</script>
