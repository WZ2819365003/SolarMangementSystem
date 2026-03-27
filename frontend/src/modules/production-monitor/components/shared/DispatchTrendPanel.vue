<template>
  <app-section-card title="指令执行趋势" subtitle="关注当日指令下发、执行完成与响应时长">
    <div ref="chart" data-testid="dispatch-trend-panel" class="dispatch-trend-panel__chart" />
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'DispatchTrendPanel',
  components: {
    AppSectionCard
  },
  props: {
    trendData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    trendData: {
      deep: true,
      handler() {
        this.$nextTick(() => {
          this.renderChart()
        })
      }
    }
  },
  mounted() {
    this.chart = this.$echarts.init(this.$refs.chart)
    this.renderChart()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },
    renderChart() {
      if (!this.chart || !this.trendData) {
        return
      }

      this.chart.setOption(
        {
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            top: 0,
            textStyle: {
              color: '#b7c8e6'
            }
          },
          grid: {
            top: 40,
            left: 42,
            right: 18,
            bottom: 24
          },
          xAxis: {
            type: 'category',
            data: this.trendData.axis || [],
            axisLabel: {
              color: '#9ab0d8'
            },
            axisLine: {
              lineStyle: {
                color: 'rgba(164, 188, 232, 0.28)'
              }
            }
          },
          yAxis: [
            {
              type: 'value',
              axisLabel: {
                color: '#9ab0d8'
              },
              splitLine: {
                lineStyle: {
                  color: 'rgba(164, 188, 232, 0.12)'
                }
              }
            },
            {
              type: 'value',
              axisLabel: {
                color: '#9ab0d8'
              },
              splitLine: {
                show: false
              }
            }
          ],
          series: [
            {
              name: '下发指令数',
              type: 'bar',
              data: this.trendData.issued || [],
              barWidth: 12,
              itemStyle: {
                color: 'rgba(26, 141, 255, 0.72)'
              }
            },
            {
              name: '成功执行数',
              type: 'bar',
              data: this.trendData.success || [],
              barWidth: 12,
              itemStyle: {
                color: 'rgba(0, 181, 120, 0.72)'
              }
            },
            {
              name: '响应时长',
              type: 'line',
              yAxisIndex: 1,
              smooth: true,
              symbol: 'none',
              data: this.trendData.responseSeconds || [],
              lineStyle: {
                color: '#f59b23',
                width: 2
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
.dispatch-trend-panel__chart {
  height: 320px;
}
</style>
