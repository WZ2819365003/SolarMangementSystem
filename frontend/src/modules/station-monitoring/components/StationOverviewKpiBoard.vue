<template>
  <section class="pv-card-grid" data-testid="station-overview-kpis">
    <div
      v-for="item in kpiCards"
      :key="item.key"
      data-testid="station-overview-kpi-card"
    >
      <app-metric-card
        :title="item.title"
        :value="item.value"
        :unit="item.unit"
        :helper="item.helper"
        :icon="item.icon"
        :accent="item.accent"
      />
    </div>
  </section>
</template>

<script>
import AppMetricCard from '@/components/AppMetricCard.vue'

export default {
  name: 'StationOverviewKpiBoard',
  components: {
    AppMetricCard
  },
  props: {
    kpi: {
      type: Object,
      default: () => ({})
    }
  },
  computed: {
    kpiCards() {
      return [
        {
          key: 'realtime',
          title: '实时出力',
          value: this.kpi.realtimePowerMw || 0,
          unit: 'MW',
          helper: '当前采样窗口 5 分钟',
          icon: 'el-icon-lightning',
          accent: 'blue'
        },
        {
          key: 'today',
          title: '当日电量',
          value: this.kpi.todayEnergyMwh || 0,
          unit: 'MWh',
          helper: '按日累计',
          icon: 'el-icon-data-analysis',
          accent: 'teal'
        },
        {
          key: 'up',
          title: '上调可调能力',
          value: this.kpi.upRegulationMw || 0,
          unit: 'MW',
          helper: '当前可追加上调',
          icon: 'el-icon-top',
          accent: 'emerald'
        },
        {
          key: 'down',
          title: '下调可调能力',
          value: this.kpi.downRegulationMw || 0,
          unit: 'MW',
          helper: '当前可压降空间',
          icon: 'el-icon-bottom',
          accent: 'orange'
        },
        {
          key: 'online',
          title: '在线率',
          value: this.kpi.onlineRate || 0,
          unit: '%',
          helper: '成员电站在线情况',
          icon: 'el-icon-success',
          accent: 'blue'
        },
        {
          key: 'forecast',
          title: '预测准确率',
          value: this.kpi.forecastAccuracy || 0,
          unit: '%',
          helper: '滚动预测评估',
          icon: 'el-icon-s-marketing',
          accent: 'emerald'
        }
      ]
    }
  }
}
</script>
