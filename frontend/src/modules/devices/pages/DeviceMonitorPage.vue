<template>
  <div class="pv-page">
    <app-page-hero
      title="设备监控"
      description="以设备在线率、告警状态和运维建议构成设备监控页，样式遵循宿主的深色面板和高亮状态设计。"
    />

    <section class="pv-card-grid">
      <app-metric-card
        v-for="item in summaryCards"
        :key="item.label"
        :title="item.label"
        :value="item.value"
        :unit="item.unit"
        helper="实时采样窗口 15 分钟"
        icon="el-icon-cpu"
        accent="blue"
      />
    </section>

    <section class="pv-split-grid">
      <app-section-card title="设备分布" subtitle="按设备类型统计">
        <div class="pv-group-list">
          <div v-for="item in pagedDeviceGroups" :key="item.label" class="pv-group-list__row">
            <span>{{ item.label }}</span>
            <div class="pv-group-list__track">
              <span :style="{ width: item.ratio + '%' }" />
            </div>
            <span class="pv-text-muted">{{ item.ratio }}%</span>
          </div>
        </div>
        <el-pagination
          small
          background
          layout="total, prev, pager, next"
          :page-size="deviceGroupPageSize"
          :current-page="deviceGroupCurrentPage"
          :total="deviceGroups.length"
          style="margin-top: 12px; text-align: right;"
          @current-change="val => deviceGroupCurrentPage = val"
        />
      </app-section-card>

      <app-section-card title="运维提示" subtitle="模拟值班建议">
        <ul class="pv-list">
          <li v-for="item in maintenanceTips" :key="item" class="pv-list__item">
            <div class="pv-list__title">
              {{ item }}
            </div>
          </li>
        </ul>
      </app-section-card>
    </section>

    <app-section-card title="设备状态列表" subtitle="支持按名称和状态筛选">
      <div class="pv-toolbar">
        <el-form :inline="true" :model="query">
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="设备 / 站点 / 类型" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="全部">
              <el-option label="在线" value="在线" />
              <el-option label="离线" value="离线" />
              <el-option label="告警" value="告警" />
            </el-select>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="loadData">
          查询
        </el-button>
      </div>

      <el-table :data="pagedRows" stripe size="mini">
        <el-table-column prop="deviceId" label="设备编号" min-width="130" />
        <el-table-column prop="deviceName" label="设备名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="stationName" label="所属站点" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="90">
          <template slot-scope="{ row }">
            <el-tag :type="resolveTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loadRate" label="负荷率" min-width="90" />
        <el-table-column prop="temperature" label="温度" min-width="90" />
        <el-table-column prop="lastReportAt" label="最后上报时间" min-width="160" />
      </el-table>
      <el-pagination
        small
        background
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        :current-page="currentPage"
        :total="filteredRows.length"
        style="margin-top: 12px; text-align: right;"
        @size-change="val => { pageSize = val; currentPage = 1 }"
        @current-change="val => currentPage = val"
      />
    </app-section-card>
  </div>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppPageHero from '@/components/AppPageHero.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import { fetchDeviceMonitor } from '@/api/pvms'
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'DeviceMonitorPage',
  components: {
    AppMetricCard,
    AppPageHero,
    AppSectionCard
  },
  data() {
    return {
      query: {
        keyword: '',
        status: ''
      },
      summaryCards: [],
      deviceGroups: [],
      maintenanceTips: [],
      rows: [],
      currentPage: 1,
      pageSize: 10,
      deviceGroupCurrentPage: 1,
      deviceGroupPageSize: 4
    }
  },
  computed: {
    filteredRows() {
      var kw = this.query.keyword ? this.query.keyword.toLowerCase() : ''
      var st = this.query.status
      return this.rows.filter(function (r) {
        if (kw && (r.deviceName + r.stationName + r.type).toLowerCase().indexOf(kw) === -1) return false
        if (st && r.status !== st) return false
        return true
      })
    },
    pagedRows() {
      var start = (this.currentPage - 1) * this.pageSize
      return this.filteredRows.slice(start, start + this.pageSize)
    },
    pagedDeviceGroups() {
      var start = (this.deviceGroupCurrentPage - 1) * this.deviceGroupPageSize
      return this.deviceGroups.slice(start, start + this.deviceGroupPageSize)
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    resolveTagType,
    async loadData() {
      const response = await fetchDeviceMonitor(this.query)
      this.summaryCards = response.data.summaryCards
      this.deviceGroups = response.data.deviceGroups
      this.maintenanceTips = response.data.maintenanceTips
      this.rows = response.data.rows
      this.currentPage = 1
      this.deviceGroupCurrentPage = 1
    }
  }
}
</script>
