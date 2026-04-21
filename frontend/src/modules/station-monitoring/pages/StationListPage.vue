<template>
  <div class="pv-page station-list-page" data-testid="station-list-page">
    <app-page-hero
      title="聚合资源单元"
      description="从虚拟电厂聚合商视角查看资源单元的可调能力、实时出力、预测偏差、在线率与天气条件。"
    >
      <template #meta>
        <div class="station-list-page__meta">
          <span>当前视图 {{ viewMode === 'card' ? '卡片' : '表格' }}</span>
          <span>共 {{ total }} 个资源单元</span>
        </div>
      </template>
    </app-page-hero>

    <section class="pv-card-grid" data-testid="station-statistics">
      <div v-for="item in statistics" :key="item.key" data-testid="station-stat-card">
        <app-metric-card
          :title="item.label"
          :value="item.value"
          :unit="item.unit"
          helper="M02-A 聚合资源 mock"
          :icon="item.icon"
          :accent="item.accent"
        />
      </div>
    </section>

    <app-section-card title="资源单元列表" subtitle="支持关键字、状态、区域和可调容量筛选">
      <station-list-filter-bar
        :query="query"
        :filters="filters"
        :view-mode="viewMode"
        @search="handleSearch"
        @reset="handleReset"
        @change-view="viewMode = $event"
        @refresh="loadData"
      />

      <station-card-grid
        v-if="viewMode === 'card'"
        :stations="resourceUnits"
        @select="handleSelectResourceUnit"
      />
      <station-table-view
        v-else
        :stations="resourceUnits"
        @select="handleSelectResourceUnit"
      />

      <div class="station-list-page__pagination">
        <el-pagination
          data-testid="station-list-pagination"
          layout="total, prev, pager, next"
          :current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </app-section-card>
  </div>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppPageHero from '@/components/AppPageHero.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import { fetchResourceUnitList } from '@/api/pvms'
import StationCardGrid from '../components/StationCardGrid.vue'
import StationListFilterBar from '../components/StationListFilterBar.vue'
import StationTableView from '../components/StationTableView.vue'

export default {
  name: 'StationListPage',
  components: {
    AppMetricCard,
    AppPageHero,
    AppSectionCard,
    StationCardGrid,
    StationListFilterBar,
    StationTableView
  },
  data() {
    return {
      viewMode: 'card',
      query: {
        keyword: '',
        status: '',
        region: '',
        capacityRange: '',
        page: 1,
        pageSize: 6
      },
      filters: {
        statusOptions: [],
        regionOptions: [],
        capacityOptions: []
      },
      statistics: [],
      resourceUnits: [],
      total: 0
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      const response = await fetchResourceUnitList(this.query)
      this.filters = response.data.filters
      this.statistics = response.data.statistics
      this.resourceUnits = response.data.list
      this.total = response.data.total
    },
    handleSearch(nextQuery) {
      this.query = {
        ...this.query,
        ...nextQuery,
        page: 1
      }
      this.loadData()
    },
    handleReset() {
      this.query = {
        keyword: '',
        status: '',
        region: '',
        capacityRange: '',
        page: 1,
        pageSize: 6
      }
      this.loadData()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadData()
    },
    handleSelectResourceUnit(resourceUnit) {
      this.$router.push(`/station/${resourceUnit.id}`)
    }
  }
}
</script>

<style lang="less" scoped>
.station-list-page__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  color: var(--pvms-text-secondary);
  font-size: 12px;
}

.station-list-page__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}
</style>
