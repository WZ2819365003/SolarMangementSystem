<template>
  <div class="dashboard-map-filter-panel">
    <el-select
      :value="filters.status"
      size="mini"
      placeholder="状态"
      @change="updateField('status', $event)"
    >
      <el-option
        v-for="item in options.statusOptions || []"
        :key="item.value || 'status-all'"
        :label="item.label"
        :value="item.value"
      />
    </el-select>

    <el-select
      :value="filters.region"
      size="mini"
      placeholder="区域"
      @change="updateField('region', $event)"
    >
      <el-option
        v-for="item in options.regionOptions || []"
        :key="item.value || 'region-all'"
        :label="item.label"
        :value="item.value"
      />
    </el-select>

    <el-select
      :value="filters.capacityRange"
      size="mini"
      placeholder="容量"
      @change="updateField('capacityRange', $event)"
    >
      <el-option
        v-for="item in options.capacityOptions || []"
        :key="item.value || 'capacity-all'"
        :label="item.label"
        :value="item.value"
      />
    </el-select>

    <hs-button size="mini" @click="handleReset">
      重置
    </hs-button>
  </div>
</template>

<script>
export default {
  name: 'DashboardMapFilterPanel',
  props: {
    filters: {
      type: Object,
      default: () => ({
        status: '',
        region: '',
        capacityRange: ''
      })
    },
    options: {
      type: Object,
      default: () => ({})
    }
  },
  methods: {
    updateField(field, value) {
      this.$emit(
        'change',
        Object.assign({}, this.filters, {
          [field]: value
        })
      )
    },
    handleReset() {
      this.$emit('change', {
        status: '',
        region: '',
        capacityRange: ''
      })
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-map-filter-panel {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.dashboard-map-filter-panel /deep/ .el-input__inner {
  min-width: 112px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--pvms-text-primary);
}
</style>
