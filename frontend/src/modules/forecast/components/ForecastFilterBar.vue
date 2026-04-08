<template>
  <section class="forecast-filter-bar" data-testid="forecast-filter-bar">
    <el-form :inline="true" class="forecast-filter-bar__form" @submit.native.prevent>
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

      <el-form-item label="日期范围">
        <el-date-picker
          v-model="localQuery.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>

      <el-form-item label="预测类型">
        <el-radio-group v-model="localQuery.forecastType" size="small">
          <el-radio-button label="day-ahead">日前预测</el-radio-button>
          <el-radio-button label="ultra-short">超短期预测</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <div class="forecast-filter-bar__actions">
      <el-button plain icon="el-icon-refresh" @click="$emit('refresh')">
        刷新
      </el-button>
      <el-button
        type="primary"
        icon="el-icon-search"
        data-testid="forecast-search"
        @click="emitSearch"
      >
        查询
      </el-button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'ForecastFilterBar',
  props: {
    query: {
      type: Object,
      default: () => ({})
    },
    resourceUnitOptions: {
      type: Array,
      default: () => []
    },
    allStationOptions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      localQuery: {
        resourceUnitId: '',
        stationId: '',
        dateRange: [],
        forecastType: 'day-ahead'
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
.forecast-filter-bar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 20px;
  border: 1px solid var(--pvms-border-soft);
  border-radius: 4px;
  background: var(--pvms-panel);
  box-shadow: var(--pvms-shadow-soft);
}

.forecast-filter-bar__form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 16px;
  flex: 1;
}

.forecast-filter-bar__form /deep/ .el-form-item {
  margin-bottom: 0;
}

.forecast-filter-bar__form /deep/ .el-form-item__label {
  color: var(--pvms-text-muted);
}

.forecast-filter-bar__form /deep/ .el-select {
  width: 150px;
}

.forecast-filter-bar__form /deep/ .el-date-editor {
  width: 230px;
}

.forecast-filter-bar__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
</style>
