<template>
  <app-section-card
    class="dashboard-station-ranking"
    data-testid="dashboard-station-ranking"
    title="发电排名"
    subtitle="日发电量 / 等效小时 / PR 值"
  >
    <template #extra>
      <el-radio-group
        size="mini"
        :value="payload.currentMetric"
        @input="$emit('metric-change', $event)"
      >
        <el-radio-button
          v-for="item in payload.metricOptions || []"
          :key="item.value"
          :label="item.value"
        >
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>
    </template>

    <div ref="chart" class="dashboard-station-ranking__chart" />
  </app-section-card>
</template>

<script>
import echarts from '@/shared/plugins/echarts'
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'DashboardStationRanking',
  components: {
    AppSectionCard
  },
  props: {
    payload: {
      type: Object,
      default: () => ({
        metricOptions: [],
        rankings: []
      })
    }
  },
  data() {
    return {
      chartInstance: null
    }
  },
  watch: {
    payload: {
      deep: true,
      handler() {
        this.$nextTick(() => {
          this.renderChart()
        })
      }
    }
  },
  mounted() {
    this.chartInstance = echarts.init(this.$refs.chart)
    window.addEventListener('resize', this.handleResize)
    this.renderChart()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
  },
  methods: {
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },
    resolveBarColor(index) {
      if (index === 0) {
        return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#ffd700' },
          { offset: 1, color: '#ffa500' }
        ])
      }

      if (index === 1) {
        return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#d7dde8' },
          { offset: 1, color: '#9ea8b5' }
        ])
      }

      if (index === 2) {
        return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#cd7f32' },
          { offset: 1, color: '#b8860b' }
        ])
      }

      return '#1a8dff'
    },
    renderChart() {
      if (!this.chartInstance || !this.payload.rankings) {
        return
      }

      const rows = this.payload.rankings.slice(0, 6)
      this.chartInstance.setOption(
        {
          grid: {
            left: 10,
            right: 12,
            top: 18,
            bottom: 12,
            containLabel: true
          },
          xAxis: {
            type: 'value',
            axisLabel: {
              color: 'rgba(255,255,255,0.52)'
            },
            splitLine: {
              lineStyle: {
                color: 'rgba(255,255,255,0.06)'
              }
            }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: rows.map(item => item.stationName),
            axisLabel: {
              color: 'rgba(255,255,255,0.72)',
              width: 110,
              overflow: 'truncate'
            },
            axisLine: {
              show: false
            },
            axisTick: {
              show: false
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            backgroundColor: 'rgba(8, 24, 54, 0.92)',
            borderColor: 'rgba(255,255,255,0.08)'
          },
          series: [
            {
              type: 'bar',
              barWidth: 16,
              data: rows.map((item, index) => ({
                value: item.value,
                itemStyle: {
                  borderRadius: [0, 12, 12, 0],
                  color: this.resolveBarColor(index)
                }
              })),
              label: {
                show: true,
                position: 'right',
                color: 'rgba(255,255,255,0.84)',
                formatter: params => `${params.value} ${this.payload.unit}`
              }
            }
          ]
        },
        true
      )
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard-station-ranking__chart {
  height: 320px;
}
</style>
