<template>
  <div class="station-list-filter-bar" data-testid="station-list-filter-bar">
    <el-form :inline="true" :model="localQuery" class="station-list-filter-bar__form">
      <el-form-item label="关键字">
        <el-input
          v-model="localQuery.keyword"
          clearable
          placeholder="资源单元名称 / 聚合区域 / 策略说明"
          @keyup.enter.native="handleSearch"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="localQuery.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in filters.statusOptions || []"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="区域">
        <el-select v-model="localQuery.region" clearable placeholder="全部区域">
          <el-option
            v-for="item in filters.regionOptions || []"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="可调容量">
        <el-select v-model="localQuery.capacityRange" clearable placeholder="全部容量">
          <el-option
            v-for="item in filters.capacityOptions || []"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <div class="station-list-filter-bar__actions">
      <el-radio-group :value="viewMode" size="mini" @input="$emit('change-view', $event)">
        <el-radio-button label="card">
          卡片视图
        </el-radio-button>
        <el-radio-button label="table">
          表格视图
        </el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="handleSearch">
        查询
      </el-button>
      <el-button @click="handleReset">
        重置
      </el-button>
      <el-button icon="el-icon-refresh" @click="$emit('refresh')">
        刷新
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StationListFilterBar',
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
  watch: {
    query: {
      immediate: true,
      deep: true,
      handler(value) {
        this.localQuery = {
          keyword: value.keyword || '',
          status: value.status || '',
          region: value.region || '',
          capacityRange: value.capacityRange || ''
        }
      }
    }
  },
  methods: {
    handleSearch() {
      this.$emit('search', { ...this.localQuery })
    },
    handleReset() {
      this.localQuery = {
        keyword: '',
        status: '',
        region: '',
        capacityRange: ''
      }
      this.$emit('reset')
    }
  }
}
</script>

<style lang="less" scoped>
.station-list-filter-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.station-list-filter-bar__form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 0;
}

.station-list-filter-bar__actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.station-list-filter-bar /deep/ .el-input__inner,
.station-list-filter-bar /deep/ .el-select .el-input__inner {
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: var(--pvms-text-primary);
}

@media (max-width: 1320px) {
  .station-list-filter-bar {
    flex-direction: column;
  }

  .station-list-filter-bar__actions {
    flex-wrap: wrap;
  }
}
</style>
