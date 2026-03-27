<template>
  <section class="production-monitor-filter-bar" data-testid="production-monitor-filter-bar">
    <el-form :inline="true" class="production-monitor-filter-bar__form" @submit.native.prevent>
      <el-form-item label="所属区域">
        <el-select v-model="localQuery.region" placeholder="全部区域" clearable>
          <el-option
            v-for="item in regionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="所属城市">
        <el-select v-model="localQuery.city" placeholder="全部城市" clearable>
          <el-option
            v-for="item in cityOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="资源单元">
        <el-select
          v-model="localQuery.resourceUnitId"
          placeholder="请选择资源单元"
          data-testid="production-resource-unit-select"
        >
          <el-option
            v-for="item in resourceUnitOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="时间粒度">
        <el-select v-model="localQuery.granularity" placeholder="15分钟">
          <el-option label="15 分钟" value="15m" />
          <el-option label="30 分钟" value="30m" />
          <el-option label="60 分钟" value="60m" />
        </el-select>
      </el-form-item>

      <el-form-item label="日期">
        <el-date-picker
          v-model="localQuery.date"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择日期"
        />
      </el-form-item>
    </el-form>

    <div class="production-monitor-filter-bar__actions">
      <el-button plain icon="el-icon-refresh" @click="$emit('refresh')">
        刷新
      </el-button>
      <el-button
        type="primary"
        icon="el-icon-search"
        data-testid="production-monitor-search"
        @click="emitSearch"
      >
        查询
      </el-button>
      <el-button plain icon="el-icon-download" @click="$emit('export')">
        导出
      </el-button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'ProductionMonitorFilterBar',
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
        date: '',
        granularity: '15m'
      }
    }
  },
  watch: {
    query: {
      immediate: true,
      deep: true,
      handler(value) {
        this.localQuery = {
          ...this.localQuery,
          ...value
        }
      }
    }
  },
  methods: {
    emitSearch() {
      this.$emit('search', {
        ...this.localQuery
      })
    }
  }
}
</script>

<style lang="less" scoped>
.production-monitor-filter-bar {
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

.production-monitor-filter-bar__form {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px 16px;
  flex: 1;
}

.production-monitor-filter-bar__form /deep/ .el-form-item {
  margin-bottom: 0;
}

.production-monitor-filter-bar__form /deep/ .el-form-item__label {
  color: var(--pvms-text-muted);
}

.production-monitor-filter-bar__form /deep/ .el-select,
.production-monitor-filter-bar__form /deep/ .el-date-editor {
  width: 100%;
}

.production-monitor-filter-bar__actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 1500px) {
  .production-monitor-filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .production-monitor-filter-bar__form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .production-monitor-filter-bar__actions {
    justify-content: flex-end;
  }
}
</style>
