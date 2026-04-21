<template>
  <app-section-card :title="title" :subtitle="subtitle">
    <div ref="chart" :data-testid="testId" class="weather-trend-panel__chart" />
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'WeatherTrendPanel',
  components: {
    AppSectionCard
  },
  props: {
    title: {
      type: String,
      default: '天气趋势'
    },
    subtitle: {
      type: String,
      default: ''
    },
    testId: {
      type: String,
      default: 'weather-trend-panel'
    },
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
          series: (this.trendData.series || []).map((item, index) => ({
            name: item.name,
            type: item.type || 'line',
            yAxisIndex: item.yAxisIndex || 0,
            smooth: true,
            symbol: 'none',
            data: item.data,
            lineStyle: {
              width: 2,
              color: item.color || ['#1a8dff', '#00b578', '#f59b23'][index % 3]
            },
            itemStyle: {
              color: item.color || ['#1a8dff', '#00b578', '#f59b23'][index % 3]
            },
            areaStyle: item.area
              ? {
                  color: item.area
                }
              : undefined,
            barWidth: item.type === 'bar' ? 10 : undefined
          }))
        },
        true
      )
    }
  }
}
</script>

<style lang="less" scoped>
.weather-trend-panel__chart {
  height: 320px;
}
</style>
