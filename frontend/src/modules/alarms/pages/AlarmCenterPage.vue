<template>
  <div class="pv-page">
    <app-page-hero
      title="告警中心"
      description="统一处理严重、重要和一般告警，支持后续把派单、工单和闭环动作继续拆成独立模块。"
    />

    <section class="pv-card-grid">
      <app-metric-card
        v-for="item in summaryCards"
        :key="item.label"
        :title="item.label"
        :value="item.value"
        :unit="item.unit"
        helper="取值自当前 mock 接口"
        icon="el-icon-warning-outline"
        accent="orange"
      />
    </section>

    <section class="pv-split-grid">
      <app-section-card title="处理看板" subtitle="当前处置规则">
        <ul class="pv-list">
          <li v-for="item in processBoard" :key="item" class="pv-list__item">
            <div class="pv-list__title">
              {{ item }}
            </div>
          </li>
        </ul>
      </app-section-card>

      <app-section-card title="值班提示" subtitle="告警收敛建议">
        <div class="pv-text-muted" style="line-height: 24px;">
          当严重告警与通讯中断集中在同一站点时，建议优先排查采集链路，再触发现场巡检派单。
        </div>
      </app-section-card>
    </section>

    <app-section-card title="告警列表" subtitle="支持按级别和状态过滤">
      <div class="pv-toolbar">
        <el-form :inline="true" :model="query">
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="告警 / 站点 / 设备" clearable />
          </el-form-item>
          <el-form-item label="级别">
            <el-select v-model="query.level" clearable placeholder="全部">
              <el-option label="严重" value="严重" />
              <el-option label="重要" value="重要" />
              <el-option label="一般" value="一般" />
              <el-option label="提示" value="提示" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="全部">
              <el-option label="待派单" value="待派单" />
              <el-option label="处理中" value="处理中" />
              <el-option label="待确认" value="待确认" />
              <el-option label="已关闭" value="已关闭" />
            </el-select>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="loadData">
          查询
        </el-button>
      </div>

      <el-table :data="rows" stripe>
        <el-table-column prop="alarmId" label="告警编号" min-width="160" />
        <el-table-column prop="stationName" label="站点" min-width="180" />
        <el-table-column prop="deviceName" label="设备" min-width="140" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="level" label="级别" width="110">
          <template slot-scope="{ row }">
            <el-tag :type="resolveTagType(row.level)">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="{ row }">
            <el-tag :type="resolveTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="责任人" width="120" />
        <el-table-column prop="happenedAt" label="发生时间" min-width="180" />
      </el-table>
      <el-pagination
        layout="total, prev, pager, next"
        :total="total"
        :page-size="10"
      />
    </app-section-card>
  </div>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'
import AppPageHero from '@/components/AppPageHero.vue'
import AppSectionCard from '@/components/AppSectionCard.vue'
import { fetchAlarmCenter } from '@/api/pvms'
import { resolveTagType } from '@/utils/formatters'

export default {
  name: 'AlarmCenterPage',
  components: {
    AppMetricCard,
    AppPageHero,
    AppSectionCard
  },
  data() {
    return {
      query: {
        keyword: '',
        level: '',
        status: ''
      },
      summaryCards: [],
      processBoard: [],
      rows: [],
      total: 0
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    resolveTagType,
    async loadData() {
      const response = await fetchAlarmCenter(this.query)
      this.summaryCards = response.data.summaryCards
      this.processBoard = response.data.processBoard
      this.rows = response.data.rows
      this.total = response.data.total
    }
  }
}
</script>
