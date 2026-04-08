<template>
  <section class="strategy-filter-bar" data-testid="strategy-filter-bar">
    <el-form :inline="true" class="strategy-filter-bar__form" @submit.native.prevent>
      <el-form-item label="资源单元">
        <el-select
          v-model="localQuery.resourceUnitId"
          placeholder="全部资源单元"
          clearable
          @change="handleResourceUnitChange"
        >
          <el-option
            v-for="item in resourceUnitOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="电站">
        <el-select v-model="localQuery.stationId" placeholder="全部电站" clearable>
          <el-option
            v-for="item in filteredStationOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="策略类型">
        <el-select v-model="localQuery.type" placeholder="全部类型" clearable>
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="viewKey === 'list'" label="状态">
        <el-select v-model="localQuery.status" placeholder="全部状态" clearable>
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="viewKey === 'revenue'" label="日期范围">
        <el-date-picker
          v-model="localQuery.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>

      <el-form-item label="关键词">
        <el-input
          v-model.trim="localQuery.keyword"
          clearable
          placeholder="输入策略名称或电站名称"
        />
      </el-form-item>
    </el-form>

    <div class="strategy-filter-bar__actions">
      <el-button plain icon="el-icon-refresh" @click="$emit('refresh')">
        刷新
      </el-button>
      <el-button type="primary" icon="el-icon-search" @click="emitSearch">
        查询
      </el-button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'StrategyFilterBar',
  props: {
    viewKey: {
      type: String,
      default: 'list'
    },
    query: {
      type: Object,
      default: function () { return {} }
    },
    resourceUnitOptions: {
      type: Array,
      default: function () { return [] }
    },
    allStationOptions: {
      type: Array,
      default: function () { return [] }
    },
    typeOptions: {
      type: Array,
      default: function () { return [] }
    },
    statusOptions: {
      type: Array,
      default: function () { return [] }
    }
  },
  data() {
    return {
      localQuery: {
        resourceUnitId: '',
        stationId: '',
        type: '',
        status: '',
        keyword: '',
        dateRange: []
      }
    }
  },
  computed: {
    filteredStationOptions() {
      var ruId = this.localQuery.resourceUnitId
      if (!ruId) return this.allStationOptions
      return this.allStationOptions.filter(function (s) { return s.companyId === ruId })
    }
  },
  watch: {
    query: {
      immediate: true,
      deep: true,
      handler(value) {
        this.localQuery = Object.assign({}, this.localQuery, value)
      }
    }
  },
  methods: {
    handleResourceUnitChange() {
      this.localQuery.stationId = ''
    },
    emitSearch() {
      this.$emit('search', Object.assign({}, this.localQuery))
    }
  }
}
</script>

<style lang="less" scoped>
.strategy-filter-bar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 20px;
  border: 1px solid var(--pvms-border-soft);
  border-radius: 4px;
  background: var(--pvms-panel);
  box-shadow: var(--pvms-shadow-soft);
}

.strategy-filter-bar__form {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px 16px;
  flex: 1;
}

.strategy-filter-bar__form /deep/ .el-form-item {
  margin-bottom: 0;
}

.strategy-filter-bar__form /deep/ .el-form-item__label {
  color: var(--pvms-text-muted);
}

.strategy-filter-bar__form /deep/ .el-select,
.strategy-filter-bar__form /deep/ .el-input,
.strategy-filter-bar__form /deep/ .el-date-editor {
  width: 100%;
}

.strategy-filter-bar__actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 1500px) {
  .strategy-filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .strategy-filter-bar__form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .strategy-filter-bar__actions {
    justify-content: flex-end;
  }
}
</style>
