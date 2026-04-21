<template>
  <app-section-card :title="title" :subtitle="subtitle">
    <div ref="chart" :data-testid="testId" class="power-curve-panel__chart" />
  </app-section-card>
</template>

<script>
import AppSectionCard from '@/components/AppSectionCard.vue'

export default {
  name: 'PowerCurvePanel',
  components: {
    AppSectionCard
  },
  props: {
    chartData: {
      type: Object,
      default: () => ({})
    },
    title: {
      type: String,
      default: '出力曲线'
    },
    subtitle: {
      type: String,
      default: ''
    },
    testId: {
      type: String,
      default: 'power-curve-panel'
    }
  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
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
      if (!this.chart || !this.chartData || !this.chartData.curve) {
        return
      }

      const curve = this.chartData.curve

      this.chart.setOption(
        {
          backgroundColor: 'transparent',
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
            top: 42,
            left: 48,
            right: 18,
            bottom: 32
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: curve.axis || [],
            axisLine: {
              lineStyle: {
                color: 'rgba(164, 188, 232, 0.28)'
              }
            },
            axisLabel: {
              color: '#9ab0d8'
            }
          },
          yAxis: [
            {
              type: 'value',
              name: curve.powerUnit || 'MW',
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
              name: curve.weatherUnit || 'W/m²',
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
              name: '实际出力',
              type: 'line',
              smooth: true,
              data: curve.actual || [],
              symbol: 'none',
              lineStyle: {
                color: '#1a8dff',
                width: 3
              },
              areaStyle: {
                color: 'rgba(26, 141, 255, 0.12)'
              }
            },
            {
              name: '预测出力',
              type: 'line',
              smooth: true,
              data: curve.forecast || [],
              symbol: 'none',
              lineStyle: {
                color: '#00b578',
                width: 2
              }
            },
            {
              name: '调度基线',
              type: 'line',
              smooth: true,
              data: curve.baseline || [],
              symbol: 'none',
              lineStyle: {
                color: '#f59b23',
                width: 2,
                type: 'dashed'
              }
            },
            {
              name: '辐照度',
              type: 'bar',
              yAxisIndex: 1,
              barWidth: 10,
              data: curve.irradiance || [],
              itemStyle: {
                color: 'rgba(130, 208, 255, 0.28)'
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
.power-curve-panel__chart {
  height: 360px;
}
</style>
